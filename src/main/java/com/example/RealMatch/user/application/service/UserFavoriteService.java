package com.example.RealMatch.user.application.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.brand.domain.entity.BrandDescribeTag;
import com.example.RealMatch.brand.domain.repository.BrandDescribeTagRepository;
import com.example.RealMatch.match.domain.entity.enums.BrandSortType;
import com.example.RealMatch.match.domain.entity.enums.CampaignSortType;
import com.example.RealMatch.user.domain.repository.UserFavoriteBrandQueryRepository;
import com.example.RealMatch.user.domain.repository.UserFavoriteCampaignQueryRepository;
import com.example.RealMatch.user.presentation.dto.response.FavoriteBrandDto;
import com.example.RealMatch.user.presentation.dto.response.FavoriteBrandListResponseDto;
import com.example.RealMatch.user.presentation.dto.response.FavoriteCampaignDto;
import com.example.RealMatch.user.presentation.dto.response.FavoriteCampaignListResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFavoriteService {

    private final UserFavoriteCampaignQueryRepository userFavoriteCampaignQueryRepository;
    private final UserFavoriteBrandQueryRepository userFavoriteBrandQueryRepository;
    private final BrandDescribeTagRepository brandDescribeTagRepository;

    /* ===== 찜한 브랜드 ===== */
    public FavoriteBrandListResponseDto getMyFavoriteBrands(
            Long userId,
            BrandSortType sortType
    ) {
        List<FavoriteBrandDto> brands =
                userFavoriteBrandQueryRepository.findFavoriteBrands(userId, sortType);

        if (brands.isEmpty()) {
            return FavoriteBrandListResponseDto.empty();
        }

        List<Long> brandIds = brands.stream()
                .map(FavoriteBrandDto::getBrandId)
                .toList();

        List<BrandDescribeTag> tags =
                brandDescribeTagRepository.findAllByBrandIdIn(brandIds);

        Map<Long, List<String>> tagMap = tags.stream()
                .collect(Collectors.groupingBy(
                        tag -> tag.getBrand().getId(),
                        Collectors.mapping(
                                BrandDescribeTag::getBrandDescribeTag,
                                Collectors.toList()
                        )
                ));

        brands.forEach(dto ->
                dto.setTags(tagMap.getOrDefault(dto.getBrandId(), List.of()))
        );

        return FavoriteBrandListResponseDto.builder()
                .count(brands.size())
                .brands(brands)
                .build();
    }

    /* ===== 찜한 캠페인 ===== */
    public FavoriteCampaignListResponseDto getMyFavoriteCampaigns(
            Long userId,
            CampaignSortType sortType
    ) {
        List<FavoriteCampaignDto> campaigns =
                userFavoriteCampaignQueryRepository.findFavoriteCampaigns(userId, sortType);

        if (campaigns.isEmpty()) {
            return FavoriteCampaignListResponseDto.empty();
        }

        return FavoriteCampaignListResponseDto.builder()
                .count(campaigns.size())
                .campaigns(campaigns)
                .build();
    }
}
