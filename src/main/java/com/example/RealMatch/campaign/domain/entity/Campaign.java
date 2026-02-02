package com.example.RealMatch.campaign.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.campaign.domain.enums.CampaignOriginType;
import com.example.RealMatch.campaign.domain.enums.CampaignRecruitingStatus;
import com.example.RealMatch.campaign.domain.enums.CampaignStatus;
import com.example.RealMatch.global.common.DeleteBaseEntity;

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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "campaign")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Campaign extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "proposal_id")
    private Long proposalId;  // originType이 proposal이라면 무조건 값이 있어야함

    @Enumerated(EnumType.STRING)
    @Column(name = "origin_type", nullable = false, length = 30)
    private CampaignOriginType originType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CampaignStatus status;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "preferred_skills", nullable = false, length = 1000)
    private String preferredSkills;

    @Column(name = "schedule", nullable = false, length = 1000)
    private String schedule;

    @Column(name = "video_spec", nullable = false, length = 1000)
    private String videoSpec;

    // 협찬품으로 수정 필요!!
    @Column(nullable = false)
    private String product;

    @Column(name = "reward_amount", nullable = false)
    private Long rewardAmount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "recruit_start_date", nullable = false)
    private LocalDateTime recruitStartDate;

    @Column(name = "recruit_end_date", nullable = false)
    private LocalDateTime recruitEndDate;

    @Column(nullable = false)
    private Integer quota;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Builder
    public Campaign(
            Brand brand,
            String title,
            String description,
            String preferredSkills,
            String schedule,
            String videoSpec,
            String product,
            Long rewardAmount,
            String imageUrl,
            Long proposalId,
            CampaignOriginType originType,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime recruitStartDate,
            LocalDateTime recruitEndDate,
            Integer quota,
            Long createdBy
    ) {
        if (originType == CampaignOriginType.PROPOSAL && proposalId == null) {
            throw new IllegalArgumentException(
                    "originType이 PROPOSAL이면 proposalId는 필수입니다."
            );
        }

        if (originType == CampaignOriginType.DIRECT && proposalId != null) {
            throw new IllegalArgumentException(
                    "DIRECT 캠페인은 proposalId를 가질 수 없습니다."
            );
        }

        this.brand = brand;
        this.title = title;
        this.description = description;
        this.preferredSkills = preferredSkills;
        this.schedule = schedule;
        this.videoSpec = videoSpec;
        this.product = product;
        this.rewardAmount = rewardAmount;
        this.imageUrl = imageUrl;
        this.proposalId = proposalId;
        this.originType = originType;
        this.status = CampaignStatus.DRAFT;
        this.startDate = startDate;
        this.endDate = endDate;
        this.recruitStartDate = recruitStartDate;
        this.recruitEndDate = recruitEndDate;
        this.quota = quota;
        this.createdBy = createdBy;
    }

    public void softDelete(Long deletedBy) {
        this.deletedBy = deletedBy;
    }

    public CampaignRecruitingStatus getCampaignRecrutingStatus(LocalDateTime now) {
        if (now.isBefore(this.recruitStartDate)) {
            return CampaignRecruitingStatus.UPCOMING;
        }
        if (now.isAfter(this.recruitEndDate)) {
            return CampaignRecruitingStatus.CLOSED;
        }
        return CampaignRecruitingStatus.RECRUITING;
    }

    public void activate() {
        if (this.status != CampaignStatus.DRAFT) {
            throw new IllegalStateException("DRAFT 상태에서만 활성화할 수 있습니다.");
        }
        this.status = CampaignStatus.ACTIVE;
    }

    public void complete() {
        if (this.status != CampaignStatus.ACTIVE) {
            throw new IllegalStateException("ACTIVE 상태에서만 종료할 수 있습니다.");
        }
        this.status = CampaignStatus.COMPLETED;
    }

    public void cancel() {
        if (this.status == CampaignStatus.COMPLETED) {
            throw new IllegalStateException("이미 종료된 캠페인은 취소할 수 없습니다.");
        }
        this.status = CampaignStatus.CANCELLED;
    }


}
