package com.example.RealMatch.user.presentation.dto.response;

import com.example.RealMatch.user.domain.entity.UserMatchingDetail;

public record MyMatchingResultResponseDto(
        String creatorType
) {
    public static MyMatchingResultResponseDto from(UserMatchingDetail detail) {
        return new MyMatchingResultResponseDto(detail.getCreatorType());
    }
}
