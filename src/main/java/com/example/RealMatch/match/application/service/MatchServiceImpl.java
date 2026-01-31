package com.example.RealMatch.match.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.repository.BrandLikeRepository;
import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.business.domain.repository.CampaignApplyRepository;
import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.campaign.domain.repository.CampaignLikeRepository;
import com.example.RealMatch.campaign.domain.repository.CampaignRepository;
import com.example.RealMatch.match.application.util.MatchScoreCalculator;
import com.example.RealMatch.match.domain.entity.MatchBrandHistory;
import com.example.RealMatch.match.domain.entity.MatchCampaignHistory;
import com.example.RealMatch.match.domain.repository.MatchBrandHistoryRepository;
import com.example.RealMatch.match.domain.repository.MatchCampaignHistoryRepository;
import com.example.RealMatch.match.infrastructure.redis.RedisDocumentHelper;
import com.example.RealMatch.match.infrastructure.redis.document.BrandTagDocument;
import com.example.RealMatch.match.infrastructure.redis.document.CampaignTagDocument;
import com.example.RealMatch.match.infrastructure.redis.document.UserTagDocument;
import com.example.RealMatch.match.presentation.dto.request.MatchRequestDto;
import com.example.RealMatch.match.presentation.dto.response.MatchBrandResponseDto;
import com.example.RealMatch.match.presentation.dto.response.MatchCampaignResponseDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto.BrandDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto.CampaignDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto.CreatorAnalysisDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto.HighMatchingBrandListDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto.HighMatchingCampaignListDto;
import com.example.RealMatch.tag.domain.repository.BrandTagRepository;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchServiceImpl implements MatchService {

    private static final Logger LOG = LoggerFactory.getLogger(MatchServiceImpl.class);
    private static final int TOP_MATCH_COUNT = 10;

    private final RedisDocumentHelper redisDocumentHelper;

    private final BrandRepository brandRepository;
    private final CampaignRepository campaignRepository;
    private final BrandLikeRepository brandLikeRepository;
    private final CampaignLikeRepository campaignLikeRepository;
    private final CampaignApplyRepository campaignApplyRepository;
    private final BrandTagRepository brandTagRepository;
    private final UserRepository userRepository;
    private final MatchBrandHistoryRepository matchBrandHistoryRepository;
    private final MatchCampaignHistoryRepository matchCampaignHistoryRepository;
    
    // ******* //
    // 매칭 요청 //
    // ******* //
    @Override
    @Transactional
    public MatchResponseDto match(MatchRequestDto requestDto) {
        Long userId = Long.parseLong(requestDto.getUserId());

        UserTagDocument userDoc = convertToUserTagDocument(requestDto);

        CreatorAnalysisDto creatorAnalysis = buildCreatorAnalysisFromRequest(requestDto, userDoc);

        List<BrandMatchResult> brandResults = findMatchingBrandResults(userDoc, userId);
        List<BrandDto> matchedBrands = brandResults.stream()
                .map(this::toBrandDto)
                .toList();

        HighMatchingBrandListDto brandListDto = HighMatchingBrandListDto.builder()
                .count(matchedBrands.size())
                .brands(matchedBrands)
                .build();

        List<CampaignMatchResult> campaignResults = findMatchingCampaignResults(userDoc, userId);
        List<CampaignDto> matchedCampaigns = campaignResults.stream()
                .map(this::toCampaignDto)
                .toList();

        HighMatchingCampaignListDto campaignListDto = HighMatchingCampaignListDto.builder()
                .count(matchedCampaigns.size())
                .brands(matchedCampaigns)
                .build();

        saveMatchHistory(userId, brandResults, campaignResults);

        return MatchResponseDto.builder()
                .creatorAnalysis(creatorAnalysis)
                .highMatchingBrandList(brandListDto)
                .highMatchingCampaignList(campaignListDto)
                .build();
    }

    private void saveMatchHistory(Long userId, List<BrandMatchResult> brandResults, List<CampaignMatchResult> campaignResults) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            LOG.warn("User not found for saving match history. userId={}", userId);
            return;
        }

        for (BrandMatchResult result : brandResults) {
            if (!matchBrandHistoryRepository.existsByUserIdAndBrandId(userId, result.brand().getId())) {
                MatchBrandHistory history = MatchBrandHistory.builder()
                        .user(user)
                        .brand(result.brand())
                        .build();
                matchBrandHistoryRepository.save(history);
            }
        }

        for (CampaignMatchResult result : campaignResults) {
            if (!matchCampaignHistoryRepository.existsByUserIdAndCampaignId(userId, result.campaign().getId())) {
                MatchCampaignHistory history = MatchCampaignHistory.builder()
                        .user(user)
                        .campaign(result.campaign())
                        .build();
                matchCampaignHistoryRepository.save(history);
            }
        }

        LOG.info("Match history saved. userId={}, brands={}, campaigns={}",
                userId, brandResults.size(), campaignResults.size());
    }

    // Request -> Redis Document Converter
    private UserTagDocument convertToUserTagDocument(MatchRequestDto dto) {
        Set<String> fashionTags = new HashSet<>();
        Set<String> beautyTags = new HashSet<>();
        Set<String> contentTags = new HashSet<>();

        if (dto.getFashion() != null) {
            if (dto.getFashion().getStyles() != null) {
                fashionTags.addAll(dto.getFashion().getStyles());
            }
            if (dto.getFashion().getItems() != null) {
                fashionTags.addAll(dto.getFashion().getItems());
            }
        }

        if (dto.getBeauty() != null) {
            if (dto.getBeauty().getInterests() != null) {
                beautyTags.addAll(dto.getBeauty().getInterests());
            }
            if (dto.getBeauty().getFunctions() != null) {
                beautyTags.addAll(dto.getBeauty().getFunctions());
            }
            if (dto.getBeauty().getSkinType() != null) {
                beautyTags.add(dto.getBeauty().getSkinType());
            }
            if (dto.getBeauty().getMakeupStyle() != null) {
                beautyTags.add(dto.getBeauty().getMakeupStyle());
            }
        }

        if (dto.getSns() != null && dto.getSns().getContentStyle() != null) {
            MatchRequestDto.ContentStyleDto contentStyle = dto.getSns().getContentStyle();
            if (contentStyle.getFormat() != null) {
                contentTags.add(contentStyle.getFormat());
            }
            if (contentStyle.getType() != null) {
                contentTags.add(contentStyle.getType());
            }
        }

        String topSize = null;
        String bottomSize = null;
        if (dto.getSize() != null) {
            if (dto.getSize().getUpper() != null) {
                topSize = String.valueOf(dto.getSize().getUpper());
            }
            if (dto.getSize().getBottom() != null) {
                bottomSize = String.valueOf(dto.getSize().getBottom());
            }
        }

        Long avgViews = null;
        if (dto.getSns() != null && dto.getSns().getContentStyle() != null
                && dto.getSns().getContentStyle().getAvgViews() != null) {
            try {
                avgViews = Long.parseLong(dto.getSns().getContentStyle().getAvgViews().replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                LOG.debug("Could not parse avgViews: {}", dto.getSns().getContentStyle().getAvgViews());
            }
        }

        Set<String> contentsAge = new HashSet<>();
        Set<String> contentsGender = new HashSet<>();
        if (dto.getSns() != null && dto.getSns().getMainAudience() != null) {
            if (dto.getSns().getMainAudience().getAge() != null) {
                contentsAge.addAll(dto.getSns().getMainAudience().getAge());
            }
            if (dto.getSns().getMainAudience().getSex() != null) {
                contentsGender.addAll(dto.getSns().getMainAudience().getSex());
            }
        }

        String contentsLength = null;
        if (dto.getSns() != null && dto.getSns().getContentStyle() != null) {
            contentsLength = dto.getSns().getContentStyle().getAvgVideoLength();
        }

        return UserTagDocument.builder()
                .userId(Long.parseLong(dto.getUserId()))
                .fashionTags(fashionTags)
                .beautyTags(beautyTags)
                .contentTags(contentTags)
                .height(dto.getHeight())
                .topSize(topSize)
                .bottomSize(bottomSize)
                .averageContentsViews(avgViews)
                .contentsAge(contentsAge)
                .contentsGender(contentsGender)
                .contentsLength(contentsLength)
                .build();
    }

    private CreatorAnalysisDto buildCreatorAnalysisFromRequest(MatchRequestDto dto, UserTagDocument userDoc) {
        String creatorType = determineCreatorType(userDoc);

        String beautyStyle = dto.getBeauty() != null && dto.getBeauty().getMakeupStyle() != null
                ? dto.getBeauty().getMakeupStyle()
                : getFirstFromList(dto.getBeauty() != null ? dto.getBeauty().getInterests() : null);

        String fashionStyle = getFirstFromList(dto.getFashion() != null ? dto.getFashion().getStyles() : null);

        String contentStyle = dto.getSns() != null && dto.getSns().getContentStyle() != null
                ? dto.getSns().getContentStyle().getType()
                : null;

        return CreatorAnalysisDto.builder()
                .creatorType(creatorType)
                .beautyStyle(beautyStyle != null ? beautyStyle : "미정")
                .fashionStyle(fashionStyle != null ? fashionStyle : "미정")
                .contentStyle(contentStyle != null ? contentStyle : "미정")
                .bestFitBrand(findBestFitBrandName(userDoc))
                .build();
    }

    private String getFirstFromList(List<String> list) {
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }
    
    // **************** //
    // Redis에서 매칭 요청 //
    // **************** //
    private List<BrandMatchResult> findMatchingBrandResults(UserTagDocument userDoc, Long userId) {
        List<BrandTagDocument> allBrandDocs = redisDocumentHelper.findAllBrandTagDocuments();

        if (allBrandDocs.isEmpty()) {
            LOG.info("No brand tag documents found in Redis");
            return List.of();
        }

        Set<Long> likedBrandIds = brandLikeRepository.findByUserId(userId).stream()
                .map(like -> like.getBrand().getId())
                .collect(Collectors.toSet());

        Map<Long, Brand> brandMap = brandRepository.findAll().stream()
                .collect(Collectors.toMap(Brand::getId, b -> b));

        List<BrandMatchResult> results = new ArrayList<>();

        for (BrandTagDocument brandDoc : allBrandDocs) {
            Brand brand = brandMap.get(brandDoc.getBrandId());
            if (brand == null) {
                continue;
            }

            int matchScore = MatchScoreCalculator.calculateBrandMatchScore(userDoc, brandDoc);

            results.add(new BrandMatchResult(
                    brand,
                    brandDoc,
                    matchScore,
                    likedBrandIds.contains(brand.getId()),
                    isRecruitingBrand(brand.getId())
            ));
        }

        return results.stream()
                .sorted(Comparator.comparingInt(BrandMatchResult::matchScore).reversed())
                .limit(TOP_MATCH_COUNT)
                .toList();
    }

    private List<CampaignMatchResult> findMatchingCampaignResults(UserTagDocument userDoc, Long userId) {
        List<CampaignTagDocument> allCampaignDocs = redisDocumentHelper.findAllCampaignTagDocuments();

        if (allCampaignDocs.isEmpty()) {
            LOG.info("No campaign tag documents found in Redis");
            return List.of();
        }

        Set<Long> likedCampaignIds = campaignLikeRepository.findByUserId(userId).stream()
                .map(like -> like.getCampaign().getId())
                .collect(Collectors.toSet());

        Map<Long, Campaign> campaignMap = campaignRepository.findAll().stream()
                .collect(Collectors.toMap(Campaign::getId, c -> c));

        Map<Long, Long> applyCountMap = getApplyCountMap();

        List<CampaignMatchResult> results = new ArrayList<>();

        for (CampaignTagDocument campaignDoc : allCampaignDocs) {
            Campaign campaign = campaignMap.get(campaignDoc.getCampaignId());
            if (campaign == null) {
                continue;
            }

            if (campaign.getRecruitEndDate().isBefore(LocalDateTime.now())) {
                continue;
            }

            int matchScore = MatchScoreCalculator.calculateCampaignMatchScore(userDoc, campaignDoc);

            results.add(new CampaignMatchResult(
                    campaign,
                    campaignDoc,
                    matchScore,
                    likedCampaignIds.contains(campaign.getId()),
                    applyCountMap.getOrDefault(campaign.getId(), 0L).intValue()
            ));
        }

        return results.stream()
                .sorted(Comparator.comparingInt(CampaignMatchResult::matchScore).reversed())
                .limit(TOP_MATCH_COUNT)
                .toList();
    }
    
    private List<MatchBrandResponseDto.BrandDto> findMatchingBrandsForList(UserTagDocument userDoc, Long userId) {
        List<BrandTagDocument> allBrandDocs = redisDocumentHelper.findAllBrandTagDocuments();

        if (allBrandDocs.isEmpty()) {
            return List.of();
        }

        Set<Long> likedBrandIds = brandLikeRepository.findByUserId(userId).stream()
                .map(like -> like.getBrand().getId())
                .collect(Collectors.toSet());

        Map<Long, Brand> brandMap = brandRepository.findAll().stream()
                .collect(Collectors.toMap(Brand::getId, b -> b));

        Map<Long, List<String>> brandTagsMap = getBrandTagsMap();

        List<BrandMatchResult> results = new ArrayList<>();

        for (BrandTagDocument brandDoc : allBrandDocs) {
            Brand brand = brandMap.get(brandDoc.getBrandId());
            if (brand == null) {
                continue;
            }

            int matchScore = MatchScoreCalculator.calculateBrandMatchScore(userDoc, brandDoc);

            results.add(new BrandMatchResult(
                    brand,
                    brandDoc,
                    matchScore,
                    likedBrandIds.contains(brand.getId()),
                    isRecruitingBrand(brand.getId())
            ));
        }

        return results.stream()
                .sorted(Comparator.comparingInt(BrandMatchResult::matchScore).reversed())
                .limit(TOP_MATCH_COUNT)
                .map(r -> toMatchBrandDto(r, brandTagsMap.getOrDefault(r.brand().getId(), List.of())))
                .toList();
    }

    private List<MatchCampaignResponseDto.CampaignDto> findMatchingCampaignsForList(UserTagDocument userDoc, Long userId) {
        List<CampaignTagDocument> allCampaignDocs = redisDocumentHelper.findAllCampaignTagDocuments();

        if (allCampaignDocs.isEmpty()) {
            return List.of();
        }

        Set<Long> likedCampaignIds = campaignLikeRepository.findByUserId(userId).stream()
                .map(like -> like.getCampaign().getId())
                .collect(Collectors.toSet());

        Map<Long, Campaign> campaignMap = campaignRepository.findAll().stream()
                .collect(Collectors.toMap(Campaign::getId, c -> c));

        Map<Long, Long> applyCountMap = getApplyCountMap();

        List<CampaignMatchResult> results = new ArrayList<>();

        for (CampaignTagDocument campaignDoc : allCampaignDocs) {
            Campaign campaign = campaignMap.get(campaignDoc.getCampaignId());
            if (campaign == null) {
                continue;
            }

            if (campaign.getRecruitEndDate().isBefore(LocalDateTime.now())) {
                continue;
            }

            int matchScore = MatchScoreCalculator.calculateCampaignMatchScore(userDoc, campaignDoc);

            results.add(new CampaignMatchResult(
                    campaign,
                    campaignDoc,
                    matchScore,
                    likedCampaignIds.contains(campaign.getId()),
                    applyCountMap.getOrDefault(campaign.getId(), 0L).intValue()
            ));
        }

        return results.stream()
                .sorted(Comparator.comparingInt(CampaignMatchResult::matchScore).reversed())
                .limit(TOP_MATCH_COUNT)
                .map(this::toMatchCampaignDto)
                .toList();
    }

    // *********** //
    // 매칭 결과 조회 //
    // *********** //
    @Override
    public MatchBrandResponseDto getMatchingBrands(String userId) {
        Long userIdLong = Long.parseLong(userId);

        UserTagDocument userDoc = redisDocumentHelper.findUserTagDocumentById(userIdLong);

        if (userDoc == null) {
            LOG.warn("User tag document not found in Redis. userId={}", userId);
            return MatchBrandResponseDto.builder()
                    .count(0)
                    .brands(List.of())
                    .build();
        }

        List<MatchBrandResponseDto.BrandDto> matchedBrands = findMatchingBrandsForList(userDoc, userIdLong);

        return MatchBrandResponseDto.builder()
                .count(matchedBrands.size())
                .brands(matchedBrands)
                .build();
    }

    @Override
    public MatchCampaignResponseDto getMatchingCampaigns(String userId) {
        Long userIdLong = Long.parseLong(userId);

        UserTagDocument userDoc = redisDocumentHelper.findUserTagDocumentById(userIdLong);

        if (userDoc == null) {
            LOG.warn("User tag document not found in Redis. userId={}", userId);
            return MatchCampaignResponseDto.builder()
                    .count(0)
                    .brands(List.of())
                    .build();
        }

        List<MatchCampaignResponseDto.CampaignDto> matchedCampaigns = findMatchingCampaignsForList(userDoc, userIdLong);

        return MatchCampaignResponseDto.builder()
                .count(matchedCampaigns.size())
                .brands(matchedCampaigns)
                .build();
    }

    private CreatorAnalysisDto buildCreatorAnalysis(UserTagDocument userDoc) {
        String creatorType = determineCreatorType(userDoc);
        String beautyStyle = getFirstTag(userDoc.getBeautyTags());
        String fashionStyle = getFirstTag(userDoc.getFashionTags());
        String contentStyle = getFirstTag(userDoc.getContentTags());

        return CreatorAnalysisDto.builder()
                .creatorType(creatorType)
                .beautyStyle(beautyStyle != null ? beautyStyle : "미정")
                .fashionStyle(fashionStyle != null ? fashionStyle : "미정")
                .contentStyle(contentStyle != null ? contentStyle : "미정")
                .bestFitBrand(findBestFitBrandName(userDoc))
                .build();
    }

    private String determineCreatorType(UserTagDocument userDoc) {
        int fashionCount = safeSize(userDoc.getFashionTags());
        int beautyCount = safeSize(userDoc.getBeautyTags());
        int contentCount = safeSize(userDoc.getContentTags());

        if (fashionCount >= beautyCount && fashionCount >= contentCount) {
            return "패션";
        } else if (beautyCount >= contentCount) {
            return "뷰티";
        } else {
            return "콘텐츠";
        }
    }

    private String findBestFitBrandName(UserTagDocument userDoc) {
        List<BrandTagDocument> allBrandDocs = redisDocumentHelper.findAllBrandTagDocuments();

        return allBrandDocs.stream()
                .max(Comparator.comparingInt(brandDoc ->
                        MatchScoreCalculator.calculateBrandMatchScore(userDoc, brandDoc)))
                .map(BrandTagDocument::getBrandName)
                .orElse("미정");
    }

    private BrandDto toBrandDto(BrandMatchResult result) {
        List<String> tags = new ArrayList<>();
        if (result.brandDoc().getPreferredFashionTags() != null) {
            tags.addAll(result.brandDoc().getPreferredFashionTags());
        }
        if (result.brandDoc().getPreferredBeautyTags() != null) {
            tags.addAll(result.brandDoc().getPreferredBeautyTags());
        }

        return BrandDto.builder()
                .id(result.brand().getId())
                .name(result.brand().getBrandName())
                .matchingRatio(result.matchScore())
                .isLiked(result.isLiked())
                .isRecruiting(result.isRecruiting())
                .tags(tags.stream().limit(3).toList())
                .build();
    }

    private CampaignDto toCampaignDto(CampaignMatchResult result) {
        Campaign campaign = result.campaign();
        int dDay = (int) ChronoUnit.DAYS.between(LocalDate.now(), campaign.getRecruitEndDate().toLocalDate());

        return CampaignDto.builder()
                .id(campaign.getId())
                .name(campaign.getTitle())
                .matchingRatio(result.matchScore())
                .isLiked(result.isLiked())
                .isRecruiting(campaign.getRecruitEndDate().isAfter(LocalDateTime.now()))
                .manuscriptFee(campaign.getRewardAmount().intValue())
                .detail(campaign.getDescription())
                .dDay(Math.max(dDay, 0))
                .totalRecruit(campaign.getQuota())
                .currentRecruit(result.currentApplyCount())
                .build();
    }

    private MatchBrandResponseDto.BrandDto toMatchBrandDto(BrandMatchResult result, List<String> tags) {
        return MatchBrandResponseDto.BrandDto.builder()
                .id(result.brand().getId())
                .name(result.brand().getBrandName())
                .matchingRatio(result.matchScore())
                .isLiked(result.isLiked())
                .isRecruiting(result.isRecruiting())
                .tags(tags.stream().limit(3).toList())
                .build();
    }

    private MatchCampaignResponseDto.CampaignDto toMatchCampaignDto(CampaignMatchResult result) {
        Campaign campaign = result.campaign();
        int dDay = (int) ChronoUnit.DAYS.between(LocalDate.now(), campaign.getRecruitEndDate().toLocalDate());

        return MatchCampaignResponseDto.CampaignDto.builder()
                .id(campaign.getId())
                .name(campaign.getTitle())
                .matchingRatio(result.matchScore())
                .isLiked(result.isLiked())
                .isRecruiting(campaign.getRecruitEndDate().isAfter(LocalDateTime.now()))
                .manuscriptFee(campaign.getRewardAmount().intValue())
                .detail(campaign.getDescription())
                .dDay(Math.max(dDay, 0))
                .totalRecruit(campaign.getQuota())
                .currentRecruit(result.currentApplyCount())
                .build();
    }

    private boolean isRecruitingBrand(Long brandId) {

        return campaignRepository.findByRecruitEndDateAfter(LocalDateTime.now()).stream()
                .anyMatch(c -> {
                    Brand brand = brandRepository.findByCreatedBy(c.getCreatedBy()).orElse(null);
                    return brand != null && brand.getId().equals(brandId);
                });
    }

    private Map<Long, Long> getApplyCountMap() {

        return campaignApplyRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        apply -> apply.getCampaign().getId(),
                        Collectors.counting()
                ));
    }

    private Map<Long, List<String>> getBrandTagsMap() {
        return brandTagRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        bt -> bt.getBrand().getId(),
                        Collectors.mapping(bt -> bt.getTag().getTagName(), Collectors.toList())
                ));
    }

    private String getFirstTag(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return tags.iterator().next();
    }

    private int safeSize(Set<?> set) {
        return set != null ? set.size() : 0;
    }

    private record BrandMatchResult(
            Brand brand,
            BrandTagDocument brandDoc,
            int matchScore,
            boolean isLiked,
            boolean isRecruiting
    ) {}

    private record CampaignMatchResult(
            Campaign campaign,
            CampaignTagDocument campaignDoc,
            int matchScore,
            boolean isLiked,
            int currentApplyCount
    ) {}
}
