package com.example.RealMatch.chat.application.service.room;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.RealMatch.brand.domain.entity.BrandAvailableSponsor;
import com.example.RealMatch.brand.domain.entity.BrandSponsorImage;
import com.example.RealMatch.brand.domain.repository.BrandAvailableSponsorRepository;
import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.campaign.domain.enums.CampaignStatus;
import com.example.RealMatch.campaign.domain.repository.CampaignRepository;
import com.example.RealMatch.chat.application.util.SystemMessagePayloadSerializer;
import com.example.RealMatch.chat.domain.entity.ChatMessage;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.domain.repository.ChatMessageRepository;
import com.example.RealMatch.chat.presentation.dto.response.CampaignSummaryResponse;
import com.example.RealMatch.chat.presentation.dto.response.CampaignSummaryResponse.CampaignSummarySponsorProductResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatMatchedCampaignPayloadResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatProposalCardPayloadResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CampaignSummaryServiceImpl implements CampaignSummaryService {

    private static final Logger LOG = LoggerFactory.getLogger(CampaignSummaryServiceImpl.class);

    private final ChatMessageRepository chatMessageRepository;
    private final CampaignRepository campaignRepository;
    private final BrandAvailableSponsorRepository brandAvailableSponsorRepository;
    private final SystemMessagePayloadSerializer payloadSerializer;

    @Override
    public CampaignSummaryResponse getCampaignSummary(Long roomId) {
        List<ChatMessage> proposalMessages = chatMessageRepository.findProposalMessagesByRoomId(roomId);

        if (proposalMessages.isEmpty()) {
            return null; // 제안이 없는 경우
        }

        // 가장 최근 제안 메시지에서 campaignId 추출
        ChatMessage latestProposalMessage = proposalMessages.get(0);
        ChatSystemMessageKind kind = latestProposalMessage.getSystemKind();
        String rawPayload = latestProposalMessage.getSystemPayload();

        if (rawPayload == null || rawPayload.isBlank()) {
            return null;
        }

        try {
            ChatSystemMessagePayload payload = payloadSerializer.deserialize(kind, rawPayload);
            Long campaignId = null;

            if (payload instanceof ChatProposalCardPayloadResponse proposalCard) {
                campaignId = proposalCard.campaignId();
            } else if (payload instanceof ChatMatchedCampaignPayloadResponse matchedCard) {
                campaignId = matchedCard.campaignId();
            }

            if (campaignId == null) {
                return null;
            }

            // 캠페인 정보 조회
            Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
            if (campaign == null || campaign.isDeleted()) {
                return null;
            }
            // 캠페인 확정 및 종료 시 상단 고정 배너 없음
            if (campaign.getStatus() == CampaignStatus.COMPLETED) {
                return null;
            }

            // 캠페인 협찬 제품 조회 (요약 바용)
            List<CampaignSummarySponsorProductResponse> sponsorProducts = brandAvailableSponsorRepository
                    .findByCampaignIdWithImages(campaignId)
                    .stream()
                    .map(CampaignSummaryServiceImpl::toSummarySponsorProduct)
                    .collect(Collectors.toList());

            return new CampaignSummaryResponse(
                    campaignId,
                    campaign.getTitle(),
                    sponsorProducts
            );
        } catch (Exception ex) {
            LOG.warn("Failed to extract campaign summary from system message. roomId={}, messageId={}", 
                    roomId, latestProposalMessage.getId(), ex);
            return null;
        }
    }

    private static CampaignSummarySponsorProductResponse toSummarySponsorProduct(BrandAvailableSponsor sponsor) {
        String thumbnailUrl = null;
        List<BrandSponsorImage> images = sponsor.getImages();
        if (images != null && !images.isEmpty()) {
            thumbnailUrl = images.get(0).getImageUrl();
        }
        return new CampaignSummarySponsorProductResponse(
                sponsor.getId(),
                sponsor.getName(),
                thumbnailUrl
        );
    }
}
