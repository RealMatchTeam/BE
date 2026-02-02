package com.example.RealMatch.match.domain.repository;

import static com.example.RealMatch.brand.domain.entity.QBrand.brand;
import static com.example.RealMatch.campaign.domain.entity.QCampaign.campaign;
import static com.example.RealMatch.match.domain.entity.QMatchCampaignHistory.matchCampaignHistory;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.example.RealMatch.match.domain.entity.MatchCampaignHistory;
import com.example.RealMatch.match.domain.entity.enums.CategoryType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

/**
 * 캠페인 매칭 히스토리 검색/페이지네이션
 */
@Repository
@RequiredArgsConstructor
public class MatchCampaignHistoryRepositoryCustomImpl implements MatchCampaignHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MatchCampaignHistory> searchCampaigns(
            Long userId,
            String keyword,
            CategoryType category,
            Pageable pageable
    ) {
        BooleanBuilder whereClause = buildWhereClause(userId, keyword, category);

        List<MatchCampaignHistory> content = queryFactory
                .selectFrom(matchCampaignHistory)
                .join(matchCampaignHistory.campaign, campaign).fetchJoin()
                .join(campaign.brand, brand).fetchJoin()
                .where(whereClause)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = countSearchCampaigns(userId, keyword, category);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public long countSearchCampaigns(Long userId, String keyword, CategoryType category) {
        BooleanBuilder whereClause = buildWhereClause(userId, keyword, category);

        Long count = queryFactory
                .select(matchCampaignHistory.count())
                .from(matchCampaignHistory)
                .join(matchCampaignHistory.campaign, campaign)
                .join(campaign.brand, brand)
                .where(whereClause)
                .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * 검색 조건 빌더
     */
    private BooleanBuilder buildWhereClause(Long userId, String keyword, CategoryType category) {
        BooleanBuilder builder = new BooleanBuilder();

        // 필수 조건: 사용자 ID, deprecated=false
        builder.and(matchCampaignHistory.user.id.eq(userId));
        builder.and(matchCampaignHistory.isDeprecated.eq(false));

        // 모집 중인 캠페인만 (recruitEndDate가 null이거나 현재보다 이후)
        LocalDateTime now = LocalDateTime.now();
        builder.and(
                campaign.recruitEndDate.isNull()
                        .or(campaign.recruitEndDate.after(now))
        );

        // 삭제되지 않은 캠페인만
        builder.and(campaign.isDeleted.eq(false));

        // 캠페인명 검색 (keyword)
        if (StringUtils.hasText(keyword)) {
            builder.and(campaign.title.containsIgnoreCase(keyword.trim()));
        }

        // 카테고리 필터
        if (category != null && category != CategoryType.ALL) {
            builder.and(brand.industryType.stringValue().equalsIgnoreCase(category.name()));
        }

        return builder;
    }
}
