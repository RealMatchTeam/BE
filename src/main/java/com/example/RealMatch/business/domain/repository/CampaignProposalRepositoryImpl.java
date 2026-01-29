package com.example.RealMatch.business.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.RealMatch.business.domain.enums.CollaborationType;
import com.example.RealMatch.business.presentation.dto.response.CollaborationResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import static com.example.RealMatch.brand.domain.entity.QBrand.brand;
import static com.example.RealMatch.business.domain.entity.QCampaignProposal.campaignProposal;
import static com.example.RealMatch.campaign.domain.entity.QCampaign.campaign;

@Repository
@RequiredArgsConstructor
public class CampaignProposalRepositoryImpl
        implements CampaignProposalRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CollaborationResponse> findProposalCollaborations(
            List<UUID> ids,
            CollaborationType type
    ) {
        return queryFactory
                .select(Projections.constructor(
                        CollaborationResponse.class,
                        campaign.id,               // Long
                        campaignProposal.id,               // UUID
                        brand.brandName,
                        brand.logoUrl,
                        campaignProposal.title,
                        campaignProposal.status,
                        campaignProposal.startDate,
                        campaignProposal.endDate,
                        Expressions.constant(type) // ⭐ 마지막
                ))
                .from(campaignProposal)
                .join(campaignProposal.brand, brand)
                .leftJoin(campaignProposal.campaign, campaign)
                .where(campaignProposal.id.in(ids))
                .fetch();
    }
}

