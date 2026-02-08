package com.example.RealMatch.business.domain.repository;

import java.util.List;

import com.example.RealMatch.business.domain.enums.CollaborationType;
import com.example.RealMatch.business.presentation.dto.response.CollaborationProjection;

public interface CampaignProposalRepositoryCustom {
    List<CollaborationProjection> findProposalCollaborations(
            List<Long> proposalIds,
            CollaborationType type
    );

    List<CollaborationProjection> searchProposalCollaborations(
            List<Long> proposalIds,
            CollaborationType type,
            List<Long> brandIds
    );

}
