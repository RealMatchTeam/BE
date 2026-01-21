package com.example.RealMatch.match.application.service;

import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.example.RealMatch.match.presentation.dto.request.MatchRequestDto;
import com.example.RealMatch.match.presentation.dto.response.MatchBrandResponseDto;
import com.example.RealMatch.match.presentation.dto.response.MatchCampaignResponseDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto.BrandDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto.CampaignDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto.CreatorAnalysisDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto.HighMatchingBrandListDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto.HighMatchingCampaignListDto;

@Service
public class MatchServiceImpl implements MatchService {

    // TODO: 실제 매칭 로직 구현 필요
    @Override
    public MatchResponseDto matchBrand(MatchRequestDto requestDto) {
        CreatorAnalysisDto creatorAnalysis = CreatorAnalysisDto.builder()
                .creatorType("민첩")
                .beautyStyle("미니멀")
                .fashionStyle("스트릿")
                .contentStyle("정보성 리뷰")
                .bestFitBrand("이니스프리")
                .build();

        HighMatchingBrandListDto highMatchingBrandList = HighMatchingBrandListDto.builder()
                .count(3)
                .brands(Arrays.asList(
                        BrandDto.builder()
                                .id(101L)
                                .name("비플레인")
                                .matchingRatio(98)
                                .isLiked(true)
                                .isRecruiting(true)
                                .tags(Arrays.asList("비건", "천연재료"))
                                .build(),
                        BrandDto.builder()
                                .id(102L)
                                .name("이즈앤트리")
                                .matchingRatio(95)
                                .isLiked(true)
                                .isRecruiting(false)
                                .tags(Arrays.asList("저자극", "클린뷰티"))
                                .build(),
                        BrandDto.builder()
                                .id(103L)
                                .name("마녀공장")
                                .matchingRatio(93)
                                .isLiked(true)
                                .isRecruiting(true)
                                .tags(Arrays.asList("순한성분", "클린뷰티"))
                                .build()
                ))
                .build();

        HighMatchingCampaignListDto highMatchingCampaignList = HighMatchingCampaignListDto.builder()
                .count(3)
                .brands(Arrays.asList(
                        CampaignDto.builder()
                                .id(101L)
                                .name("비플레인")
                                .matchingRatio(98)
                                .isLiked(true)
                                .isRecruiting(true)
                                .manuscriptFee(200000)
                                .detail("신제품 릴스 홍보")
                                .dDay(7)
                                .totalRecruit(10)
                                .currentRecruit(7)
                                .build(),
                        CampaignDto.builder()
                                .id(102L)
                                .name("이즈앤트리")
                                .matchingRatio(95)
                                .isLiked(true)
                                .isRecruiting(null)
                                .manuscriptFee(200000)
                                .detail("신제품 릴스 홍보")
                                .dDay(7)
                                .totalRecruit(10)
                                .currentRecruit(7)
                                .build(),
                        CampaignDto.builder()
                                .id(103L)
                                .name("마녀공장")
                                .matchingRatio(93)
                                .isLiked(true)
                                .isRecruiting(true)
                                .manuscriptFee(200000)
                                .detail("신제품 릴스 홍보")
                                .dDay(7)
                                .totalRecruit(10)
                                .currentRecruit(7)
                                .build()
                ))
                .build();

        return MatchResponseDto.builder()
                .creatorAnalysis(creatorAnalysis)
                .highMatchingBrandList(highMatchingBrandList)
                .highMatchingCampaignList(highMatchingCampaignList)
                .build();
    }

    @Override
    public MatchBrandResponseDto getMatchingBrands(String userId) {
        return MatchBrandResponseDto.builder()
                .count(3)
                .brands(Arrays.asList(
                        MatchBrandResponseDto.BrandDto.builder()
                                .id(101L)
                                .name("비플레인")
                                .matchingRatio(98)
                                .isLiked(true)
                                .isRecruiting(true)
                                .tags(Arrays.asList("비건", "천연재료"))
                                .build(),
                        MatchBrandResponseDto.BrandDto.builder()
                                .id(102L)
                                .name("이즈앤트리")
                                .matchingRatio(95)
                                .isLiked(true)
                                .isRecruiting(false)
                                .tags(Arrays.asList("저자극", "클린뷰티"))
                                .build(),
                        MatchBrandResponseDto.BrandDto.builder()
                                .id(103L)
                                .name("마녀공장")
                                .matchingRatio(93)
                                .isLiked(true)
                                .isRecruiting(true)
                                .tags(Arrays.asList("순한성분", "클린뷰티"))
                                .build()
                ))
                .build();
    }

    @Override
    public MatchCampaignResponseDto getMatchingCampaigns(String userId) {
        return MatchCampaignResponseDto.builder()
                .count(3)
                .brands(Arrays.asList(
                        MatchCampaignResponseDto.CampaignDto.builder()
                                .id(101L)
                                .name("비플레인")
                                .matchingRatio(98)
                                .isLiked(true)
                                .isRecruiting(true)
                                .manuscriptFee(200000)
                                .detail("신제품 릴스 홍보")
                                .dDay(7)
                                .totalRecruit(10)
                                .currentRecruit(7)
                                .build(),
                        MatchCampaignResponseDto.CampaignDto.builder()
                                .id(102L)
                                .name("이즈앤트리")
                                .matchingRatio(95)
                                .isLiked(true)
                                .isRecruiting(null)
                                .manuscriptFee(200000)
                                .detail("신제품 릴스 홍보")
                                .dDay(7)
                                .totalRecruit(10)
                                .currentRecruit(7)
                                .build(),
                        MatchCampaignResponseDto.CampaignDto.builder()
                                .id(103L)
                                .name("마녀공장")
                                .matchingRatio(93)
                                .isLiked(true)
                                .isRecruiting(true)
                                .manuscriptFee(200000)
                                .detail("신제품 릴스 홍보")
                                .dDay(7)
                                .totalRecruit(10)
                                .currentRecruit(7)
                                .build()
                ))
                .build();
    }
}
