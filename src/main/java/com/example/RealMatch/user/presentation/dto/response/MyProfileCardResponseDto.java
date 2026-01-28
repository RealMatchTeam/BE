package com.example.RealMatch.user.presentation.dto.response;

import java.util.List;

import com.example.RealMatch.user.domain.entity.User;

public record MyProfileCardResponseDto(
        String nickname,
        String gender,
        int age,
        List<String> interests,
        String snsAccount,
        MatchingResult matchingResult,
        MyType myType
) {
    public static MyProfileCardResponseDto sample(User user) {
        return new MyProfileCardResponseDto(
                user.getNickname(),
                "FEMALE",
                22,
                List.of("뷰티", "패션"),
                "www.instagram.com/vivi",
                MatchingResult.sample(),
                MyType.sample()
        );
    }

    public record MatchingResult(
            String creatorType,
            String fitBrand
    ) {
        public static MatchingResult sample() {
            return new MatchingResult("OO한 크리에이터", "OO한 브랜드");
        }
    }

    public record MyType(
            BeautyType beautyType,
            FashionType fashionType,
            ContentsType contentsType
    ) {
        public static MyType sample() {
            return new MyType(
                    BeautyType.sample(),
                    FashionType.sample(),
                    ContentsType.sample()
            );
        }
    }

    public record BeautyType(
            List<String> skinType,
            String skinBrightness,
            List<String> makeupStyle
    ) {
        public static BeautyType sample() {
            return new BeautyType(
                    List.of("건성", "민감성"),
                    "17_TO_21",
                    List.of("내추럴", "글로우")
            );
        }
    }

    public record FashionType(
            int height,
            String bodyType,
            String upperSize,
            int bottomSize
    ) {
        public static FashionType sample() {
            return new FashionType(165, "WAVE", "S", 33);
        }
    }

    public record ContentsType(
            String gender,
            String age,
            String averageLength,
            String averageView
    ) {
        public static ContentsType sample() {
            return new ContentsType("여성", "20대", "1분 내외", "1,000회");
        }
    }
}
