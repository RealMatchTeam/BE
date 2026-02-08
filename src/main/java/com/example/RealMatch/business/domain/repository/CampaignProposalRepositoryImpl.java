package com.example.RealMatch.business.domain.repository;

import static com.example.RealMatch.brand.domain.entity.QBrand.brand;
import static com.example.RealMatch.business.domain.entity.QCampaignProposal.campaignProposal;
import static com.example.RealMatch.campaign.domain.entity.QCampaign.campaign;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.RealMatch.business.domain.enums.CollaborationType;
import com.example.RealMatch.business.presentation.dto.response.CollaborationProjection;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CampaignProposalRepositoryImpl
        implements CampaignProposalRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // ✅ /me 조회용
    @Override
    public List<CollaborationProjection> findProposalCollaborations(
            List<Long> proposalIds,
            CollaborationType type
    ) {
        return baseProposalQuery(proposalIds, type, null);
    }

    // ✅ search 조회용
    @Override
    public List<CollaborationProjection> searchProposalCollaborations(
            List<Long> proposalIds,
            CollaborationType type,
            List<Long> brandIds
    ) {
        return baseProposalQuery(proposalIds, type, brandIds);
    }

    /**
     * 공통 Querydsl 베이스
     */
    private List<CollaborationProjection> baseProposalQuery(
            List<Long> proposalIds,
            CollaborationType type,
            List<Long> brandIds
    ) {
        return queryFactory
                .select(Projections.constructor(
                        CollaborationProjection.class,
                        campaign.id,
                        campaignProposal.id,
                        brand.brandName,
                        brand.logoUrl,
                        campaignProposal.title,
                        campaignProposal.status,
                        campaignProposal.startDate,
                        campaignProposal.endDate,
                        Expressions.constant(type)
                ))
                .from(campaignProposal)
                .join(campaignProposal.brand, brand)
                .leftJoin(campaignProposal.campaign, campaign)
                .where(
                        campaignProposal.id.in(proposalIds),
                        brandIds != null ? brand.id.in(brandIds) : null
                )
                .fetch();
    }
}

