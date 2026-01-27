package com.example.RealMatch.user.presentation.dto.response;

import com.example.RealMatch.user.domain.entity.User;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageResponseDto {

    private String nickname;
    private String name;
    private String email;
    private String profileImageUrl;
    private boolean hasMatchingTest; // 매칭 검사 여부

    // Entity -> DTO 변환 메서드 (매칭 검사 여부는 외부에서 주입)
    public static MyPageResponseDto from(User user, boolean hasMatchingTest) {
        return MyPageResponseDto.builder()
                .nickname(user.getNickname())
                .name(user.getName())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .hasMatchingTest(hasMatchingTest)
                .build();
    }
}
