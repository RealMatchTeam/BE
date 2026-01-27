package com.example.RealMatch.user.infrastructure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.RealMatch.user.presentation.dto.response.MyScrapResponseDto.BrandScrap;
import com.example.RealMatch.user.presentation.dto.response.MyScrapResponseDto.CampaignScrap;

@Component
public class ScrapMockDataProvider { // Querydsl 대신 사용할 목업 데이터 제공자

    // 브랜드 데이터 조회
    public List<BrandScrap> getBrandScraps(String sort, String categoryFilter) {
        // 하드코딩 데이터 생성
        List<BrandScrap> list = createBrandMockData();

        // 필터링 로직 (예: 카테고리 필터가 들어왔다면)
        if (categoryFilter != null && !categoryFilter.isBlank()) {
            list = list.stream()
                    .filter(b -> b.hashtags().contains(categoryFilter))
                    .collect(Collectors.toList());
        }

        // 정렬 로직
        sortBrandList(list, sort);

        return list;
    }

    // 캠페인 데이터 조회
    public List<CampaignScrap> getCampaignScraps(String sort, Integer minRewardFilter) {
        // 하드코딩 데이터
        List<CampaignScrap> list = createCampaignMockData();

        // 필터링 로직
        if (minRewardFilter != null && minRewardFilter > 0) {
            list = list.stream()
                    .filter(c -> c.reward() >= minRewardFilter)
                    .collect(Collectors.toList());
        }

        // 정렬 로직
        sortCampaignList(list, sort);

        return list;
    }

    // --- 데이터 생성 메서드 (매번 새로운 리스트 반환) ---
    private List<BrandScrap> createBrandMockData() {
        List<BrandScrap> list = new ArrayList<>();
        list.add(new BrandScrap(1L, "라운드랩", "url", 99, List.of("청정자극", "저자극", "심플한 감성"), true));
        list.add(new BrandScrap(2L, "명큐파머", "url", 89, List.of("자연주의", "식간갑의 미학", "심플함적"), true));
        list.add(new BrandScrap(3L, "이즈앤트리", "url", 79, List.of("클린 뷰티", "저자극", "성분 중심"), true));
        return list;
    }

    private List<CampaignScrap> createCampaignMockData() {
        List<CampaignScrap> list = new ArrayList<>();
        list.add(new CampaignScrap(101L, "비플레인", "'글로우업' 선크림 신제품 홍보", "url", 98, 100000, 0, 2, 5, true));
        list.add(new CampaignScrap(102L, "라운드랩", "'글로우잇미' 크림 신제품 홍보", "url", 89, 0, 3, 3, 6, true));
        list.add(new CampaignScrap(103L, "이즈앤트리", "비타크림 신제품 체험단 모집", "url", 79, 150000, 5, 2, 7, true));
        return list;
    }

    // --- 정렬 프라이빗 메서드들 --- 아직 불완전
    private void sortBrandList(List<BrandScrap> list, String sort) {
        if (sort == null || sort.isBlank()) {
            sort = "matchingRate"; // 기본값 명시
        }

        switch (sort.toLowerCase()) {
            case "matchingrate" ->
                    list.sort(Comparator.comparingInt(BrandScrap::matchingRate).reversed());
            case "popular" ->
                    list.sort(Comparator.comparingLong(BrandScrap::brandId));
            case "recent" ->
                    list.sort(Comparator.comparingLong(BrandScrap::brandId).reversed());
            default ->
                    list.sort(Comparator.comparingInt(BrandScrap::matchingRate).reversed());
        }
    }

    private void sortCampaignList(List<CampaignScrap> list, String sort) {
        if (sort == null || sort.isBlank()) {
            sort = "matchingRate"; // 기본값 명시
        }

        switch (sort.toLowerCase()) {
            case "matchingrate" ->
                    list.sort(Comparator.comparingInt(CampaignScrap::matchingRate).reversed());
            case "popular" ->
                    list.sort(Comparator.comparingInt(CampaignScrap::currentApplicants).reversed());
            case "reward" ->
                    list.sort(Comparator.comparingInt(CampaignScrap::reward).reversed());
            case "dday" ->
                    list.sort(Comparator.comparingInt(CampaignScrap::dDay));
            default ->
                    list.sort(Comparator.comparingInt(CampaignScrap::matchingRate).reversed());
        }
    }
}
