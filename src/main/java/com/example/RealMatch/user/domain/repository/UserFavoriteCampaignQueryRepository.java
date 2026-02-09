package com.example.RealMatch.user.domain.repository;

import java.util.List;

import com.example.RealMatch.match.domain.entity.enums.CampaignSortType;
import com.example.RealMatch.user.presentation.dto.response.FavoriteCampaignDto;

public interface UserFavoriteCampaignQueryRepository {

    List<FavoriteCampaignDto> findFavoriteCampaigns(
            Long userId,
            CampaignSortType sortType
    );
}
