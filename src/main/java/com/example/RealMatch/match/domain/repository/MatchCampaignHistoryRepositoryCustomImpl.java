package com.example.RealMatch.match.domain.repository;

import static com.example.RealMatch.brand.domain.entity.QBrand.brand;
import static com.example.RealMatch.business.domain.entity.QCampaignTag.campaignTag;
import static com.example.RealMatch.campaign.domain.entity.QCampaign.campaign;
import static com.example.RealMatch.match.domain.entity.QMatchCampaignHistory.matchCampaignHistory;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.example.RealMatch.campaign.domain.entity.QCampaignLike;
import com.example.RealMatch.match.domain.entity.MatchCampaignHistory;
import com.example.RealMatch.match.domain.entity.enums.CampaignSortType;
import com.example.RealMatch.match.domain.entity.enums.CategoryType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MatchCampaignHistoryRepositoryCustomImpl implements MatchCampaignHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MatchCampaignHistory> searchCampaigns(
            Long userId,
            String keyword,
            CategoryType category,
            CampaignSortType sortBy,
            List<String> tags,
            Pageable pageable
    ) {
        BooleanBuilder whereClause = buildWhereClause(userId, keyword, category, tags);
        List<OrderSpecifier<?>> orderSpecifiers = buildOrderSpecifiers(sortBy);

        List<MatchCampaignHistory> content = queryFactory
                .selectFrom(matchCampaignHistory)
                .join(matchCampaignHistory.campaign, campaign).fetchJoin()
                .join(campaign.brand, brand).fetchJoin()
                .where(whereClause)
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier<?>[0]))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = countSearchCampaigns(userId, keyword, category, tags);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public long countSearchCampaigns(Long userId, String keyword, CategoryType category, List<String> tags) {
        BooleanBuilder whereClause = buildWhereClause(userId, keyword, category, tags);

        Long count = queryFactory
                .select(matchCampaignHistory.count())
                .from(matchCampaignHistory)
                .join(matchCampaignHistory.campaign, campaign)
                .join(campaign.brand, brand)
                .where(whereClause)
                .fetchOne();

        return count != null ? count : 0L;
    }

    // *********** //
    // 검색 조건 빌더 //
    // *********** //
    private BooleanBuilder buildWhereClause(Long userId, String keyword, CategoryType category, List<String> tags) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(matchCampaignHistory.user.id.eq(userId));
        builder.and(matchCampaignHistory.isDeprecated.eq(false));

        LocalDateTime now = LocalDateTime.now();
        builder.and(
                campaign.recruitEndDate.isNull()
                        .or(campaign.recruitEndDate.after(now))
        );

        builder.and(campaign.isDeleted.eq(false));

        if (StringUtils.hasText(keyword)) {
            builder.and(campaign.title.containsIgnoreCase(keyword.trim()));
        }

        if (category != null && category != CategoryType.ALL) {
            builder.and(brand.industryType.stringValue().equalsIgnoreCase(category.name()));
        }

        // 태그 필터링: 캠페인이 해당 태그를 하나라도 가지고 있으면 통과
        if (tags != null && !tags.isEmpty()) {
            List<Long> tagIds = tags.stream()
                    .map(tag -> {
                        try {
                            return Long.parseLong(tag);
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    })
                    .filter(id -> id != null)
                    .toList();

            if (!tagIds.isEmpty()) {
                builder.and(
                        JPAExpressions
                                .selectOne()
                                .from(campaignTag)
                                .where(
                                        campaignTag.campaign.id.eq(campaign.id),
                                        campaignTag.tag.id.in(tagIds)
                                )
                                .exists()
                );
            }
        }

        return builder;
    }

    // *********** //
    // 정렬 조건 빌더 //
    // *********** //
    /**
     * 캠페인별 좋아요 수 (상관 서브쿼리, 정렬용).
     * 메인 쿼리의 campaign과 연관되어 행마다 해당 캠페인의 좋아요 수를 반환.
     */
    private com.querydsl.core.types.Expression<Long> likeCountSubquery() {
        QCampaignLike likeSub = new QCampaignLike("cl");
        return JPAExpressions
                .select(likeSub.id.count())
                .from(likeSub)
                .where(likeSub.campaign.id.eq(campaign.id));
    }

    /** 좋아요 수 내림차순 OrderSpecifier (null은 마지막) */
    private OrderSpecifier<?> likeCountDesc() {
        return new OrderSpecifier<>(Order.DESC, likeCountSubquery(), OrderSpecifier.NullHandling.NullsLast);
    }

    /**
     * 정렬 타입별 OrderSpecifier 목록.
     * 동률 시: 매칭률 → 인기 → id 순으로 2차·3차 정렬 적용.
     * - MATCH_SCORE: 매칭률 → 인기 → id
     * - POPULARITY: 인기 → 매칭률 → id
     * - REWARD_AMOUNT: 금액 → 매칭률 → 인기 → id
     * - D_DAY: 마감 → 매칭률 → 인기 → id
     */
    private List<OrderSpecifier<?>> buildOrderSpecifiers(CampaignSortType sortBy) {
        if (sortBy == null) {
            sortBy = CampaignSortType.MATCH_SCORE;
        }

        OrderSpecifier<?> matchingRatioDesc = matchCampaignHistory.matchingRatio.desc().nullsLast();
        OrderSpecifier<?> campaignIdAsc = campaign.id.asc();

        return switch (sortBy) {
            case MATCH_SCORE -> List.of(matchingRatioDesc, likeCountDesc(), campaignIdAsc);
            case POPULARITY -> List.of(likeCountDesc(), matchingRatioDesc, campaignIdAsc);
            case REWARD_AMOUNT -> List.of(
                    campaign.rewardAmount.desc().nullsLast(),
                    matchingRatioDesc,
                    likeCountDesc(),
                    campaignIdAsc
            );
            case D_DAY -> List.of(
                    campaign.recruitEndDate.asc().nullsLast(),
                    matchingRatioDesc,
                    likeCountDesc(),
                    campaignIdAsc
            );
        };
    }
}
