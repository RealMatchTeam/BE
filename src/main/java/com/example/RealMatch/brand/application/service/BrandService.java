package com.example.RealMatch.brand.application.service;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.entity.BrandAvailableSponsor;
import com.example.RealMatch.brand.domain.entity.BrandCategory;
import com.example.RealMatch.brand.domain.entity.BrandCategoryView;
import com.example.RealMatch.brand.domain.entity.BrandLike;
import com.example.RealMatch.brand.domain.repository.BrandAvailableSponsorRepository;
import com.example.RealMatch.brand.domain.repository.BrandCategoryRepository;
import com.example.RealMatch.brand.domain.repository.BrandCategoryViewRepository;
import com.example.RealMatch.brand.domain.repository.BrandLikeRepository;
import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.brand.presentation.dto.request.BrandCreateRequestDto;
import com.example.RealMatch.brand.presentation.dto.request.BrandUpdateRequestDto;
import com.example.RealMatch.brand.presentation.dto.response.ActionDto;
import com.example.RealMatch.brand.presentation.dto.response.BeautyFilterDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandCreateResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandDetailResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandFilterResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandListResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.SponsorInfoDto;
import com.example.RealMatch.brand.presentation.dto.response.SponsorItemDto;
import com.example.RealMatch.brand.presentation.dto.response.SponsorProductDetailResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.SponsorProductListResponseDto;
import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.global.presentation.advice.ResourceNotFoundException;
import com.example.RealMatch.global.presentation.code.GeneralErrorCode;
import com.example.RealMatch.tag.domain.entity.BrandTag;
import com.example.RealMatch.tag.domain.entity.Tag;
import com.example.RealMatch.tag.domain.enums.TagType;
import com.example.RealMatch.tag.domain.repository.BrandTagRepository;
import com.example.RealMatch.tag.domain.repository.TagRepository;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandService {

    private final BrandRepository brandRepository;
    private final BrandTagRepository brandTagRepository;
    private final TagRepository tagRepository;
    private final BrandLikeRepository brandLikeRepository;
    private final BrandCategoryViewRepository brandCategoryViewRepository;
    private final BrandCategoryRepository brandCategoryRepository;
    private final BrandAvailableSponsorRepository brandAvailableSponsorRepository;
    private final UserRepository userRepository;

    private Long getCurrentUserId() {
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            return Long.parseLong(principal);
        } catch (NumberFormatException e) {
            throw new CustomException(GeneralErrorCode.UNAUTHORIZED);
        }
    }

    public BrandDetailResponseDto getBrandDetail(Long brandId) {
        Long currentUserId = getCurrentUserId();

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new IllegalArgumentException("Brand not found with id: " + brandId));

        List<BrandTag> brandTags = brandTagRepository.findAllByBrandIdWithTag(brandId);

        List<String> brandTagNames = brandTags.stream()
                .map(bt -> bt.getTag().getTagName())
                .collect(Collectors.toList());

        boolean isLiked = brandLikeRepository.existsByUserIdAndBrandId(currentUserId, brandId);

        List<String> brandCategories = brandCategoryViewRepository.findByBrandId(brandId).stream()
                .map(brandCategoryView -> brandCategoryView.getCategory().getName())
                .collect(Collectors.toList());

        Map<String, List<String>> skincareTagsMap = brandTags.stream()
                .filter(bt -> TagType.BEAUTY.getDescription().equals(bt.getTag().getTagType()))
                .filter(bt -> "스킨케어".equals(bt.getTag().getTagCategory()))
                .collect(Collectors.groupingBy(
                        bt -> bt.getTag().getTagCategory(),
                        Collectors.mapping(bt -> bt.getTag().getTagName(), Collectors.toList())
                ));

        Map<String, List<String>> makeupTagsMap = brandTags.stream()
                .filter(bt -> TagType.BEAUTY.getDescription().equals(bt.getTag().getTagType()))
                .filter(bt -> "메이크업".equals(bt.getTag().getTagCategory()))
                .collect(Collectors.groupingBy(
                        bt -> bt.getTag().getTagCategory(),
                        Collectors.mapping(bt -> bt.getTag().getTagName(), Collectors.toList())
                ));

        BrandDetailResponseDto.BrandSkinCareTagDto skinCareTagDto = BrandDetailResponseDto.BrandSkinCareTagDto.builder()
                .brandSkinType(skincareTagsMap.getOrDefault("스킨케어", List.of()))
                .brandMainFunction(List.of())
                .build();

        BrandDetailResponseDto.BrandMakeUpTagDto makeUpTagDto = BrandDetailResponseDto.BrandMakeUpTagDto.builder()
                .brandSkinType(List.of())
                .brandMakeUpStyle(makeupTagsMap.getOrDefault("메이크업", List.of()))
                .build();

        return BrandDetailResponseDto.builder()
                .brandName(brand.getBrandName())
                .brandTag(brandTagNames)
                .brandDescription(brand.getDetailIntro())
                .brandMatchingRatio(brand.getMatchingRate())
                .brandIsLiked(isLiked)
                .brandCategory(brandCategories)
                .brandSkinCareTag(skinCareTagDto)
                .brandMakeUpTag(makeUpTagDto)
                .build();
    }

    @Transactional
    public Boolean likeBrand(Long brandId) {
        Long currentUserId = getCurrentUserId();

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

    @Transactional(readOnly = true)
    public List<SponsorProductListResponseDto> getSponsorProducts(Long brandId) {
        brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("브랜드 정보를 찾을 수 없습니다."));

        List<BrandAvailableSponsor> products = brandAvailableSponsorRepository.findByBrandIdWithImages(brandId);

        return products.stream()
                .map(SponsorProductListResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public BrandCreateResponseDto createBrand(BrandCreateRequestDto requestDto) {
        Long currentUserId = getCurrentUserId();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        Optional<Brand> existingBrand = brandRepository.findByUser(user);
        if (existingBrand.isPresent()) {
            throw new IllegalArgumentException("이미 해당 유저(ID: " + user.getId() + ")의 브랜드(ID: " + existingBrand.get().getId() + ")가 존재합니다.");
        }

        Brand brand = requestDto.toEntity(user);

        if (requestDto.getBrandCategory() != null) {
            for (String categoryName : requestDto.getBrandCategory()) {
                BrandCategory category = brandCategoryRepository.findByName(categoryName)
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryName));
                brand.addBrandCategoryView(BrandCategoryView.builder().category(category).build());
            }
        }

        if (requestDto.getBrandSkinCareTag() != null) {
            BrandCreateRequestDto.BrandSkinCareTagDto skinCareTags = requestDto.getBrandSkinCareTag();
            if (skinCareTags.getSkinType() != null) {
                addTagsToBrand(brand, skinCareTags.getSkinType(), TagType.BEAUTY, "스킨케어");
            }
            if (skinCareTags.getMainFunction() != null) {
                addTagsToBrand(brand, skinCareTags.getMainFunction(), TagType.BEAUTY, "스킨케어");
            }
        }
        if (requestDto.getBrandMakeUpTag() != null) {
            BrandCreateRequestDto.BrandMakeUpTagDto makeUpTags = requestDto.getBrandMakeUpTag();
            if (makeUpTags.getSkinType() != null) {
                addTagsToBrand(brand, makeUpTags.getSkinType(), TagType.BEAUTY, "메이크업");
            }
            if (makeUpTags.getBrandMakeUpStyle() != null) {
                addTagsToBrand(brand, makeUpTags.getBrandMakeUpStyle(), TagType.BEAUTY, "메이크업");
            }
        }
        if (requestDto.getBrandClothingTag() != null) {
            BrandCreateRequestDto.BrandClothingTagDto clothingTags = requestDto.getBrandClothingTag();
            if (clothingTags.getBrandType() != null) {
                addTagsToBrand(brand, clothingTags.getBrandType(), TagType.FASHION, "의류");
            }
            if (clothingTags.getBrandStyle() != null) {
                addTagsToBrand(brand, clothingTags.getBrandStyle(), TagType.FASHION, "의류");
            }
        }

        Brand savedBrand = brandRepository.save(brand);

        return BrandCreateResponseDto.builder()
                .brandId(savedBrand.getId())
                .build();
    }

    @Transactional
    public void updateBrand(Long brandId, BrandUpdateRequestDto requestDto) {
        Long currentUserId = getCurrentUserId();

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + brandId));

        if (!brand.getUser().getId().equals(currentUserId)) {
            throw new CustomException(GeneralErrorCode.FORBIDDEN);
        }

        brand.update(
                requestDto.getBrandName(),
                requestDto.getLogoUrl(),
                requestDto.getSimpleIntro(),
                requestDto.getDetailIntro(),
                requestDto.getHomepageUrl(),
                currentUserId
        );

        brand.getBrandTags().clear();
        brand.getBrandCategoryViews().clear();
        brandRepository.saveAndFlush(brand);

        List<String> requestedCategories = requestDto.getBrandCategory();
        if (requestedCategories != null && !requestedCategories.isEmpty()) {
            for (String categoryName : requestedCategories) {
                brandCategoryRepository.findByName(categoryName).ifPresent(category ->
                        brand.addBrandCategoryView(BrandCategoryView.builder().category(category).build())
                );
            }
        }

        if (requestedCategories != null) {
            if (requestedCategories.contains("스킨케어") && requestDto.getBrandSkinCareTag() != null) {
                var tags = requestDto.getBrandSkinCareTag();
                addTagsToBrand(brand, tags.getSkinType(), TagType.BEAUTY, "스킨케어");
                addTagsToBrand(brand, tags.getMainFunction(), TagType.BEAUTY, "스킨케어");
            }

            if (requestedCategories.contains("메이크업") && requestDto.getBrandMakeUpTag() != null) {
                var tags = requestDto.getBrandMakeUpTag();
                addTagsToBrand(brand, tags.getSkinType(), TagType.BEAUTY, "메이크업");
                addTagsToBrand(brand, tags.getBrandMakeUpStyle(), TagType.BEAUTY, "메이크업");
            }

            if (requestedCategories.contains("의류") && requestDto.getBrandClothingTag() != null) {
                var tags = requestDto.getBrandClothingTag();
                addTagsToBrand(brand, tags.getBrandType(), TagType.FASHION, "의류");
                addTagsToBrand(brand, tags.getBrandStyle(), TagType.FASHION, "의류");
            }
        }
    }

    private void addTagsToBrand(Brand brand, List<String> tagNames, TagType type, String category) {
        if (tagNames == null || tagNames.isEmpty()) return;

        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByTagNameAndTagCategory(tagName, category)
                    .orElseGet(() -> tagRepository.save(Tag.builder()
                            .tagName(tagName)
                            .tagType(type.getDescription())
                            .tagCategory(category)
                            .build()));

            boolean isAlreadyLinked = brand.getBrandTags().stream()
                    .anyMatch(bt -> bt.getTag().getId().equals(tag.getId()));

            if (!isAlreadyLinked) {
                brand.addBrandTag(BrandTag.builder().tag(tag).build());
            }
        }
    }

    @Transactional
    public void deleteBrand(Long brandId) {
        Long currentUserId = getCurrentUserId();
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        brand.clearContent(currentUserId);
    }

    public List<BrandListResponseDto> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(brand -> BrandListResponseDto.builder()
                        .brandId(brand.getId())
                        .brandName(brand.getBrandName())
                        .logoUrl(brand.getLogoUrl())
                        .build())
                .collect(Collectors.toList());
    }
}
