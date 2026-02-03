package com.example.RealMatch.brand.presentation.dto.request;

import java.util.List;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.entity.enums.IndustryType;
import com.example.RealMatch.user.domain.entity.User;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BrandCreateRequestDto {
    // 기본 정보
    private String brandName;
    private IndustryType industryType;
    private String logoUrl;
    private String simpleIntro;
    private String detailIntro;
    private String homepageUrl;

    // 카테고리
    private List<String> brandCategory;

    // 조건부 태그 객체들
    private BrandSkinCareTagDto brandSkinCareTag;
    private BrandMakeUpTagDto brandMakeUpTag;
    private BrandClothingTagDto brandClothingTag;


    public Brand toEntity(User user) {
        return Brand.builder()
                .brandName(this.brandName)
                .industryType(this.industryType)
                .logoUrl(this.logoUrl)
                .simpleIntro(this.simpleIntro)
                .detailIntro(this.detailIntro)
                .homepageUrl(this.homepageUrl)
                .matchingRate(0) // 기본값 설정
                .createdBy(user.getId())
                .user(user)
                .build();
    }

    // --- 중첩 클래스 정의 ---
    @Getter
    @NoArgsConstructor
    public static class BrandSkinCareTagDto {
        private List<String> skinType;
        private List<String> mainFunction;
    }

    @Getter
    @NoArgsConstructor
    public static class BrandMakeUpTagDto {
        private List<String> skinType;
        private List<String> brandMakeUpStyle;
    }

    @Getter
    @NoArgsConstructor
    public static class BrandClothingTagDto {
        private List<String> brandType;
        private List<String> brandStyle;
    }
}
