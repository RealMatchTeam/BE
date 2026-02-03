package com.example.RealMatch.chat.application.service.room;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.campaign.domain.repository.CampaignRepository;
import com.example.RealMatch.chat.presentation.dto.response.ChatMatchedCampaignPayloadResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchedCampaignPayloadProviderImpl implements MatchedCampaignPayloadProvider {

    private static final String DEFAULT_CURRENCY = "KRW";

    private final CampaignRepository campaignRepository;

    @Override
    public Optional<ChatMatchedCampaignPayloadResponse> getPayload(Long campaignId) {
        if (campaignId == null) {
            return Optional.empty();
        }
        return campaignRepository.findById(campaignId)
                .filter(c -> !c.isDeleted())
                .map(this::toPayload);
    }

    private ChatMatchedCampaignPayloadResponse toPayload(Campaign campaign) {
        // TODO: orderNumber - 결제/주문 도메인에서 주문 번호 확정 시 채팅으로 전달하거나,
        //       ProposalStatusChangedEvent(또는 매칭 완료 이벤트)에 orderNumber 포함 후 여기서 사용
        return new ChatMatchedCampaignPayloadResponse(
                campaign.getId(),
                campaign.getTitle(),
                campaign.getRewardAmount() != null ? campaign.getRewardAmount() : 0L,
                DEFAULT_CURRENCY,
                "",
                null
        );
    }
}
