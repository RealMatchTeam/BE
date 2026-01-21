package com.example.RealMatch.campaign.domain.entity;

import java.time.LocalDateTime;

import com.example.RealMatch.global.common.UpdateBaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "p_campaign_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CampaignType extends UpdateBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(length = 100)
    private String name;

    @Builder
    public CampaignType(Campaign campaign, String name) {
        this.campaign = campaign;
        this.name = name;
    }
}
