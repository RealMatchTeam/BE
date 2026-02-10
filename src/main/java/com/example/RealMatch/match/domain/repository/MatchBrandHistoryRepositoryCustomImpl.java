package com.example.RealMatch.match.domain.repository;

import static com.example.RealMatch.brand.domain.entity.QBrand.brand;
import static com.example.RealMatch.brand.domain.entity.QBrandDescribeTag.brandDescribeTag;
import static com.example.RealMatch.match.domain.entity.QMatchBrandHistory.matchBrandHistory;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.example.RealMatch.brand.domain.entity.QBrandLike;
import com.example.RealMatch.match.domain.entity.MatchBrandHistory;
import com.example.RealMatch.match.domain.entity.enums.BrandSortType;
import com.example.RealMatch.match.domain.entity.enums.CategoryType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MatchBrandHistoryRepositoryCustomImpl implements MatchBrandHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MatchBrandHistory> searchBrands(
            Long userId,
            String title,
            CategoryType category,
            BrandSortType sortBy,
            List<String> tags,
            Pageable pageable
    ) {
        BooleanBuilder whereClause = buildWhereClause(userId, title, category, tags);
        List<OrderSpecifier<?>> orderSpecifiers = buildOrderSpecifiers(sortBy);

        List<MatchBrandHistory> content = queryFactory
                .selectFrom(matchBrandHistory)
                .join(matchBrandHistory.brand, brand).fetchJoin()
                .where(whereClause)
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier<?>[0]))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = countSearchBrands(userId, title, category, tags);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public long countSearchBrands(Long userId, String title, CategoryType category, List<String> tags) {
        BooleanBuilder whereClause = buildWhereClause(userId, title, category, tags);

        Long count = queryFactory
                .select(matchBrandHistory.count())
                .from(matchBrandHistory)
                .join(matchBrandHistory.brand, brand)
                .where(whereClause)
                .fetchOne();

        return count != null ? count : 0L;
    }

    // *********** //
    // 검색 조건 빌더 //
    // *********** //
    private BooleanBuilder buildWhereClause(Long userId, String title, CategoryType category, List<String> tags) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(matchBrandHistory.user.id.eq(userId));
        builder.and(matchBrandHistory.isDeprecated.eq(false));

        if (StringUtils.hasText(title)) {
            builder.and(brand.brandName.containsIgnoreCase(title.trim()));
        }

        if (category != null && category != CategoryType.ALL) {
            builder.and(brand.industryType.stringValue().equalsIgnoreCase(category.name()));
        }

        if (tags != null && !tags.isEmpty()) {
            List<String> normalizedTags = tags.stream()
                    .filter(StringUtils::hasText)
                    .map(tag -> tag.trim().toLowerCase())
                    .toList();

            if (!normalizedTags.isEmpty()) {
                StringExpression tagLower = brandDescribeTag.brandDescribeTag.lower();
                builder.and(
                        JPAExpressions
                                .selectOne()
                                .from(brandDescribeTag)
                                .where(
                                        brandDescribeTag.brand.id.eq(brand.id),
                                        tagLower.in(normalizedTags)
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
    private com.querydsl.core.types.Expression<Long> likeCountSubquery() {
        QBrandLike likeSub = new QBrandLike("bl");
        return JPAExpressions
                .select(likeSub.id.count())
                .from(likeSub)
                .where(likeSub.brand.id.eq(brand.id));
    }

    private OrderSpecifier<?> likeCountDesc() {
        return new OrderSpecifier<>(Order.DESC, likeCountSubquery(), OrderSpecifier.NullHandling.NullsLast);
    }

    private List<OrderSpecifier<?>> buildOrderSpecifiers(BrandSortType sortBy) {
        if (sortBy == null) {
            sortBy = BrandSortType.MATCH_SCORE;
        }

        OrderSpecifier<?> matchingRatioDesc = matchBrandHistory.matchingRatio.desc().nullsLast();
        OrderSpecifier<?> brandIdDesc = brand.id.desc();
        OrderSpecifier<?> popularityDesc = likeCountDesc();

        return switch (sortBy) {
            case POPULARITY -> List.of(popularityDesc, matchingRatioDesc, brandIdDesc);
            case NEWEST -> List.of(brandIdDesc);
            default -> List.of(matchingRatioDesc, popularityDesc, brandIdDesc);
        };
    }
}
