package com.example.RealMatch.match.domain.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.RealMatch.match.domain.entity.MatchBrandHistory;
import com.example.RealMatch.match.domain.entity.enums.BrandSortType;
import com.example.RealMatch.match.domain.entity.enums.CategoryType;

public interface MatchBrandHistoryRepositoryCustom {

    Page<MatchBrandHistory> searchBrands(
            Long userId,
            String title,
            CategoryType category,
            BrandSortType sortBy,
            List<String> tags,
            Pageable pageable
    );

    long countSearchBrands(
            Long userId,
            String title,
            CategoryType category,
            List<String> tags
    );
}
