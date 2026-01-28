package com.example.RealMatch.chat.application.service.room;

import com.example.RealMatch.chat.presentation.dto.response.CampaignSummaryResponse;

public interface CampaignSummaryService {

    CampaignSummaryResponse getCampaignSummary(Long roomId);
}
