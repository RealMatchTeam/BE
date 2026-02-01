package com.example.RealMatch.business.domain.repository;

import java.util.List;

import com.example.RealMatch.business.domain.enums.CollaborationType;
import com.example.RealMatch.business.presentation.dto.response.CollaborationResponse;

public interface CampaignProposalRepositoryCustom {
    List<CollaborationResponse> findProposalCollaborations(
            List<Long> ids,
            CollaborationType type
    );
}
