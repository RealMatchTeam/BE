package com.example.RealMatch.campaign.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.RealMatch.global.common.DeleteBaseEntity;

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
@Table(name = "campaign")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Campaign extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    public Campaign(String title, String description, String preferredSkills, String schedule,
                    String product, Long rewardAmount,
                    LocalDate startDate, LocalDate endDate,
                    LocalDateTime recruitStartDate, LocalDateTime recruitEndDate,
                    Integer quota, Long createdBy) {
        this.title = title;
        this.description = description;
        this.preferredSkills = preferredSkills;
        this.schedule = schedule;
        this.product = product;
        this.rewardAmount = rewardAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.recruitStartDate = recruitStartDate;
        this.recruitEndDate = recruitEndDate;
        this.quota = quota;
        this.createdBy = createdBy;
    }

    public void update(String title, String description, String preferredSkills, String schedule,
                       String product, Long rewardAmount,
                       LocalDate startDate, LocalDate endDate,
                       LocalDateTime recruitStartDate, LocalDateTime recruitEndDate,
                       Integer quota, Long updatedBy) {
        this.title = title;
        this.description = description;
        this.preferredSkills = preferredSkills;
        this.schedule = schedule;
        this.product = product;
        this.rewardAmount = rewardAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.recruitStartDate = recruitStartDate;
        this.recruitEndDate = recruitEndDate;
        this.quota = quota;
        this.updatedBy = updatedBy;
    }

    public void softDelete(Long deletedBy) {
        this.deletedBy = deletedBy;
    }
}
