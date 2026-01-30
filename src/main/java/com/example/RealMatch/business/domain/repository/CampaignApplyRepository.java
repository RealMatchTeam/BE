package com.example.RealMatch.business.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.business.domain.entity.CampaignApply;
import com.example.RealMatch.business.domain.enums.ProposalStatus;

public interface CampaignApplyRepository extends JpaRepository<CampaignApply, Long> {
    boolean existsByUserIdAndCampaignId(Long userId, Long campaignId);

    // 사용자의 지원 목록 조회
    List<CampaignApply> findByUserId(Long userId);

    // ID로 조회 (UUID)
    Optional<CampaignApply> findById(UUID id);
}
