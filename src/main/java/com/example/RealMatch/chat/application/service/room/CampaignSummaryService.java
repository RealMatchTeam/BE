package com.example.RealMatch.chat.application.service.room;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.brand.domain.entity.BrandAvailableSponsor;
import com.example.RealMatch.brand.domain.entity.BrandSponsorImage;
import com.example.RealMatch.brand.domain.repository.BrandAvailableSponsorRepository;
import com.example.RealMatch.business.domain.repository.CampaignProposalRepository;
import com.example.RealMatch.chat.application.dto.response.CampaignSummaryResponse;
import com.example.RealMatch.chat.application.dto.response.CampaignSummaryResponse.CampaignSummarySponsorProductResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CampaignSummaryService {

    private final CampaignProposalRepository proposals;
    private final BrandAvailableSponsorRepository brandAvailableSponsorRepository;

    @Transactional(readOnly = true)
    public CampaignSummaryResponse getCampaignSummary(Long roomId) {
        return proposals.findActiveForRoom(roomId, PageRequest.of(0, 1)).stream()
                .findFirst().map(proposal -> {
                    var campaign = proposal.getCampaign();
                    var products = brandAvailableSponsorRepository.findById(proposal.getProductId())
                            .map(CampaignSummaryService::toSummarySponsorProduct).stream().toList();
                    return new CampaignSummaryResponse(campaign.getId(), campaign.getTitle(), products);
                }).orElse(null);
    }

    private static CampaignSummarySponsorProductResponse toSummarySponsorProduct(BrandAvailableSponsor sponsor) {
        String thumbnailUrl = null;
        List<BrandSponsorImage> images = sponsor.getImages();
        if (!images.isEmpty()) {
            thumbnailUrl = images.get(0).getImageUrl();
        }
        return new CampaignSummarySponsorProductResponse(
                sponsor.getId(),
                sponsor.getName(),
                thumbnailUrl
        );
    }
}
