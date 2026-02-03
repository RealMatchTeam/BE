package com.example.RealMatch.brand.application.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.entity.BrandAvailableSponsor;
import com.example.RealMatch.brand.domain.entity.BrandCategory;
import com.example.RealMatch.brand.domain.entity.BrandCategoryView;
import com.example.RealMatch.brand.domain.entity.BrandLike;
import com.example.RealMatch.brand.domain.entity.enums.IndustryType;
import com.example.RealMatch.brand.domain.repository.BrandAvailableSponsorRepository;
import com.example.RealMatch.brand.domain.repository.BrandCategoryRepository;
import com.example.RealMatch.brand.domain.repository.BrandCategoryViewRepository;
import com.example.RealMatch.brand.domain.repository.BrandLikeRepository;
import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.brand.exception.BrandErrorCode;
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
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
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
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
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
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        }
        log.warn("Authentication principal is not of type CustomUserDetails: {}", principal.getClass().getName());
        throw new CustomException(GeneralErrorCode.UNAUTHORIZED);
    }

    public BrandDetailResponseDto getBrandDetail(Long brandId) {
        Long currentUserId = getCurrentUserId();

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new IllegalArgumentException("Brand not found with id: " + brandId));

        boolean isLiked = brandLikeRepository.existsByUserIdAndBrandId(currentUserId, brandId);

        BrandDetailResponseDto.BrandDetailResponseDtoBuilder responseBuilder = BrandDetailResponseDto.builder()
                .userId(brand.getUser().getId())
                .brandName(brand.getBrandName())
                .logoUrl(brand.getLogoUrl())
                .simpleIntro(brand.getSimpleIntro())
                .detailIntro(brand.getDetailIntro())
                .homepageUrl(brand.getHomepageUrl())
                .brandMatchingRatio(brand.getMatchingRate())
                .brandIsLiked(isLiked)
                .brandTag(Collections.emptyList());

        if (brand.getIndustryType() == IndustryType.BEAUTY) {
            List<String> brandCategories = tagRepository.findAllByTagCategory("관심 스타일").stream()
                    .map(Tag::getTagName)
                    .collect(Collectors.toList());

            List<String> skinTypes = tagRepository.findAllByTagCategory("피부 타입").stream()
                    .map(Tag::getTagName)
                    .collect(Collectors.toList());

            List<String> mainFunctions = tagRepository.findAllByTagCategory("관심 기능").stream()
                    .map(Tag::getTagName)
                    .collect(Collectors.toList());

            List<String> makeupStyles = tagRepository.findAllByTagCategory("메이크업").stream()
                    .map(Tag::getTagName)
                    .collect(Collectors.toList());

            BrandDetailResponseDto.BrandSkinCareTagDto skinCareTagDto = BrandDetailResponseDto.BrandSkinCareTagDto.builder()
                    .skinType(skinTypes)
                    .mainFunction(mainFunctions)
                    .build();

            BrandDetailResponseDto.BrandMakeUpTagDto makeUpTagDto = BrandDetailResponseDto.BrandMakeUpTagDto.builder()
                    .brandMakeUpStyle(makeupStyles)
                    .build();

            responseBuilder.brandCategory(brandCategories)
                    .brandSkinCareTag(skinCareTagDto)
                    .brandMakeUpTag(makeUpTagDto);

        } else if (brand.getIndustryType() == IndustryType.FASHION) {
            List<String> brandTypes = tagRepository.findAllByTagCategory("관심 브랜드 종류").stream()
                    .map(Tag::getTagName)
                    .collect(Collectors.toList());

            List<String> brandStyles = tagRepository.findAllByTagCategory("관심 스타일").stream()
                    .map(Tag::getTagName)
                    .collect(Collectors.toList());

            BrandDetailResponseDto.BrandClothingTagDto clothingTagDto = BrandDetailResponseDto.BrandClothingTagDto.builder()
                    .brandType(brandTypes)
                    .brandStyle(brandStyles)
                    .build();

            responseBuilder.brandClothingTag(clothingTagDto);
        }

        return responseBuilder.build();
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
            throw new CustomException(BrandErrorCode.BRAND_ALREADY_EXISTS, "이미 해당 유저(ID: " + user.getId() + ")의 브랜드(ID: " + existingBrand.get().getId() + ")가 존재합니다.");
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
                addTagsToBrand(brand, skinCareTags.getSkinType(), TagType.BEAUTY, "스킨케어_피부타입");
            }
            if (skinCareTags.getMainFunction() != null) {
                addTagsToBrand(brand, skinCareTags.getMainFunction(), TagType.BEAUTY, "스킨케어_주요기능");
            }
        }
        if (requestDto.getBrandMakeUpTag() != null) {
            BrandCreateRequestDto.BrandMakeUpTagDto makeUpTags = requestDto.getBrandMakeUpTag();
            if (makeUpTags.getBrandMakeUpStyle() != null) {
                addTagsToBrand(brand, makeUpTags.getBrandMakeUpStyle(), TagType.BEAUTY, "메이크업_스타일");
            }
        }
        if (requestDto.getBrandClothingTag() != null) {
            BrandCreateRequestDto.BrandClothingTagDto clothingTags = requestDto.getBrandClothingTag();
            if (clothingTags.getBrandType() != null) {
                addTagsToBrand(brand, clothingTags.getBrandType(), TagType.FASHION, "의류_종류");
            }
            if (clothingTags.getBrandStyle() != null) {
                addTagsToBrand(brand, clothingTags.getBrandStyle(), TagType.FASHION, "의류_스타일");
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

        updateCategories(brand, requestDto.getBrandCategory());
        updateTags(brand, requestDto);
    }

    private void updateCategories(Brand brand, List<String> requestedCategoryNames) {
        if (requestedCategoryNames == null) {
            requestedCategoryNames = new ArrayList<>();
        }

        Set<String> existingCategoryNames = brand.getBrandCategoryViews().stream()
                .map(bcv -> bcv.getCategory().getName())
                .collect(Collectors.toSet());

        Set<String> requestedCategoryNamesSet = Set.copyOf(requestedCategoryNames);

        brand.getBrandCategoryViews().removeIf(bcv -> !requestedCategoryNamesSet.contains(bcv.getCategory().getName()));

        requestedCategoryNamesSet.stream()
                .filter(name -> !existingCategoryNames.contains(name))
                .forEach(name -> {
                    BrandCategory category = brandCategoryRepository.findByName(name)
                            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + name));
                    brand.addBrandCategoryView(BrandCategoryView.builder().category(category).build());
                });
    }

    private void updateTags(Brand brand, BrandUpdateRequestDto requestDto) {
        brand.getBrandTags().clear();

        if (requestDto.getBrandSkinCareTag() != null) {
            BrandUpdateRequestDto.BrandSkinCareTagDto skinCareTags = requestDto.getBrandSkinCareTag();
            if (skinCareTags.getSkinType() != null) {
                addTagsToBrand(brand, skinCareTags.getSkinType(), TagType.BEAUTY, "스킨케어_피부타입");
            }
            if (skinCareTags.getMainFunction() != null) {
                addTagsToBrand(brand, skinCareTags.getMainFunction(), TagType.BEAUTY, "스킨케어_주요기능");
            }
        }
        if (requestDto.getBrandMakeUpTag() != null) {
            BrandUpdateRequestDto.BrandMakeUpTagDto makeUpTags = requestDto.getBrandMakeUpTag();
            if (makeUpTags.getBrandMakeUpStyle() != null) {
                addTagsToBrand(brand, makeUpTags.getBrandMakeUpStyle(), TagType.BEAUTY, "메이크업_스타일");
            }
        }
        if (requestDto.getBrandClothingTag() != null) {
            BrandUpdateRequestDto.BrandClothingTagDto clothingTags = requestDto.getBrandClothingTag();
            if (clothingTags.getBrandType() != null) {
                addTagsToBrand(brand, clothingTags.getBrandType(), TagType.FASHION, "의류_종류");
            }
            if (clothingTags.getBrandStyle() != null) {
                addTagsToBrand(brand, clothingTags.getBrandStyle(), TagType.FASHION, "의류_스타일");
            }
        }
    }


    private void addTagsToBrand(Brand brand, List<String> tagNames, TagType type, String category) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }

        Map<String, Tag> existingTags = tagRepository.findAllByTagNameInAndTagCategory(tagNames, category).stream()
                .collect(Collectors.toMap(Tag::getTagName, Function.identity()));

        List<Tag> newTags = tagNames.stream()
                .filter(name -> !existingTags.containsKey(name))
                .map(name -> Tag.builder()
                        .tagName(name)
                        .tagType(type.getDescription())
                        .tagCategory(category)
                        .build())
                .collect(Collectors.toList());

        if (!newTags.isEmpty()) {
            tagRepository.saveAll(newTags);
        }

        List<Tag> allTags = new ArrayList<>(existingTags.values());
        allTags.addAll(newTags);

        for (Tag tag : allTags) {
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
        brand.softDelete(currentUserId);
    }

    public Page<BrandListResponseDto> getAllBrands(Pageable pageable) {
        return brandRepository.findAll(pageable)
                .map(BrandListResponseDto::from);
    }

    public Long getBrandIdByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Brand brand = brandRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found for user: " + userId));
        return brand.getId();
    }
}
