package com.example.RealMatch.campaign.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.RealMatch.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_campaign")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CampaignEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

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

    @Column(name = "matching_rate")
    private Integer matchingRate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Builder
    public CampaignEntity(String title, String description, Long rewardAmount,
                          LocalDate startDate, LocalDate endDate,
                          LocalDateTime recruitStartDate, LocalDateTime recruitEndDate,
                          Integer quota, Integer matchingRate, Long createdBy) {
        this.title = title;
        this.description = description;
        this.rewardAmount = rewardAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.recruitStartDate = recruitStartDate;
        this.recruitEndDate = recruitEndDate;
        this.quota = quota;
        this.matchingRate = matchingRate;
        this.createdBy = createdBy;
        this.isDeleted = false;
    }

    public void update(String title, String description, Long rewardAmount,
                       LocalDate startDate, LocalDate endDate,
                       LocalDateTime recruitStartDate, LocalDateTime recruitEndDate,
                       Integer quota, Long updatedBy) {
        this.title = title;
        this.description = description;
        this.rewardAmount = rewardAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.recruitStartDate = recruitStartDate;
        this.recruitEndDate = recruitEndDate;
        this.quota = quota;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    public void softDelete(Long deletedBy) {
        this.isDeleted = true;
        this.deletedBy = deletedBy;
        this.deletedAt = LocalDateTime.now();
    }
}
