package com.example.RealMatch.business.application.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.RealMatch.attachment.application.service.AttachmentUrlService;
import com.example.RealMatch.business.domain.enums.CollaborationType;
import com.example.RealMatch.business.domain.enums.ProposalStatus;
import com.example.RealMatch.business.domain.repository.CampaignApplyRepository;
import com.example.RealMatch.business.domain.repository.CampaignProposalRepository;
import com.example.RealMatch.business.presentation.dto.response.CollaborationProjection;
import com.example.RealMatch.business.presentation.dto.response.CollaborationResponse;
import com.example.RealMatch.user.domain.entity.enums.Role;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CollaborationQueryService {

    private final CampaignApplyRepository campaignApplyRepository;
    private final CampaignProposalRepository campaignProposalRepository;
    private final AttachmentUrlService attachmentUrlService;

    public List<CollaborationResponse> getMyCollaborations(
            Long userId,
            Role role,
            CollaborationType type,
            ProposalStatus status,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<CollaborationResponse> result = new ArrayList<>();
        getReceivedProposal(userId, type, status, startDate, endDate, result);
        getAppliedCampaign(userId, type, status, startDate, endDate, result);
        getSentProposal(userId, type, status, startDate, endDate, result);
        return result;

    }


    private void getAppliedCampaign(Long userId, CollaborationType type, ProposalStatus status,
                                    LocalDate startDate, LocalDate endDate, List<CollaborationResponse> result) {
        if (type == null || type == CollaborationType.APPLIED) {

            List<CollaborationProjection> projections =
                    campaignApplyRepository.findMyAppliedCollaborations(
                            userId, status, startDate, endDate
                    );

            result.addAll(
                    projections.stream()
                            .map(p -> new CollaborationResponse(
                                    p.campaignId(),
                                    null,
                                    p.brandName(),
//                                    attachmentUrlService.getAccessUrl(p.thumbnailS3Key()),
                                    p.thumbnailS3Key(),
                                    p.title(),
                                    p.status(),
                                    p.startDate(),
                                    p.endDate(),
                                    CollaborationType.APPLIED
                            ))
                            .toList()
            );
        }
    }

    private void getReceivedProposal(Long userId, CollaborationType type, ProposalStatus status,
                                     LocalDate startDate, LocalDate endDate, List<CollaborationResponse> result) {
        // 2️⃣ 내가 받은 제안
        if (type == null || type == CollaborationType.RECEIVED) {
            List<Long> receivedIds =
                    campaignProposalRepository.findReceivedProposalIds(userId, status, startDate, endDate);

            if (!receivedIds.isEmpty()) {
                List<CollaborationProjection> projections =
                        campaignProposalRepository.findProposalCollaborations(
                                receivedIds,
                                CollaborationType.RECEIVED
                        );

                result.addAll(
                        projections.stream()
                                .map(p -> new CollaborationResponse(
                                        p.campaignId(),
                                        p.proposalId(),
                                        p.brandName(),
//                                        attachmentUrlService.getAccessUrl(p.thumbnailS3Key()),
                                        p.thumbnailS3Key(),
                                        p.title(),
                                        p.status(),
                                        p.startDate(),
                                        p.endDate(),
                                        CollaborationType.RECEIVED
                                ))
                                .toList()
                );
            }
        }
    }

    private void getSentProposal(Long userId, CollaborationType type, ProposalStatus status,
                                 LocalDate startDate, LocalDate endDate, List<CollaborationResponse> result) {
        // 3️⃣ 내가 보낸 제안
        if (type == null || type == CollaborationType.SENT) {
            List<Long> sentIds =
                    campaignProposalRepository.findSentProposalIds(userId, status, startDate, endDate);

            if (!sentIds.isEmpty()) {
                List<CollaborationProjection> projections =
                        campaignProposalRepository.findProposalCollaborations(
                                sentIds,
                                CollaborationType.SENT
                        );

                result.addAll(
                        projections.stream()
                                .map(p -> new CollaborationResponse(
                                        p.campaignId(),
                                        p.proposalId(),
                                        p.brandName(),
//                                        attachmentUrlService.getAccessUrl(p.thumbnailS3Key()),
                                        p.thumbnailS3Key(),
                                        p.title(),
                                        p.status(),
                                        p.startDate(),
                                        p.endDate(),
                                        CollaborationType.SENT
                                ))
                                .toList()
                );
            }
        }
    }

}
