package com.example.RealMatch.chat.application.service.room;

import java.util.Optional;

import com.example.RealMatch.chat.presentation.dto.response.ChatMatchedCampaignPayloadResponse;

public interface MatchedCampaignPayloadProvider {

    Optional<ChatMatchedCampaignPayloadResponse> getPayload(Long campaignId);
}
