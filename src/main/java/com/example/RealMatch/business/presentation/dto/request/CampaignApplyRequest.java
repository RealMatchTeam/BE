package com.example.RealMatch.business.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CampaignApplyRequest(
        @NotBlank(message = "지원 사유는 필수입니다.")
        @Size(max = 1000, message = "지원 사유는 최대 1000자까지 입력할 수 있습니다.")
        String reason
) {
}
