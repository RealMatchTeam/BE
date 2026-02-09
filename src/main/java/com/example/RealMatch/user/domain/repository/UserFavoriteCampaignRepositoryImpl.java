package com.example.RealMatch.user.domain.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.RealMatch.brand.domain.entity.QBrand;
import com.example.RealMatch.campaign.domain.entity.QCampaign;
import com.example.RealMatch.campaign.domain.entity.QCampaignLike;
import com.example.RealMatch.match.domain.entity.QMatchCampaignHistory;
import com.example.RealMatch.match.domain.entity.enums.CampaignSortType;
import com.example.RealMatch.user.presentation.dto.response.FavoriteCampaignDto;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserFavoriteCampaignRepositoryImpl
        implements UserFavoriteCampaignQueryRepository {

    private final JPAQueryFactory queryFactory;

    // ✅ Checkstyle 대응: static final → 대문자 상수
    private static final QCampaignLike CAMPAIGN_LIKE = QCampaignLike.campaignLike;
    private static final QCampaign CAMPAIGN = QCampaign.campaign;
    private static final QBrand BRAND = QBrand.brand;
    private static final QMatchCampaignHistory MATCH_CAMPAIGN_HISTORY =
            QMatchCampaignHistory.matchCampaignHistory;

    @Override
    public List<FavoriteCampaignDto> findFavoriteCampaigns(
            Long userId,
            CampaignSortType sortType
    ) {
        List<OrderSpecifier<?>> orderSpecifiers =
                buildCampaignOrderSpecifiers(sortType);

        return queryFactory
                .select(Projections.constructor(
                        FavoriteCampaignDto.class,
                        CAMPAIGN.id,
                        CAMPAIGN.title,
                        dDayExpression(),
                        CAMPAIGN.rewardAmount,
                        CAMPAIGN.quota,
                        campaignMatchingRatioExpression(),
                        campaignLikeCountExpression(),
                        BRAND.id,
                        BRAND.brandName,
                        BRAND.logoUrl
                ))
                .from(CAMPAIGN_LIKE)
                .join(CAMPAIGN_LIKE.campaign, CAMPAIGN)
                .join(CAMPAIGN.brand, BRAND)
                .leftJoin(MATCH_CAMPAIGN_HISTORY).on(
                        MATCH_CAMPAIGN_HISTORY.campaign.id.eq(CAMPAIGN.id),
                        MATCH_CAMPAIGN_HISTORY.user.id.eq(userId),
                        MATCH_CAMPAIGN_HISTORY.isDeprecated.eq(false)
                )
                .where(CAMPAIGN_LIKE.user.id.eq(userId))
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                .fetch();
    }

    private List<OrderSpecifier<?>> buildCampaignOrderSpecifiers(
            CampaignSortType sortType
    ) {
        if (sortType == null) {
            sortType = CampaignSortType.MATCH_SCORE;
        }

        return switch (sortType) {
            case MATCH_SCORE -> List.of(
                    campaignMatchingRatioDesc(),
                    popularityDesc(),
                    campaignIdAsc()
            );

            case POPULARITY -> List.of(
                    popularityDesc(),
                    campaignMatchingRatioDesc(),
                    campaignIdAsc()
            );

            case REWARD_AMOUNT -> List.of(
                    CAMPAIGN.rewardAmount.desc().nullsLast(),
                    campaignMatchingRatioDesc(),
                    popularityDesc(),
                    campaignIdAsc()
            );

            case D_DAY -> List.of(
                    dDayAscNullsLast(),
                    campaignMatchingRatioDesc(),
                    popularityDesc(),
                    campaignIdAsc()
            );
        };
    }

    private OrderSpecifier<?> campaignMatchingRatioDesc() {
        return MATCH_CAMPAIGN_HISTORY.matchingRatio.desc().nullsLast();
    }

    private OrderSpecifier<?> popularityDesc() {
        return new OrderSpecifier<>(
                com.querydsl.core.types.Order.DESC,
                campaignLikeCountExpression(),
                OrderSpecifier.NullHandling.NullsLast
        );
    }

    private OrderSpecifier<?> campaignIdAsc() {
        return CAMPAIGN.id.asc();
    }

    private OrderSpecifier<?> dDayAscNullsLast() {
        return new OrderSpecifier<>(
                com.querydsl.core.types.Order.ASC,
                dDayExpression(),
                OrderSpecifier.NullHandling.NullsLast
        );
    }

    private Expression<Long> campaignLikeCountExpression() {
        QCampaignLike cl = QCampaignLike.campaignLike;

        return JPAExpressions
                .select(cl.id.count())
                .from(cl)
                .where(cl.campaign.id.eq(CAMPAIGN.id));
    }

    private Expression<Integer> dDayExpression() {
        return Expressions.numberTemplate(
                Integer.class,
                "DATEDIFF({0}, CURDATE())",
                CAMPAIGN.recruitEndDate
        );
    }

    private Expression<Long> campaignMatchingRatioExpression() {
        return Expressions.numberTemplate(
                Long.class,
                "COALESCE({0}, 0)",
                MATCH_CAMPAIGN_HISTORY.matchingRatio
        );
    }
}
