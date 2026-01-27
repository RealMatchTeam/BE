package com.example.RealMatch.brand.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.entity.BrandAvailableSponsor;
import com.example.RealMatch.brand.domain.repository.BrandAvailableSponsorRepository;
import com.example.RealMatch.brand.domain.repository.BrandCategoryViewRepository;
import com.example.RealMatch.brand.domain.repository.BrandLikeRepository;
import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.brand.domain.repository.BrandTagParentRepository;
import com.example.RealMatch.brand.presentation.dto.response.BrandDetailResponseDto;
import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.campaign.domain.repository.CampaignRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandService {

    private final BrandRepository brandRepository;
    private final BrandTagParentRepository brandTagParentRepository;
    private final BrandLikeRepository brandLikeRepository;
    private final BrandCategoryViewRepository brandCategoryViewRepository;
    private final CampaignRepository campaignRepository;
    private final BrandAvailableSponsorRepository brandAvailableSponsorRepository;

    public BrandDetailResponseDto getBrandDetail(Long brandId) {
        // TODO: Replace with actual user ID from security context
        Long currentUserId = 1L;

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new IllegalArgumentException("Brand not found with id: " + brandId));

        List<String> brandTags = brandTagParentRepository.findByBrandId(brandId).stream()
                .map(brandTagParent -> brandTagParent.getTagParentName())
                .collect(Collectors.toList());

        boolean isLiked = brandLikeRepository.existsByUserIdAndBrandId(currentUserId, brandId);

        List<String> brandCategories = brandCategoryViewRepository.findByBrandId(brandId).stream()
                .map(brandCategoryView -> brandCategoryView.getCategory().getName())
                .collect(Collectors.toList());

        // Assuming createdBy in Campaign refers to brandId
        List<Campaign> allCampaigns = campaignRepository.findByCreatedBy(brandId);

        List<BrandDetailResponseDto.BrandOnGoingCampaignDto> onGoingCampaigns = allCampaigns.stream()
                .filter(c -> c.getRecruitEndDate().isAfter(LocalDateTime.now()))
                .map(campaign -> BrandDetailResponseDto.BrandOnGoingCampaignDto.builder()
                        .brandId(brandId)
                        .brandName(brand.getBrandName())
                        .recruitingTotalNumber(campaign.getQuota())
                        .recruitedNumber(0) // TODO: Need to implement logic to count recruited number
                        .campaignDescription(campaign.getDescription())
                        .campaginManuscriptFee(String.valueOf(campaign.getRewardAmount()))
                        .build())
                .collect(Collectors.toList());

        List<BrandDetailResponseDto.CampaignHistoryDto> campaignHistories = allCampaigns.stream()
                .filter(c -> c.getRecruitEndDate().isBefore(LocalDateTime.now()))
                .map(campaign -> BrandDetailResponseDto.CampaignHistoryDto.builder()
                        .campaignId(campaign.getId())
                        .campaignTitle(campaign.getTitle())
                        .startDate(campaign.getStartDate().toString())
                        .endDate(campaign.getEndDate().toString())
                        .build())
                .collect(Collectors.toList());

        List<BrandAvailableSponsor> availableSponsors = brandAvailableSponsorRepository.findByBrandId(brandId);
        List<BrandDetailResponseDto.AvailableSponsorProdDto> availableSponsorProds = availableSponsors.stream()
                .map(sponsor -> BrandDetailResponseDto.AvailableSponsorProdDto.builder()
                        .productId(sponsor.getId()) // Assuming sponsor id is product id
                        .productName(sponsor.getName())
                        .availableType("본품") // TODO: This seems to be hardcoded, check if it can be fetched from somewhere
                        .availableQuantity(1) // TODO: Hardcoded
                        .availableSize(0) // TODO: Hardcoded
                        .build())
                .collect(Collectors.toList());

        // TODO: Logic for skin care and makeup tags needs to be implemented
        BrandDetailResponseDto.BrandSkinCareTagDto skinCareTagDto = BrandDetailResponseDto.BrandSkinCareTagDto.builder().build();
        BrandDetailResponseDto.BrandMakeUpTagDto makeUpTagDto = BrandDetailResponseDto.BrandMakeUpTagDto.builder().build();

        return BrandDetailResponseDto.builder()
                .brandName(brand.getBrandName())
                .brandTag(brandTags)
                .brandDescription(brand.getDetailIntro())
                .brandMatchingRatio(brand.getMatchingRate())
                .brandIsLiked(isLiked)
                .brandCategory(brandCategories)
                .brandSkinCareTag(skinCareTagDto)
                .brandMakeUpTag(makeUpTagDto)
                .brandOnGoingCampaign(onGoingCampaigns)
                .availableSponsorProd(availableSponsorProds)
                .campaignHistory(campaignHistories)
                .build();
    }
}
