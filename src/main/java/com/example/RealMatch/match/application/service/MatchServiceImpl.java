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
import com.example.RealMatch.match.domain.entity.enums.CategoryType;
import com.example.RealMatch.match.domain.entity.enums.SortType;
import com.example.RealMatch.match.infrastructure.redis.RedisDocumentHelper;
import com.example.RealMatch.match.infrastructure.redis.document.BrandTagDocument;
import com.example.RealMatch.match.infrastructure.redis.document.CampaignTagDocument;
import com.example.RealMatch.match.infrastructure.redis.document.UserTagDocument;
import com.example.RealMatch.match.presentation.dto.request.MatchRequestDto;
import com.example.RealMatch.match.presentation.dto.response.MatchBrandResponseDto;
import com.example.RealMatch.match.presentation.dto.response.MatchCampaignResponseDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto.BrandDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto.HighMatchingBrandListDto;
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

        String userType = determineUserType(userDoc);
        List<String> typeTag = determineTypeTags(userDoc);

        List<BrandMatchResult> brandResults = findMatchingBrandResults(userDoc, userId);
        List<BrandDto> matchedBrands = brandResults.stream()
                .map(this::toBrandDto)
                .toList();

        HighMatchingBrandListDto brandListDto = HighMatchingBrandListDto.builder()
                .count(matchedBrands.size())
                .brands(matchedBrands)
                .build();

        List<CampaignMatchResult> campaignResults = findMatchingCampaignResults(userDoc, userId);
        saveMatchHistory(userId, brandResults, campaignResults);

        return MatchResponseDto.builder()
                .userType(userType)
                .typeTag(typeTag)
                .highMatchingBrandList(brandListDto)
                .build();
    }

    private void saveMatchHistory(Long userId, List<BrandMatchResult> brandResults, List<CampaignMatchResult> campaignResults) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            LOG.warn("User not found for saving match history. userId={}", userId);
            return;
        }

        for (BrandMatchResult result : brandResults) {
            Long brandId = result.brandDoc().getBrandId();
            if (!matchBrandHistoryRepository.existsByUserIdAndBrandId(userId, brandId)) {
                Brand brand = brandRepository.findById(brandId).orElse(null);
                if (brand != null) {
                    MatchBrandHistory history = MatchBrandHistory.builder()
                            .user(user)
                            .brand(brand)
                            .build();
                    matchBrandHistoryRepository.save(history);
                }
            }
        }

        for (CampaignMatchResult result : campaignResults) {
            Long campaignId = result.campaignDoc().getCampaignId();
            if (!matchCampaignHistoryRepository.existsByUserIdAndCampaignId(userId, campaignId)) {
                Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
                if (campaign != null) {
                    MatchCampaignHistory history = MatchCampaignHistory.builder()
                            .user(user)
                            .campaign(campaign)
                            .build();
                    matchCampaignHistoryRepository.save(history);
                }
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

    private String determineUserType(UserTagDocument userDoc) {
        int fashionCount = safeSize(userDoc.getFashionTags());
        int beautyCount = safeSize(userDoc.getBeautyTags());
        int contentCount = safeSize(userDoc.getContentTags());

        if (fashionCount >= beautyCount && fashionCount >= contentCount) {
            return "유연한 연출가";
        } else if (beautyCount >= contentCount) {
            return "트렌드 리더";
        } else {
            return "콘텐츠 크리에이터";
        }
    }

    private List<String> determineTypeTags(UserTagDocument userDoc) {
        List<String> tags = new ArrayList<>();

        int fashionCount = safeSize(userDoc.getFashionTags());
        int beautyCount = safeSize(userDoc.getBeautyTags());
        int contentCount = safeSize(userDoc.getContentTags());

        if (fashionCount > 0) {
            tags.add("연출 유연");
        }
        if (beautyCount > 0 || contentCount > 0) {
            tags.add("트렌드 적용");
        }
        tags.add("브랜드 이해도");

        return tags.stream().limit(3).toList();
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

        Set<Long> recruitingBrandIds = getRecruitingBrandIds();

        return allBrandDocs.stream()
                .map(brandDoc -> new BrandMatchResult(
                        brandDoc,
                        MatchScoreCalculator.calculateBrandMatchScore(userDoc, brandDoc),
                        likedBrandIds.contains(brandDoc.getBrandId()),
                        recruitingBrandIds.contains(brandDoc.getBrandId())
                ))
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

        Map<Long, Long> applyCountMap = getApplyCountMap();

        return allCampaignDocs.stream()
                .filter(campaignDoc -> campaignDoc.getRecruitEndDate() == null
                        || campaignDoc.getRecruitEndDate().isAfter(LocalDateTime.now()))
                .map(campaignDoc -> new CampaignMatchResult(
                        campaignDoc,
                        MatchScoreCalculator.calculateCampaignMatchScore(userDoc, campaignDoc),
                        likedCampaignIds.contains(campaignDoc.getCampaignId()),
                        applyCountMap.getOrDefault(campaignDoc.getCampaignId(), 0L).intValue()
                ))
                .sorted(Comparator.comparingInt(CampaignMatchResult::matchScore).reversed())
                .limit(TOP_MATCH_COUNT)
                .toList();
    }
    
    private List<MatchBrandResponseDto.BrandDto> findMatchingBrandsForList(
            UserTagDocument userDoc, Long userId, SortType sortBy, CategoryType category, List<String> filterTags) {
        List<BrandTagDocument> allBrandDocs = redisDocumentHelper.findAllBrandTagDocuments();

        if (allBrandDocs.isEmpty()) {
            return List.of();
        }

        Set<Long> likedBrandIds = brandLikeRepository.findByUserId(userId).stream()
                .map(like -> like.getBrand().getId())
                .collect(Collectors.toSet());

        Set<Long> recruitingBrandIds = getRecruitingBrandIds();
        Map<Long, Long> brandLikeCountMap = getBrandLikeCountMap();

        return allBrandDocs.stream()
                .filter(brandDoc -> filterByCategory(brandDoc, category))
                .filter(brandDoc -> filterByTags(brandDoc, filterTags))
                .map(brandDoc -> new BrandMatchResult(
                        brandDoc,
                        MatchScoreCalculator.calculateBrandMatchScore(userDoc, brandDoc),
                        likedBrandIds.contains(brandDoc.getBrandId()),
                        recruitingBrandIds.contains(brandDoc.getBrandId())
                ))
                .sorted(getBrandComparator(sortBy, brandLikeCountMap))
                .limit(TOP_MATCH_COUNT)
                .map(this::toMatchBrandDto)
                .toList();
    }

    private List<MatchCampaignResponseDto.CampaignDto> findMatchingCampaignsForList(
            UserTagDocument userDoc, Long userId, SortType sortBy, CategoryType category, List<String> filterTags) {
        List<CampaignTagDocument> allCampaignDocs = redisDocumentHelper.findAllCampaignTagDocuments();

        if (allCampaignDocs.isEmpty()) {
            return List.of();
        }

        Set<Long> likedCampaignIds = campaignLikeRepository.findByUserId(userId).stream()
                .map(like -> like.getCampaign().getId())
                .collect(Collectors.toSet());

        Map<Long, Long> applyCountMap = getApplyCountMap();
        Map<Long, Long> campaignLikeCountMap = getCampaignLikeCountMap();

        return allCampaignDocs.stream()
                .filter(campaignDoc -> campaignDoc.getRecruitEndDate() == null
                        || campaignDoc.getRecruitEndDate().isAfter(LocalDateTime.now()))
                .filter(campaignDoc -> filterByCategoryCampaign(campaignDoc, category))
                .filter(campaignDoc -> filterByTagsCampaign(campaignDoc, filterTags))
                .map(campaignDoc -> new CampaignMatchResult(
                        campaignDoc,
                        MatchScoreCalculator.calculateCampaignMatchScore(userDoc, campaignDoc),
                        likedCampaignIds.contains(campaignDoc.getCampaignId()),
                        applyCountMap.getOrDefault(campaignDoc.getCampaignId(), 0L).intValue()
                ))
                .sorted(getCampaignComparator(sortBy, campaignLikeCountMap))
                .limit(TOP_MATCH_COUNT)
                .map(this::toMatchCampaignDto)
                .toList();
    }

    // ************* //
    // 필터링 메서드 //
    // ************* //
    private boolean filterByCategory(BrandTagDocument brandDoc, CategoryType category) {
        if (category == null || category == CategoryType.ALL) {
            return true;
        }
        Set<String> categories = brandDoc.getCategories();
        if (categories == null || categories.isEmpty()) {
            return true;
        }
        return categories.contains(category.name());
    }

    private boolean filterByTags(BrandTagDocument brandDoc, List<String> filterTags) {
        if (filterTags == null || filterTags.isEmpty()) {
            return true;
        }
        Set<String> allTags = new HashSet<>();
        if (brandDoc.getPreferredFashionTags() != null) {
            allTags.addAll(brandDoc.getPreferredFashionTags());
        }
        if (brandDoc.getPreferredBeautyTags() != null) {
            allTags.addAll(brandDoc.getPreferredBeautyTags());
        }
        return filterTags.stream().anyMatch(allTags::contains);
    }

    private boolean filterByCategoryCampaign(CampaignTagDocument campaignDoc, CategoryType category) {
        if (category == null || category == CategoryType.ALL) {
            return true;
        }
        Set<String> categories = campaignDoc.getCategories();
        if (categories == null || categories.isEmpty()) {
            return true;
        }
        return categories.contains(category.name());
    }

    private boolean filterByTagsCampaign(CampaignTagDocument campaignDoc, List<String> filterTags) {
        if (filterTags == null || filterTags.isEmpty()) {
            return true;
        }
        Set<String> allTags = new HashSet<>();
        if (campaignDoc.getPreferredFashionTags() != null) {
            allTags.addAll(campaignDoc.getPreferredFashionTags());
        }
        if (campaignDoc.getPreferredBeautyTags() != null) {
            allTags.addAll(campaignDoc.getPreferredBeautyTags());
        }
        return filterTags.stream().anyMatch(allTags::contains);
    }

    // ************* //
    // 정렬 메서드 //
    // ************* //
    private Comparator<BrandMatchResult> getBrandComparator(SortType sortBy, Map<Long, Long> likeCountMap) {
        return switch (sortBy) {
            case POPULARITY -> Comparator.comparingLong(
                    (BrandMatchResult r) -> likeCountMap.getOrDefault(r.brandDoc().getBrandId(), 0L)
            ).reversed();
            case NEWEST -> Comparator.comparingLong(
                    (BrandMatchResult r) -> r.brandDoc().getBrandId()
            ).reversed();
            default -> Comparator.comparingInt(BrandMatchResult::matchScore).reversed();
        };
    }

    private Comparator<CampaignMatchResult> getCampaignComparator(SortType sortBy, Map<Long, Long> likeCountMap) {
        return switch (sortBy) {
            case POPULARITY -> Comparator.comparingLong(
                    (CampaignMatchResult r) -> likeCountMap.getOrDefault(r.campaignDoc().getCampaignId(), 0L)
            ).reversed();
            case NEWEST -> Comparator.comparingLong(
                    (CampaignMatchResult r) -> r.campaignDoc().getCampaignId()
            ).reversed();
            default -> Comparator.comparingInt(CampaignMatchResult::matchScore).reversed();
        };
    }

    private Map<Long, Long> getBrandLikeCountMap() {
        return brandLikeRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        like -> like.getBrand().getId(),
                        Collectors.counting()
                ));
    }

    private Map<Long, Long> getCampaignLikeCountMap() {
        return campaignLikeRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        like -> like.getCampaign().getId(),
                        Collectors.counting()
                ));
    }

    // *********** //
    // 매칭 결과 조회 //
    // *********** //
    @Override
    public MatchBrandResponseDto getMatchingBrands(String userId, SortType sortBy, CategoryType category, List<String> tags) {
        Long userIdLong = Long.parseLong(userId);

        UserTagDocument userDoc = redisDocumentHelper.findUserTagDocumentById(userIdLong);

        if (userDoc == null) {
            LOG.warn("User tag document not found in Redis. userId={}", userId);
            return MatchBrandResponseDto.builder()
                    .count(0)
                    .brands(List.of())
                    .build();
        }

        List<MatchBrandResponseDto.BrandDto> matchedBrands = findMatchingBrandsForList(userDoc, userIdLong, sortBy, category, tags);

        return MatchBrandResponseDto.builder()
                .count(matchedBrands.size())
                .brands(matchedBrands)
                .build();
    }

    @Override
    public MatchCampaignResponseDto getMatchingCampaigns(String userId, SortType sortBy, CategoryType category, List<String> tags) {
        Long userIdLong = Long.parseLong(userId);

        UserTagDocument userDoc = redisDocumentHelper.findUserTagDocumentById(userIdLong);

        if (userDoc == null) {
            LOG.warn("User tag document not found in Redis. userId={}", userId);
            return MatchCampaignResponseDto.builder()
                    .count(0)
                    .brands(List.of())
                    .build();
        }

        List<MatchCampaignResponseDto.CampaignDto> matchedCampaigns = findMatchingCampaignsForList(userDoc, userIdLong, sortBy, category, tags);

        return MatchCampaignResponseDto.builder()
                .count(matchedCampaigns.size())
                .brands(matchedCampaigns)
                .build();
    }

    private BrandDto toBrandDto(BrandMatchResult result) {
        return BrandDto.builder()
                .brandId(result.brandDoc().getBrandId())
                .brandName(result.brandDoc().getBrandName())
                .matchingRatio(result.matchScore())
                .build();
    }

    private MatchBrandResponseDto.BrandDto toMatchBrandDto(BrandMatchResult result) {
        List<String> tags = new ArrayList<>();
        if (result.brandDoc().getPreferredFashionTags() != null) {
            tags.addAll(result.brandDoc().getPreferredFashionTags());
        }
        if (result.brandDoc().getPreferredBeautyTags() != null) {
            tags.addAll(result.brandDoc().getPreferredBeautyTags());
        }

        return MatchBrandResponseDto.BrandDto.builder()
                .brandId(result.brandDoc().getBrandId())
                .brandName(result.brandDoc().getBrandName())
                .brandMatchingRatio(result.matchScore())
                .brandIsLiked(result.isLiked())
                .brandIsRecruiting(result.isRecruiting())
                .brandTags(tags.stream().limit(3).toList())
                .build();
    }

    private MatchCampaignResponseDto.CampaignDto toMatchCampaignDto(CampaignMatchResult result) {
        CampaignTagDocument campaignDoc = result.campaignDoc();
        int dDay = campaignDoc.getRecruitEndDate() != null
                ? (int) ChronoUnit.DAYS.between(LocalDate.now(), campaignDoc.getRecruitEndDate().toLocalDate())
                : 0;

        return MatchCampaignResponseDto.CampaignDto.builder()
                .brandId(campaignDoc.getCampaignId())
                .brandName(campaignDoc.getCampaignName())
                .brandMatchingRatio(result.matchScore())
                .brandIsLiked(result.isLiked())
                .brandIsRecruiting(campaignDoc.getRecruitEndDate() == null
                        || campaignDoc.getRecruitEndDate().isAfter(LocalDateTime.now()))
                .campaignManuscriptFee(campaignDoc.getRewardAmount() != null
                        ? campaignDoc.getRewardAmount().intValue() : 0)
                .campaignDetail(campaignDoc.getDescription())
                .campaignDDay(Math.max(dDay, 0))
                .campaignTotalRecruit(campaignDoc.getQuota())
                .campaignTotalCurrentRecruit(result.currentApplyCount())
                .build();
    }

    private Set<Long> getRecruitingBrandIds() {
        return campaignRepository.findByRecruitEndDateAfter(LocalDateTime.now()).stream()
                .map(c -> brandRepository.findByCreatedBy(c.getCreatedBy()).orElse(null))
                .filter(brand -> brand != null)
                .map(Brand::getId)
                .collect(Collectors.toSet());
    }

    private Map<Long, Long> getApplyCountMap() {

        return campaignApplyRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        apply -> apply.getCampaign().getId(),
                        Collectors.counting()
                ));
    }
    private int safeSize(Set<?> set) {
        return set != null ? set.size() : 0;
    }

    private record BrandMatchResult(
            BrandTagDocument brandDoc,
            int matchScore,
            boolean isLiked,
            boolean isRecruiting
    ) {}

    private record CampaignMatchResult(
            CampaignTagDocument campaignDoc,
            int matchScore,
            boolean isLiked,
            int currentApplyCount
    ) {}
}
