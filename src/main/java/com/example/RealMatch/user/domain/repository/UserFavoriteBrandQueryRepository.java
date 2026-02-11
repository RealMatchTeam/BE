package com.example.RealMatch.user.domain.repository;

import java.util.List;

import com.example.RealMatch.match.domain.entity.enums.BrandSortType;
import com.example.RealMatch.user.presentation.dto.response.FavoriteBrandDto;

public interface UserFavoriteBrandQueryRepository {

    List<FavoriteBrandDto> findFavoriteBrands(
            Long userId,
            BrandSortType sortType
    );
}
