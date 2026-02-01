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
import com.example.RealMatch.match.domain.entity.enums.CategoryType;
import com.example.RealMatch.match.domain.entity.enums.SortType;
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
    public MatchResponseDto match(Long userId, MatchRequestDto requestDto) {
        UserTagDocument userDoc = convertToUserTagDocument(userId, requestDto);

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
    private UserTagDocument convertToUserTagDocument(Long userId, MatchRequestDto dto) {
        Set<Integer> fashionTags = new HashSet<>();
        Set<Integer> beautyTags = new HashSet<>();
        Set<Integer> contentTags = new HashSet<>();

        // Fashion 태그 수집
        if (dto.getFashion() != null) {
            if (dto.getFashion().getInterestStyleTags() != null) {
                fashionTags.addAll(dto.getFashion().getInterestStyleTags());
            }
            if (dto.getFashion().getPreferredItemTags() != null) {
                fashionTags.addAll(dto.getFashion().getPreferredItemTags());
            }
            if (dto.getFashion().getPreferredBrandTags() != null) {
                fashionTags.addAll(dto.getFashion().getPreferredBrandTags());
            }
        }

        // Beauty 태그 수집
        if (dto.getBeauty() != null) {
            if (dto.getBeauty().getInterestStyleTags() != null) {
                beautyTags.addAll(dto.getBeauty().getInterestStyleTags());
            }
            if (dto.getBeauty().getPrefferedFunctionTags() != null) {
                beautyTags.addAll(dto.getBeauty().getPrefferedFunctionTags());
            }
            if (dto.getBeauty().getSkinTypeTags() != null) {
                beautyTags.add(dto.getBeauty().getSkinTypeTags());
            }
            if (dto.getBeauty().getSkinToneTags() != null) {
                beautyTags.add(dto.getBeauty().getSkinToneTags());
            }
            if (dto.getBeauty().getMakeupStyleTags() != null) {
                beautyTags.add(dto.getBeauty().getMakeupStyleTags());
            }
        }

        // Content 태그 수집
        if (dto.getContent() != null) {
            if (dto.getContent().getTypeTags() != null) {
                contentTags.addAll(dto.getContent().getTypeTags());
            }
            if (dto.getContent().getToneTags() != null) {
                contentTags.addAll(dto.getContent().getToneTags());
            }
            if (dto.getContent().getPrefferedInvolvementTags() != null) {
                contentTags.addAll(dto.getContent().getPrefferedInvolvementTags());
            }
            if (dto.getContent().getPrefferedCoverageTags() != null) {
                contentTags.addAll(dto.getContent().getPrefferedCoverageTags());
            }
        }

        // Fashion 관련 필드
        Integer heightTag = null;
        Integer bodyTypeTag = null;
        Integer topSizeTag = null;
        Integer bottomSizeTag = null;
        if (dto.getFashion() != null) {
            heightTag = dto.getFashion().getHeightTag();
            bodyTypeTag = dto.getFashion().getWeightTypeTag();
            topSizeTag = dto.getFashion().getTopSizeTag();
            bottomSizeTag = dto.getFashion().getBottomSizeTag();
        }

        // Content SNS 관련 필드
        Set<Integer> contentsAgeTags = new HashSet<>();
        Set<Integer> contentsGenderTags = new HashSet<>();
        Set<Integer> contentsLengthTags = new HashSet<>();
        Set<Integer> averageContentsViewsTags = new HashSet<>();

        if (dto.getContent() != null && dto.getContent().getSns() != null) {
            MatchRequestDto.SnsDto sns = dto.getContent().getSns();

            if (sns.getMainAudience() != null) {
                if (sns.getMainAudience().getAgeTags() != null) {
                    contentsAgeTags.addAll(sns.getMainAudience().getAgeTags());
                }
                if (sns.getMainAudience().getGenderTags() != null) {
                    contentsGenderTags.addAll(sns.getMainAudience().getGenderTags());
                }
            }

            if (sns.getAverageAudience() != null) {
                if (sns.getAverageAudience().getVideoLengthTags() != null) {
                    contentsLengthTags.addAll(sns.getAverageAudience().getVideoLengthTags());
                }
                if (sns.getAverageAudience().getVideoViewsTags() != null) {
                    averageContentsViewsTags.addAll(sns.getAverageAudience().getVideoViewsTags());
                }
            }
        }

        return UserTagDocument.builder()
                .userId(userId)
                .fashionTags(fashionTags)
                .beautyTags(beautyTags)
                .contentTags(contentTags)
                .heightTag(heightTag)
                .bodyTypeTag(bodyTypeTag)
                .topSizeTag(topSizeTag)
                .bottomSizeTag(bottomSizeTag)
                .averageContentsViewsTags(averageContentsViewsTags)
                .contentsAgeTags(contentsAgeTags)
                .contentsGenderTags(contentsGenderTags)
                .contentsLengthTags(contentsLengthTags)
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
        Set<Integer> allTags = new HashSet<>();
        if (brandDoc.getPreferredFashionTags() != null) {
            allTags.addAll(brandDoc.getPreferredFashionTags());
        }
        if (brandDoc.getPreferredBeautyTags() != null) {
            allTags.addAll(brandDoc.getPreferredBeautyTags());
        }
        return filterTags.stream()
                .map(tag -> {
                    try {
                        return Integer.parseInt(tag);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(tag -> tag != null)
                .anyMatch(allTags::contains);
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
        Set<Integer> allTags = new HashSet<>();
        if (campaignDoc.getPreferredFashionTags() != null) {
            allTags.addAll(campaignDoc.getPreferredFashionTags());
        }
        if (campaignDoc.getPreferredBeautyTags() != null) {
            allTags.addAll(campaignDoc.getPreferredBeautyTags());
        }
        return filterTags.stream()
                .map(tag -> {
                    try {
                        return Integer.parseInt(tag);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(tag -> tag != null)
                .anyMatch(allTags::contains);
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
        List<Integer> tags = new ArrayList<>();
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
                .brandTags(tags.stream().limit(3).map(String::valueOf).toList())
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
