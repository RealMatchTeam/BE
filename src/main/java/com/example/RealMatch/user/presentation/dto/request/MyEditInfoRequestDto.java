package com.example.RealMatch.user.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MyEditInfoRequestDto(
        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname,

        @NotBlank(message = "주소는 필수입니다.")
        String address,

        @NotBlank(message = "상세 주소는 필수입니다.")
        String detailAddress
) {
}
