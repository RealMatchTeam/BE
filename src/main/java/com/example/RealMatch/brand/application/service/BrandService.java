package com.example.RealMatch.brand.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.entity.BrandAvailableSponsor;
import com.example.RealMatch.brand.domain.entity.BrandDescribeTag;
import com.example.RealMatch.brand.domain.entity.BrandLike;
import com.example.RealMatch.brand.domain.entity.enums.IndustryType;
import com.example.RealMatch.brand.domain.repository.BrandAvailableSponsorRepository;
import com.example.RealMatch.brand.domain.repository.BrandCategoryRepository;
import com.example.RealMatch.brand.domain.repository.BrandCategoryViewRepository;
import com.example.RealMatch.brand.domain.repository.BrandDescribeTagRepository;
import com.example.RealMatch.brand.domain.repository.BrandLikeRepository;
import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.brand.exception.BrandErrorCode;
import com.example.RealMatch.brand.presentation.dto.request.BrandBeautyCreateRequestDto;
import com.example.RealMatch.brand.presentation.dto.request.BrandBeautyUpdateRequestDto;
import com.example.RealMatch.brand.presentation.dto.request.BrandFashionCreateRequestDto;
import com.example.RealMatch.brand.presentation.dto.request.BrandFashionUpdateRequestDto;
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
import com.example.RealMatch.match.domain.repository.MatchBrandHistoryRepository;
import com.example.RealMatch.tag.domain.entity.BrandTag;
import com.example.RealMatch.tag.domain.entity.Tag;
import com.example.RealMatch.tag.domain.enums.TagCategory;
import com.example.RealMatch.tag.domain.enums.TagType;
import com.example.RealMatch.tag.domain.repository.TagBrandRepository;
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
    private final BrandLikeRepository brandLikeRepository;
    private final BrandCategoryViewRepository brandCategoryViewRepository;
    private final BrandCategoryRepository brandCategoryRepository;
    private final BrandAvailableSponsorRepository brandAvailableSponsorRepository;
    private final BrandDescribeTagRepository brandDescribeTagRepository;

    private final MatchBrandHistoryRepository matchBrandHistoryRepository;

    private final TagBrandRepository tagBrandRepository;
    private final TagRepository tagRepository;

    private final UserRepository userRepository;

    private static final Pattern URL_PATTERN = Pattern.compile("^https?://([\\da-z.-]+)\\.([a-z.]{2,6})[/\\w .-]*/?$");

    // ******** //
    // 브랜드 조회 //
    // ******** //
    public BrandDetailResponseDto getBrandDetail(Long brandId, Long currentUserId) {

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new IllegalArgumentException("Brand not found"));

        boolean isLiked = brandLikeRepository.existsByUserIdAndBrandId(currentUserId, brandId);

        // 사용자 맞춤 브랜드 매칭률 조회
        Long brandMatchingRatio = matchBrandHistoryRepository.findByUserIdAndBrandId(currentUserId, brandId)
                .map(history -> history.getMatchingRatio())
                .orElse(0L);

        List<String> brandDescriptionTags = brandDescribeTagRepository.findAllByBrandId(brandId)
                .stream()
                .map(BrandDescribeTag::getBrandDescribeTag)
                .collect(Collectors.toList());

        // 공통 메서드 응답 빌드
        BrandDetailResponseDto.BrandDetailResponseDtoBuilder responseBuilder = BrandDetailResponseDto.builder()
                .userId(currentUserId)
                .brandName(brand.getBrandName())
                .logoUrl(brand.getLogoUrl())
                .simpleIntro(brand.getSimpleIntro())
                .detailIntro(brand.getDetailIntro())
                .homepageUrl(brand.getHomepageUrl())
                .brandMatchingRatio(brandMatchingRatio.intValue())
                .brandIsLiked(isLiked)
                .brandDescriptionTags(brandDescriptionTags);

        List<String> brandCategories;

        // **** 브랜드가 뷰티 카테고리인 경우 **** //
        if (brand.getIndustryType() == IndustryType.BEAUTY) {

            // 매칭: 관심 스타일   <-> 카테고리
            // 매칭: 피부 타입     <-> 스킨케어 태그: 피부 타입
            // 매칭: 관심 기능     <-> 스킨케어 태그: 주요 기능
            // 매칭: 메이크업 스타일 <-> 메이크업 태그: 메이크업 스타일 

            brandCategories = tagBrandRepository.findTagNamesByBrandIdAndTagCategory(                   // 매칭: 관심 스타일
                                                        brandId, TagCategory.BEAUTY_INTEREST_STYLE.getDescription());

            // 코드 가독성을 위해 각각 분리함. -> 성능적으로는 좋지는 않음.
            List<String> brandSkinType = tagBrandRepository.findTagNamesByBrandIdAndTagCategory(        // 매칭: 피부 타입
                                                        brandId, TagCategory.BEAUTY_SKIN_TYPE.getDescription());

            List<String> brandMainFunction = tagBrandRepository.findTagNamesByBrandIdAndTagCategory(    // 매칭: 관심 기능
                                                        brandId, TagCategory.BEAUTY_INTEREST_FUNCTION.getDescription());

            List<String> brandMakeUpStyle = tagBrandRepository.findTagNamesByBrandIdAndTagCategory(     // 매칭: 메이크업 스타일
                                                        brandId, TagCategory.BEAUTY_MAKEUP_STYLE.getDescription());

            responseBuilder.beautyResponse(BrandDetailResponseDto.BrandBeautyResponse.builder()
                    .categories(brandCategories)        // 뷰티: 카테고리
                    .skinType(brandSkinType)            // 스킨케어 태그: 피부타입
                    .mainFunction(brandMainFunction)    // 스킨케어 태그: 주요 기능
                    .makeUpStyle(brandMakeUpStyle)      // 메이크업 태그: 메이크업 스타일
                    .build());

        // **** 브랜드가 패션 카테고리인 경우 **** //
        } else if (brand.getIndustryType() == IndustryType.FASHION) {

            // 매칭: 관심 아이템/분야 <-> 카테고리
            // 매칭: 관심 브랜드 종류 <-> 의류 태그: 브랜드 종류
            // 매칭: 관심 스타일     <-> 의류 태그: 브랜드 스타일

            brandCategories = tagBrandRepository.findTagNamesByBrandIdAndTagCategory(           // 매칭: 관심 아이템/분야
                                                        brandId, TagCategory.FASHION_INTEREST_ITEM.getDescription());

            List<String> brandType = tagBrandRepository.findTagNamesByBrandIdAndTagCategory(    // 매칭: 관심 브랜드 종류
                                                        brandId, TagCategory.FASHION_INTEREST_TYPE.getDescription());

            List<String> brandStyle = tagBrandRepository.findTagNamesByBrandIdAndTagCategory(   // 매칭: 관심 스타일
                                                        brandId, TagCategory.FASHION_INTEREST_STYLE.getDescription());

            responseBuilder.fashionResponse(BrandDetailResponseDto.BrandFashionResponse.builder()
                    .categories(brandCategories)        // 패션: 카테고리
                    .brandType(brandType)               // 의류 태그: 브랜드 종류
                    .brandStyle(brandStyle)             // 의류 태그: 브랜드 스타일
                    .build());
        }

        return responseBuilder.build();
    }

    @Transactional
    public Boolean likeBrand(Long brandId, Long currentUserId) {

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

    // ******** //
    // 브랜드 생성 //
    // ******** //
    
    // 브랜드가 뷰티인지, 패션인지에 따라 저장하는 형태가 달라짐.

    @Transactional
    public BrandCreateResponseDto createBeautyBrand(BrandBeautyCreateRequestDto requestDto, Long currentUserId) {

        validateHomepageUrl(requestDto.getHomepageUrl());

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        // 이미 브랜드가 존재하는지 확인
        Optional<Brand> existingBrand = brandRepository.findByUser(user);
        if (existingBrand.isPresent()) {
            throw new CustomException(BrandErrorCode.BRAND_ALREADY_EXISTS,
                "이미 해당 유저(ID: " + user.getId() + ")의 브랜드(ID: " + existingBrand.get().getId() + ")가 존재합니다.");
        }

        // 브랜드 생성
        Brand brand = Brand.builder()
                .brandName(requestDto.getBrandName())
                .industryType(IndustryType.BEAUTY)
                .logoUrl(requestDto.getLogoUrl())
                .simpleIntro(requestDto.getSimpleIntro())
                .detailIntro(requestDto.getDetailIntro())
                .homepageUrl(requestDto.getHomepageUrl())
                .createdBy(currentUserId)
                .user(user)
                .build();

        // 브랜드 먼저 저장
        Brand savedBrand = brandRepository.save(brand);

        // *** 브랜드 매칭용/상세 페이지용 뷰티 태그 추가 *** //
        if (requestDto.getBrandTags() != null) {
            BrandBeautyCreateRequestDto.BrandTagsDto brandTags = requestDto.getBrandTags();

            // 관심 스타일 태그 추가
            addTagsToBrandByIds(savedBrand, brandTags.getInterestStyle(), TagCategory.BEAUTY_INTEREST_STYLE.getDescription());

            // 관심 기능 태그 추가
            addTagsToBrandByIds(savedBrand, brandTags.getInterestFunction(), TagCategory.BEAUTY_INTEREST_FUNCTION.getDescription());

            // 피부 타입 태그 추가
            addTagsToBrandByIds(savedBrand, brandTags.getSkinType(), TagCategory.BEAUTY_SKIN_TYPE.getDescription());

            // 메이크업 스타일 태그 추가
            addTagsToBrandByIds(savedBrand, brandTags.getMakeupStyle(), TagCategory.BEAUTY_MAKEUP_STYLE.getDescription());
        }

        return BrandCreateResponseDto.builder()
                .brandId(savedBrand.getId())
                .build();
    }

    @Transactional
    public BrandCreateResponseDto createFashionBrand(BrandFashionCreateRequestDto requestDto, Long currentUserId) {

        validateHomepageUrl(requestDto.getHomepageUrl());

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        // 이미 브랜드가 존재하는지 확인
        Optional<Brand> existingBrand = brandRepository.findByUser(user);
        if (existingBrand.isPresent()) {
            throw new CustomException(BrandErrorCode.BRAND_ALREADY_EXISTS,
                "이미 해당 유저(ID: " + user.getId() + ")의 브랜드(ID: " + existingBrand.get().getId() + ")가 존재합니다.");
        }

        // 브랜드 생성
        Brand brand = Brand.builder()
                .brandName(requestDto.getBrandName())
                .industryType(IndustryType.FASHION)
                .logoUrl(requestDto.getLogoUrl())
                .simpleIntro(requestDto.getSimpleIntro())
                .detailIntro(requestDto.getDetailIntro())
                .homepageUrl(requestDto.getHomepageUrl())
                .createdBy(currentUserId)
                .user(user)
                .build();

        // 브랜드 먼저 저장
        Brand savedBrand = brandRepository.save(brand);

        // *** 브랜드 매칭용/상세 페이지용 패션 태그 추가 *** //
        if (requestDto.getBrandTags() != null) {
            BrandFashionCreateRequestDto.BrandTagsDto brandTags = requestDto.getBrandTags();

            // 관심 스타일 태그 추가
            addTagsToBrandByIds(savedBrand, brandTags.getInterestStyle(), TagCategory.FASHION_INTEREST_STYLE.getDescription());

            // 관심 아이템/분야 태그 추가
            addTagsToBrandByIds(savedBrand, brandTags.getInterestItem(), TagCategory.FASHION_INTEREST_ITEM.getDescription());

            // 관심 브랜드 종류 태그 추가
            addTagsToBrandByIds(savedBrand, brandTags.getInterestBrand(), TagCategory.FASHION_INTEREST_TYPE.getDescription());
        }

        return BrandCreateResponseDto.builder()
                .brandId(savedBrand.getId())
                .build();
    }

    // *********** //
    // 브랜드 업데이트 //
    // *********** //
    @Transactional
    public void updateBeautyBrand(Long brandId, BrandBeautyUpdateRequestDto requestDto, Long currentUserId) {

        validateHomepageUrl(requestDto.getHomepageUrl());

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + brandId));

        if (!brand.getUser().getId().equals(currentUserId)) {
            throw new CustomException(GeneralErrorCode.FORBIDDEN);
        }

        // 브랜드가 뷰티 타입인지 확인
        if (brand.getIndustryType() != IndustryType.BEAUTY) {
            throw new CustomException(BrandErrorCode.INVALID_INDUSTRY_TYPE, "해당 브랜드는 뷰티 브랜드가 아닙니다.");
        }

        brand.update(
                requestDto.getBrandName(),
                requestDto.getLogoUrl(),
                requestDto.getSimpleIntro(),
                requestDto.getDetailIntro(),
                requestDto.getHomepageUrl(),
                currentUserId
        );

        // 기존 태그 삭제
        brand.getBrandTags().clear();

        // *** 브랜드 매칭용/상세 페이지용 뷰티 태그 추가 *** //
        if (requestDto.getBrandTags() != null) {
            BrandBeautyUpdateRequestDto.BrandTagsDto brandTags = requestDto.getBrandTags();

            // 관심 스타일 태그 추가
            addTagsToBrandByIds(brand, brandTags.getInterestStyle(), TagCategory.BEAUTY_INTEREST_STYLE.getDescription());

            // 관심 기능 태그 추가
            addTagsToBrandByIds(brand, brandTags.getInterestFunction(), TagCategory.BEAUTY_INTEREST_FUNCTION.getDescription());

            // 피부 타입 태그 추가
            addTagsToBrandByIds(brand, brandTags.getSkinType(), TagCategory.BEAUTY_SKIN_TYPE.getDescription());

            // 메이크업 스타일 태그 추가
            addTagsToBrandByIds(brand, brandTags.getMakeupStyle(), TagCategory.BEAUTY_MAKEUP_STYLE.getDescription());
        }
    }

    @Transactional
    public void updateFashionBrand(Long brandId, BrandFashionUpdateRequestDto requestDto, Long currentUserId) {

        validateHomepageUrl(requestDto.getHomepageUrl());

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + brandId));

        if (!brand.getUser().getId().equals(currentUserId)) {
            throw new CustomException(GeneralErrorCode.FORBIDDEN);
        }

        // 브랜드가 패션 타입인지 확인
        if (brand.getIndustryType() != IndustryType.FASHION) {
            throw new CustomException(BrandErrorCode.INVALID_INDUSTRY_TYPE, "해당 브랜드는 패션 브랜드가 아닙니다.");
        }

        brand.update(
                requestDto.getBrandName(),
                requestDto.getLogoUrl(),
                requestDto.getSimpleIntro(),
                requestDto.getDetailIntro(),
                requestDto.getHomepageUrl(),
                currentUserId
        );

        // 기존 태그 삭제
        brand.getBrandTags().clear();

        // *** 브랜드 매칭용/상세 페이지용 패션 태그 추가 *** //
        if (requestDto.getBrandTags() != null) {
            BrandFashionUpdateRequestDto.BrandTagsDto brandTags = requestDto.getBrandTags();

            // 관심 스타일 태그 추가
            addTagsToBrandByIds(brand, brandTags.getInterestStyle(), TagCategory.FASHION_INTEREST_STYLE.getDescription());

            // 관심 아이템/분야 태그 추가
            addTagsToBrandByIds(brand, brandTags.getInterestItem(), TagCategory.FASHION_INTEREST_ITEM.getDescription());

            // 관심 브랜드 종류 태그 추가
            addTagsToBrandByIds(brand, brandTags.getInterestBrand(), TagCategory.FASHION_INTEREST_TYPE.getDescription());
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

    private void addTagsToBrandByIds(Brand brand, List<Integer> tagIds, String category) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        // Integer를 Long으로 변환하여 태그 조회
        List<Long> tagIdsLong = tagIds.stream()
                .map(Integer::longValue)
                .collect(Collectors.toList());

        List<Tag> tags = tagRepository.findAllById(tagIdsLong);

        for (Tag tag : tags) {
            // 해당 카테고리의 태그인지 확인
            if (category.equals(tag.getTagCategory())) {
                boolean isAlreadyLinked = brand.getBrandTags().stream()
                        .anyMatch(bt -> bt.getTag().getId().equals(tag.getId()));
                if (!isAlreadyLinked) {
                    brand.addBrandTag(BrandTag.builder().tag(tag).build());
                }
            }
        }
    }

    // ******** //
    // 브랜드 삭제 //
    // ******** //
    @Transactional
    public void deleteBrand(Long brandId, Long currentUserId) {
        
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

    private void validateHomepageUrl(String url) {
        if (url != null && !url.isEmpty() && !URL_PATTERN.matcher(url).matches()) {
            throw new CustomException(BrandErrorCode.INVALID_URL_FORMAT);
        }
    }
}
