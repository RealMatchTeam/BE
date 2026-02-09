package com.example.RealMatch.user.domain.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.RealMatch.brand.domain.entity.QBrand;
import com.example.RealMatch.brand.domain.entity.QBrandLike;
import com.example.RealMatch.match.domain.entity.QMatchBrandHistory;
import com.example.RealMatch.match.domain.entity.enums.BrandSortType;
import com.example.RealMatch.user.presentation.dto.response.FavoriteBrandDto;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserFavoriteBrandRepositoryImpl
        implements UserFavoriteBrandQueryRepository {

    private final JPAQueryFactory queryFactory;

    // ✅ Checkstyle 대응: static final → 대문자 상수
    private static final QBrandLike BRAND_LIKE = QBrandLike.brandLike;
    private static final QBrand BRAND = QBrand.brand;
    private static final QMatchBrandHistory MATCH_BRAND_HISTORY =
            QMatchBrandHistory.matchBrandHistory;

    @Override
    public List<FavoriteBrandDto> findFavoriteBrands(
            Long userId,
            BrandSortType sortType
    ) {
        List<OrderSpecifier<?>> orderSpecifiers =
                buildBrandOrderSpecifiers(sortType);

        return queryFactory
                .select(Projections.constructor(
                        FavoriteBrandDto.class,
                        BRAND.id,
                        BRAND.brandName,
                        BRAND.logoUrl,
                        brandMatchingRatioExpression(),
                        brandLikeCountExpression(),
                        Expressions.nullExpression(List.class) // tags는 나중에 채움
                ))
                .from(BRAND_LIKE)
                .join(BRAND_LIKE.brand, BRAND)
                .leftJoin(MATCH_BRAND_HISTORY).on(
                        MATCH_BRAND_HISTORY.brand.id.eq(BRAND.id),
                        MATCH_BRAND_HISTORY.user.id.eq(userId),
                        MATCH_BRAND_HISTORY.isDeprecated.eq(false)
                )
                .where(BRAND_LIKE.user.id.eq(userId))
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                .fetch();
    }

    private List<OrderSpecifier<?>> buildBrandOrderSpecifiers(
            BrandSortType sortType
    ) {
        if (sortType == null) {
            sortType = BrandSortType.MATCH_SCORE;
        }

        return switch (sortType) {
            case MATCH_SCORE -> List.of(
                    MATCH_BRAND_HISTORY.matchingRatio.desc().nullsLast(),
                    BRAND.id.asc()
            );

            case POPULARITY -> List.of(
                    popularityDesc(),
                    MATCH_BRAND_HISTORY.matchingRatio.desc().nullsLast(),
                    BRAND.id.asc()
            );

            case NEWEST -> List.of(
                    BRAND.createdAt.desc(),
                    BRAND.id.asc()
            );
        };
    }

    private OrderSpecifier<?> popularityDesc() {
        return new OrderSpecifier<>(
                com.querydsl.core.types.Order.DESC,
                brandLikeCountExpression(),
                OrderSpecifier.NullHandling.NullsLast
        );
    }

    private Expression<Long> brandLikeCountExpression() {
        QBrandLike bl = QBrandLike.brandLike;

        return JPAExpressions
                .select(bl.id.count())
                .from(bl)
                .where(bl.brand.id.eq(BRAND.id));
    }

    private Expression<Long> brandMatchingRatioExpression() {
        return Expressions.numberTemplate(
                Long.class,
                "COALESCE({0}, 0)",
                MATCH_BRAND_HISTORY.matchingRatio
        );
    }
}
