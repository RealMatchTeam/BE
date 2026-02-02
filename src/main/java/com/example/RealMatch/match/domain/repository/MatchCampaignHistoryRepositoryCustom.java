package com.example.RealMatch.match.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.RealMatch.match.domain.entity.MatchCampaignHistory;
import com.example.RealMatch.match.domain.entity.enums.CategoryType;

public interface MatchCampaignHistoryRepositoryCustom {

    Page<MatchCampaignHistory> searchCampaigns(
            Long userId,
            String keyword,
            CategoryType category,
            Pageable pageable
    );

    long countSearchCampaigns(Long userId, String keyword, CategoryType category);
}
