package com.example.RealMatch.business.domain.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.business.domain.enums.ProposalStatus;
import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.global.common.BaseEntity;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.enums.Role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "campaign_proposal")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CampaignProposal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Enumerated(EnumType.STRING)
    @Column(name = "who_proposed", nullable = false, length = 20)
    private Role whoProposed;

    @Column(name = "proposed_user_id", nullable = false)
    private Long proposedUserId;

    // 기존 캠페인 기반 제안이면 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    // ===== 제안 내용 (Draft) =====
    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 1000)
    private String campaignDescription;

    @Column(name = "reward_amount", nullable = false)
    private Integer rewardAmount;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // ============================

    @Column(name = "refusal_reason", length = 1000)
    private String refusalReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProposalStatus status;

    @OneToMany(
            mappedBy = "campaignProposal",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CampaignProposalContentTag> tags = new ArrayList<>();


    @Builder
    protected CampaignProposal(
            User creator,
            Brand brand,
            Role whoProposed,
            Long proposedUserId,
            Campaign campaign,
            String title,
            String campaignDescription,
            Integer rewardAmount,
            Long productId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        this.creator = creator;
        this.brand = brand;
        this.whoProposed = whoProposed;
        this.proposedUserId = proposedUserId;
        this.campaign = campaign;
        this.title = title;
        this.campaignDescription = campaignDescription;
        this.rewardAmount = rewardAmount;
        this.productId = productId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = ProposalStatus.REVIEWING;
    }

    public void modify(
            String title,
            String description,
            Integer rewardAmount,
            Long productId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (this.campaign != null) {
            this.title = title;
        }
        this.campaignDescription = description;
        this.rewardAmount = rewardAmount;
        this.productId = productId;
        this.startDate = startDate;
        this.endDate = endDate;
    }


    /* ===== 도메인 메서드 ===== */

    public boolean isModifiable() {
        return this.status == ProposalStatus.REVIEWING;
    }

    public void clearContentTags() {
        this.tags.clear();
    }

    public boolean isNewCampaignProposal() {
        return campaign == null;
    }

    public void match() {
        this.status = ProposalStatus.MATCHED;
    }

    public void reject(String refusalReason) {
        this.status = ProposalStatus.REJECTED;
        this.refusalReason = refusalReason;
    }

    public void addTag(CampaignProposalContentTag tag) {
        this.tags.add(tag);
    }
    /**
     * MATCHED 시 Campaign 생성용 변환 (추가 확인 필요, 태그 관리 필요)
     */
//    public Campaign toCampaign(Long createdBy) {
//        return Campaign.builder()
//                .title(this.title)
//                .description(this.campaignDescription)
//                .rewardAmount(this.rewardAmount)
//                .startDate(this.startDate)
//                .endDate(this.endDate)
//                .createdBy(createdBy)
//                .build();
//    }
}
