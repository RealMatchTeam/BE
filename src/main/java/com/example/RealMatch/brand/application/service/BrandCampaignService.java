package com.example.RealMatch.brand.application.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.brand.exception.BrandErrorCode;
import com.example.RealMatch.brand.presentation.dto.response.BrandCampaignResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandCampaignSliceResponse;
import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.campaign.domain.repository.CampaignRepository;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandCampaignService {
    private final BrandRepository brandRepository;
    private final CampaignRepository campaignRepository;

    // 브랜드의 캠페인 리스트 조회
    @Transactional(readOnly = true)
    public BrandCampaignSliceResponse getBrandCampaigns(Long brandId, Long cursor, int size) {
        brandRepository.findById(brandId)
                .orElseThrow(() -> new CustomException(BrandErrorCode.BRAND_NOT_FOUND));

        Pageable pageable = PageRequest.of(0, size + 1);
        List<Campaign> campaigns = campaignRepository.findBrandCampaignsWithCursor(brandId, cursor, pageable);

        boolean hasNext = campaigns.size() > size;
        if (hasNext) {
            campaigns = campaigns.subList(0, size);
        }

        LocalDateTime now = LocalDateTime.now();
        List<BrandCampaignResponseDto> items = campaigns.stream()
                .map(c -> new BrandCampaignResponseDto(
                        c.getId(),
                        c.getTitle(),
                        c.getRecruitStartDate().toLocalDate(),
                        c.getRecruitEndDate().toLocalDate(),
                        c.getCampaignRecrutingStatus(now)
                ))
                .toList();
        return new BrandCampaignSliceResponse(items, hasNext);
    }

}
