package com.example.RealMatch.brand.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.entity.BrandAvailableSponsor;
import com.example.RealMatch.brand.domain.entity.BrandLike;
import com.example.RealMatch.brand.domain.repository.BrandAvailableSponsorRepository;
import com.example.RealMatch.brand.domain.repository.BrandCategoryViewRepository;
import com.example.RealMatch.brand.domain.repository.BrandLikeRepository;
import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.brand.domain.repository.BrandTagParentRepository;
import com.example.RealMatch.brand.domain.repository.TagRepository;
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
import com.example.RealMatch.tag.domain.entity.BrandTagParent;
import com.example.RealMatch.tag.domain.entity.Tag;
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
    private final TagRepository tagRepository;

    public BrandDetailResponseDto getBrandDetail(Long brandId) {
        Long currentUserId = 1L;

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new IllegalArgumentException("Brand not found with id: " + brandId));

        final String skinCareParentName = "brandSkinCareTag";
        final String makeUpParentName = "brandMakeUpTag";

        List<String> brandTags = brandTagParentRepository.findByBrandId(brandId).stream()
                .map(BrandTagParent::getTagParentName)
                .filter(name -> !skinCareParentName.equalsIgnoreCase(name) && !makeUpParentName.equalsIgnoreCase(name))
                .collect(Collectors.toList());

        boolean isLiked = brandLikeRepository.existsByUserIdAndBrandId(currentUserId, brandId);

        List<String> brandCategories = brandCategoryViewRepository.findByBrandId(brandId).stream()
                .map(brandCategoryView -> brandCategoryView.getCategory().getName())
                .collect(Collectors.toList());

        List<Campaign> allCampaigns = campaignRepository.findByCreatedBy(brandId);

        List<BrandDetailResponseDto.BrandOnGoingCampaignDto> onGoingCampaigns = allCampaigns.stream()
                .filter(c -> c.getRecruitEndDate().isAfter(LocalDateTime.now()))
                .map(campaign -> BrandDetailResponseDto.BrandOnGoingCampaignDto.builder()
                        .brandId(brandId)
                        .brandName(brand.getBrandName())
                        .recruitingTotalNumber(campaign.getQuota())
                        .recruitedNumber(0)
                        .campaginDescription(campaign.getDescription())
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
                        .productId(sponsor.getId())
                        .productName(sponsor.getName())
                        .availableType("본품")
                        .availableQuantity(1)
                        .availableSize(0)
                        .build())
                .collect(Collectors.toList());

        List<Tag> allTags = tagRepository.findAllByBrand_IdWithTagParent(brandId);

        Map<String, List<String>> skincareTagsMap = allTags.stream()
                .filter(tag -> skinCareParentName.equalsIgnoreCase(tag.getTagParent().getTagParentName()))
                .collect(Collectors.groupingBy(Tag::getCategoryName,
                        Collectors.mapping(Tag::getName, Collectors.toList())));

        Map<String, List<String>> makeupTagsMap = allTags.stream()
                .filter(tag -> makeUpParentName.equalsIgnoreCase(tag.getTagParent().getTagParentName()))
                .collect(Collectors.groupingBy(Tag::getCategoryName,
                        Collectors.mapping(Tag::getName, Collectors.toList())));

        BrandDetailResponseDto.BrandSkinCareTagDto skinCareTagDto = BrandDetailResponseDto.BrandSkinCareTagDto.builder()
                .brandSkinType(skincareTagsMap.get("brandSkinType"))
                .brandMainFunction(skincareTagsMap.get("brandMainFunction"))
                .build();

        BrandDetailResponseDto.BrandMakeUpTagDto makeUpTagDto = BrandDetailResponseDto.BrandMakeUpTagDto.builder()
                .brandSkinType(makeupTagsMap.get("brandSkinType"))
                .brandMakeUpStyle(makeupTagsMap.get("brandMakeUpStyle"))
                .build();

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
        Long currentUserId = 1L;

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + currentUserId));
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new IllegalArgumentException("Brand not found with id: " + brandId));

        Optional<BrandLike> brandLike = brandLikeRepository.findByUserAndBrand(user, brand);

        if (brandLike.isPresent()) {
            brandLikeRepository.delete(brandLike.get());
            return false;
        } else {
            BrandLike newBrandLike = BrandLike.builder()
                    .user(user)
                    .brand(brand)
                    .build();
            brandLikeRepository.save(newBrandLike);
            return true;
        }
    }

    public BrandFilterResponseDto getBrandFilters() {
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

        if (!product.getBrand().getId().equals(brandId)) {
            throw new IllegalArgumentException("해당 브랜드의 제품이 아닙니다.");
        }

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
                .productDescription(product.getCampaign().getDescription())
                .productImageUrls(mockImageUrls)
                .categories(mockCategories)
                .sponsorInfo(sponsorInfo)
                .action(action)
                .build();
    }
}
