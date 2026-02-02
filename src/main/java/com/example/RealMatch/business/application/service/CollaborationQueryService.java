package com.example.RealMatch.business.application.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.RealMatch.business.domain.entity.CampaignApply;
import com.example.RealMatch.business.domain.enums.CollaborationType;
import com.example.RealMatch.business.domain.enums.ProposalStatus;
import com.example.RealMatch.business.domain.repository.CampaignApplyRepository;
import com.example.RealMatch.business.domain.repository.CampaignProposalRepository;
import com.example.RealMatch.business.presentation.dto.response.CollaborationResponse;
import com.example.RealMatch.user.domain.entity.enums.Role;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CollaborationQueryService {

    private final CampaignApplyRepository campaignApplyRepository;
    private final CampaignProposalRepository campaignProposalRepository;

    public List<CollaborationResponse> getMyCollaborations(
            Long userId,
            Role role,
            CollaborationType type,
            ProposalStatus status,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<CollaborationResponse> result = new ArrayList<>();
        getReceivedProposal(userId, type, status, result);
        getAppliedCampaign(userId, type, status, startDate, endDate, result);
        getSentProposal(userId, type, status, result);

        return result;

    }

    private void getAppliedCampaign(Long userId, CollaborationType type, ProposalStatus status,
                                    LocalDate startDate, LocalDate endDate, List<CollaborationResponse> result) {
        if (type == null || type == CollaborationType.APPLIED) {
            List<CampaignApply> applies =
                    campaignApplyRepository.findMyApplies(userId, status, startDate, endDate);

            for (CampaignApply apply : applies) {
                result.add(CollaborationResponse.fromApply(apply));
            }
        }
    }

    private void getReceivedProposal(Long userId, CollaborationType type, ProposalStatus status, List<CollaborationResponse> result) {
        // 2️⃣ 내가 받은 제안
        if (type == null || type == CollaborationType.RECEIVED) {
            List<Long> receivedIds =
                    campaignProposalRepository.findReceivedProposalIds(userId, status);

            if (!receivedIds.isEmpty()) {
                result.addAll(
                        campaignProposalRepository.findProposalCollaborations(
                                receivedIds, CollaborationType.RECEIVED
                        )
                );
            }
        }
    }

    private void getSentProposal(Long userId, CollaborationType type, ProposalStatus status, List<CollaborationResponse> result) {
        // 3️⃣ 내가 보낸 제안
        if (type == null || type == CollaborationType.SENT) {
            List<Long> sentIds =
                    campaignProposalRepository.findSentProposalIds(userId, status);

            if (!sentIds.isEmpty()) {
                result.addAll(
                        campaignProposalRepository.findProposalCollaborations(
                                sentIds, CollaborationType.SENT
                        )
                );
            }
        }
    }

}
