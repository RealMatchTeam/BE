package com.example.RealMatch.user.presentation.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.UserMatchingDetail;

public record MyProfileCardResponseDto(
        String nickname,
        String profileImageUrl,
        String gender,
        int age,
        String snsAccount,
        List<String> interestFields,
        MyMatchingResultResponseDto matchingResult
) {

    public static MyProfileCardResponseDto from(User user, UserMatchingDetail detail, List<String> interestFields) {
        int age = 0;
        if (user.getBirth() != null) {
            age = LocalDate.now().getYear() - user.getBirth().getYear();
        }

        return new MyProfileCardResponseDto(
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getGender() != null ? user.getGender().name() : "",
                age,
                detail != null ? detail.getSnsUrl() : "",
                interestFields,
                detail != null ? MyMatchingResultResponseDto.from(detail) : null
        );
    }
}
