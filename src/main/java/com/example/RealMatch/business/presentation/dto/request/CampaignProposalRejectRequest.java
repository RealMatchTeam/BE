package com.example.RealMatch.business.presentation.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CampaignProposalRejectRequest {

    @Size(max = 500, message = "거절 사유는 최대 500자까지 입력할 수 있습니다.")
    private String rejectReason;
}
