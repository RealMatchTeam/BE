package com.example.RealMatch.brand.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.attachment.application.service.AttachmentUrlService;
import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.brand.exception.BrandErrorCode;
import com.example.RealMatch.brand.presentation.dto.response.BrandCampaignResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandCampaignSliceResponse;
import com.example.RealMatch.brand.presentation.dto.response.BrandExistingCampaignResponse;
import com.example.RealMatch.brand.presentation.dto.response.BrandRecruitingCampaignResponse;
import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.campaign.domain.repository.CampaignLikeRepository;
import com.example.RealMatch.campaign.domain.repository.CampaignRepository;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandCampaignService {
    private final BrandRepository brandRepository;
    private final CampaignRepository campaignRepository;
    private final AttachmentUrlService attachmentUrlService;
    private final CampaignLikeRepository campaignLikeRepository;

    // 브랜드의 캠페인 리스트 조회
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

    public BrandExistingCampaignResponse getExistingCampaigns(Long brandId) {

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new CustomException(BrandErrorCode.BRAND_NOT_FOUND));

        List<Campaign> campaigns =
                campaignRepository.findRecruitingCampaignsByBrandId(brand.getId());

        // 3️⃣ DTO 변환
        List<BrandExistingCampaignResponse.CampaignItem> items = campaigns.stream()
                .map(campaign -> new BrandExistingCampaignResponse.CampaignItem(
                        campaign.getId(),
                        campaign.getTitle()
                ))
                .toList();

        return new BrandExistingCampaignResponse(items);
    }

    public BrandRecruitingCampaignResponse getRecruitingCampaigns(Long userId, Long brandId) {

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new CustomException(BrandErrorCode.BRAND_NOT_FOUND));

        List<Campaign> campaigns =
                campaignRepository.findRecruitingCampaignsByBrandId(brand.getId());

        if (campaigns.isEmpty()) {
            return new BrandRecruitingCampaignResponse(java.util.Collections.emptyList());
        }

        List<Long> campaignIds = campaigns.stream()
                .map(Campaign::getId)
                .toList();

        Set<Long> likedCampaignIds =
                campaignLikeRepository.findLikedCampaignIds(userId, campaignIds);


        LocalDate today = LocalDate.now();

        // 3️⃣ DTO 변환 (D-DAY 계산 포함)
        List<BrandRecruitingCampaignResponse.CampaignCard> cards = campaigns.stream()
                .map(campaign -> {
                    int dDay = (int) ChronoUnit.DAYS.between(today, campaign.getRecruitEndDate().toLocalDate());
                    String campaignImageUrl = attachmentUrlService.getAccessUrl(campaign.getImageUrl());
                    boolean isLiked = likedCampaignIds.contains(campaign.getId());
                    return new BrandRecruitingCampaignResponse.CampaignCard(
                            campaign.getId(),
                            brand.getBrandName(),
                            campaign.getTitle(),
                            isLiked,
                            campaign.getQuota(),
                            Math.max(dDay, 0), // D-DAY 보정
                            campaign.getRewardAmount(),
                            campaignImageUrl
                    );
                })
                .toList();

        return new BrandRecruitingCampaignResponse(cards);
    }

}
