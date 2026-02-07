package com.example.RealMatch.user.presentation.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.UserContentCategory;
import com.example.RealMatch.user.domain.entity.UserMatchingDetail;

public record MyProfileCardResponseDto(
        String nickname,
        String profileImageUrl,
        String gender,
        int age,
        String snsAccount,
        List<UserContentCategoryInfo> contentCategories,
        MyMatchingResultResponseDto matchingResult
) {

    public record UserContentCategoryInfo(
            Long userContentCategoryId
    ) {
        public static UserContentCategoryInfo from(UserContentCategory ucc) {
            return new UserContentCategoryInfo(ucc.getId());
        }
    }

    public static MyProfileCardResponseDto from(
            User user,
            UserMatchingDetail detail,
            List<UserContentCategory> categories
    ) {
        int age = 0;
        if (user.getBirth() != null) {
            age = LocalDate.now().getYear() - user.getBirth().getYear();
        }

        List<UserContentCategoryInfo> categoryInfos =
                categories == null ? List.of()
                        : categories.stream()
                        .map(UserContentCategoryInfo::from)
                        .toList();

        return new MyProfileCardResponseDto(
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getGender() != null ? user.getGender().name() : "",
                age,
                detail != null ? detail.getSnsUrl() : "",
                categoryInfos,
                detail != null ? MyMatchingResultResponseDto.from(detail) : null
        );
    }
}
