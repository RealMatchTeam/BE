package com.example.RealMatch.brand.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.entity.BrandAvailableSponsor;
import com.example.RealMatch.brand.domain.entity.BrandLike;
import com.example.RealMatch.brand.domain.entity.BrandTagParent;
import com.example.RealMatch.brand.domain.repository.BrandAvailableSponsorRepository;
import com.example.RealMatch.brand.domain.repository.BrandCategoryViewRepository;
import com.example.RealMatch.brand.domain.repository.BrandLikeRepository;
import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.brand.domain.repository.BrandTagParentRepository;
import com.example.RealMatch.brand.presentation.dto.response.ActionDto;
import com.example.RealMatch.brand.presentation.dto.response.BeautyFilterDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandDetailResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandFilterResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.SponsorInfoDto;
import com.example.RealMatch.brand.presentation.dto.response.SponsorItemDto;
import com.example.RealMatch.brand.presentation.dto.response.SponsorProductDetailResponseDto;
import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.campaign.domain.repository.CampaignRepository;
import com.example.RealMatch.global.presentation.advice.ResourceNotFoundException;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.repository.UserRepository;

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
    private final UserRepository userRepository;
    // private final BrandSponsorImageRepository brandSponsorImageRepository; // Assuming this exists

    public BrandDetailResponseDto getBrandDetail(Long brandId) {
        // TODO: Replace with actual user ID from security context
        Long currentUserId = 1L;

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new IllegalArgumentException("Brand not found with id: " + brandId));

        List<String> brandTags = brandTagParentRepository.findByBrandId(brandId).stream()
                .map(BrandTagParent::getTagParentName)
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
                        .campaignManuscriptFee(String.valueOf(campaign.getRewardAmount()))
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

    @Transactional
    public Boolean likeBrand(Long brandId) {
        // TODO: Replace with actual user ID from security context
        Long currentUserId = 1L;

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + currentUserId));
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new IllegalArgumentException("Brand not found with id: " + brandId));

        Optional<BrandLike> brandLike = brandLikeRepository.findByUserAndBrand(user, brand);

        if (brandLike.isPresent()) {
            brandLikeRepository.delete(brandLike.get());
            return false; // 좋아요 취소
        } else {
            BrandLike newBrandLike = BrandLike.builder()
                    .user(user)
                    .brand(brand)
                    .build();
            brandLikeRepository.save(newBrandLike);
            return true; // 좋아요 추가
        }
    }

    public BrandFilterResponseDto getBrandFilters() {
        // List<CategoryDto> categories = categoryRepository.findAll().stream()
        //         .map(c -> new CategoryDto(c.getId(), c.getName()))
        //         .collect(Collectors.toList());

        // List<FunctionDto> functions = functionRepository.findAll().stream()
        //         .map(f -> new FunctionDto(f.getId(), f.getName()))
        //         .collect(Collectors.toList());

        // List<SkinTypeDto> skinTypes = skinTypeRepository.findAll().stream()
        //         .map(s -> new SkinTypeDto(s.getId(), s.getName()))
        //         .collect(Collectors.toList());

        // List<MakeUpStyleDto> makeUpStyles = makeUpStyleRepository.findAll().stream()
        //         .map(m -> new MakeUpStyleDto(m.getId(), m.getName()))
        //         .collect(Collectors.toList());

        // BeautyFilterDto beautyFilter = BeautyFilterDto.builder()
        //         .category(categories)
        //         .function(functions)
        //         .skinType(skinTypes)
        //         .makeUpStyle(makeUpStyles)
        //         .build();

        // return BrandFilterResponseDto.builder()
        //         .beautyFilter(beautyFilter)
        //         .build();

        // For now, returning empty lists as the repositories are not yet available.
        BeautyFilterDto beautyFilter = BeautyFilterDto.builder()
                .category(List.of())
                .function(List.of())
                .skinType(List.of())
                .makeUpStyle(List.of())
                .build();

        return BrandFilterResponseDto.builder()
                .beautyFilter(beautyFilter)
                .build();
    }

    public SponsorProductDetailResponseDto getSponsorProductDetail(Long brandId, Long productId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("브랜드 정보를 찾을 수 없습니다."));

        BrandAvailableSponsor product = brandAvailableSponsorRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("제품 정보를 찾을 수 없습니다."));

        // Check if the product belongs to the brand
        if (!product.getBrand().getId().equals(brandId)) {
            throw new IllegalArgumentException("해당 브랜드의 제품이 아닙니다.");
        }

        // Mock data for now, assuming repositories and entities need to be created/updated
        List<String> mockImageUrls = List.of(
                "https://cdn.example.com/products/100/1.png",
                "https://cdn.example.com/products/100/2.png",
                "https://cdn.example.com/products/100/3.png"
        );

        List<String> mockCategories = List.of("스킨케어", "메이크업");

        List<SponsorItemDto> mockItems = List.of(
                SponsorItemDto.builder().itemId(1L).availableType("SAMPLE").availableQuantity(1).availableSize(50).sizeUnit("ml").build(),
                SponsorItemDto.builder().itemId(2L).availableType("FULL").availableQuantity(1).availableSize(100).sizeUnit("ml").build()
        );

        SponsorInfoDto sponsorInfo = SponsorInfoDto.builder()
                .items(mockItems)
                .shippingType("CREATOR_PAY")
                .build();

        ActionDto action = ActionDto.builder()
                .canProposeCampaign(true)
                .proposeCampaignCtaText("캠페인 제안하기")
                .build();

        return SponsorProductDetailResponseDto.builder()
                .brandId(brand.getId())
                .brandName(brand.getBrandName())
                .productId(product.getId())
                .productName(product.getName())
                .productDescription(product.getCampaign().getDescription()) // Assuming description field exists
                .productImageUrls(mockImageUrls) // Replace with actual image fetching logic
                .categories(mockCategories) // Replace with actual category fetching logic
                .sponsorInfo(sponsorInfo)
                .action(action)
                .build();
    }
}
