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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.entity.BrandDescribeTag;
import com.example.RealMatch.brand.domain.repository.BrandDescribeTagRepository;
import com.example.RealMatch.brand.domain.repository.BrandLikeRepository;
import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.business.domain.repository.CampaignApplyRepository;
import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.campaign.domain.repository.CampaignLikeRepository;
import com.example.RealMatch.campaign.domain.repository.CampaignRepository;
import com.example.RealMatch.match.application.util.MatchScoreCalculator;
import com.example.RealMatch.match.domain.entity.MatchBrandHistory;
import com.example.RealMatch.match.domain.entity.MatchCampaignHistory;
import com.example.RealMatch.match.domain.entity.enums.BrandSortType;
import com.example.RealMatch.match.domain.entity.enums.CampaignSortType;
import com.example.RealMatch.match.domain.entity.enums.CategoryType;
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
import com.example.RealMatch.user.domain.entity.UserMatchingDetail;
import com.example.RealMatch.user.domain.repository.UserMatchingDetailRepository;
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
    private final BrandDescribeTagRepository brandDescribeTagRepository;
    private final CampaignLikeRepository campaignLikeRepository;
    private final CampaignApplyRepository campaignApplyRepository;
    private final UserRepository userRepository;
    private final UserMatchingDetailRepository userMatchingDetailRepository;
    private final MatchBrandHistoryRepository matchBrandHistoryRepository;
    private final MatchCampaignHistoryRepository matchCampaignHistoryRepository;

    // 매칭 요청 //

    /**
     * 매칭 검사는 다음을 하나의 트랜잭션으로 처리한다.
     * - 기존 UserMatchingDetail 폐기
     * - 새 UserMatchingDetail 생성
     * - 브랜드/캠페인 매칭 히스토리 갱신
     */
    @Override
    @Transactional
    public MatchResponseDto match(Long userId, MatchRequestDto requestDto) {
        UserTagDocument userDoc = convertToUserTagDocument(userId, requestDto);

        String userType = determineUserType(userDoc);
        List<String> typeTag = determineTypeTags(userDoc);

        replaceUserMatchingDetail(userId, requestDto, userType);

        List<BrandMatchResult> brandResults = findMatchingBrandResults(userDoc, userId);

        List<Long> brandIds = brandResults.stream()
                .map(result -> result.brandDoc().getBrandId())
                .toList();
        Map<Long, Brand> brandMap = brandRepository.findAllById(brandIds).stream()
                .collect(Collectors.toMap(Brand::getId, brand -> brand));

        List<BrandDto> matchedBrands = brandResults.stream()
                .map(result -> toBrandDto(result, brandMap.get(result.brandDoc().getBrandId())))
                .toList();

        HighMatchingBrandListDto brandListDto = HighMatchingBrandListDto.builder()
                .count(matchedBrands.size())
                .brands(matchedBrands)
                .build();

        List<CampaignMatchResult> campaignResults = findMatchingCampaignResults(userDoc, userId);
        saveMatchHistory(userId, brandResults, campaignResults);

        String username = userRepository.findById(userId)
                .map(User::getName)
                .orElse("사용자");

        return MatchResponseDto.builder()
                .username(username)
                .userType(userType)
                .userTypeImage("https://ui-avatars.com/api/?name=" + userType + "&background=6366f1&color=fff&size=200")
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

        // 기존 매칭 기록을 벌크 UPDATE로 deprecated 처리
        matchBrandHistoryRepository.bulkDeprecateByUserId(userId);
        matchCampaignHistoryRepository.bulkDeprecateByUserId(userId);

        // 새로운 브랜드 매칭 기록 저장 (배치 처리)
        List<Long> brandIds = brandResults.stream()
                .map(result -> result.brandDoc().getBrandId())
                .toList();
        Map<Long, Brand> brandMap = brandRepository.findAllById(brandIds).stream()
                .collect(Collectors.toMap(Brand::getId, brand -> brand));

        List<MatchBrandHistory> brandHistories = brandResults.stream()
                .filter(result -> brandMap.containsKey(result.brandDoc().getBrandId()))
                .map(result -> MatchBrandHistory.builder()
                        .user(user)
                        .brand(brandMap.get(result.brandDoc().getBrandId()))
                        .matchingRatio((long) result.matchScore())
                        .build())
                .toList();
        matchBrandHistoryRepository.saveAll(brandHistories);

        // 새로운 캠페인 매칭 기록 저장 (배치 처리)
        List<Long> campaignIds = campaignResults.stream()
                .map(result -> result.campaignDoc().getCampaignId())
                .toList();
        Map<Long, Campaign> campaignMap = campaignRepository.findAllById(campaignIds).stream()
                .collect(Collectors.toMap(Campaign::getId, campaign -> campaign));

        List<MatchCampaignHistory> campaignHistories = campaignResults.stream()
                .filter(result -> campaignMap.containsKey(result.campaignDoc().getCampaignId()))
                .map(result -> MatchCampaignHistory.builder()
                        .user(user)
                        .campaign(campaignMap.get(result.campaignDoc().getCampaignId()))
                        .matchingRatio((long) result.matchScore())
                        .build())
                .toList();
        matchCampaignHistoryRepository.saveAll(campaignHistories);

        LOG.info("Match history saved. userId={}, brands={}, campaigns={}",
                userId, brandHistories.size(), campaignHistories.size());
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

        List<Long> campaignIds = allCampaignDocs.stream()
                .map(CampaignTagDocument::getCampaignId)
                .toList();
        Map<Long, Long> applyCountMap = getApplyCountMapForCampaignIds(campaignIds);

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


    private Map<Long, Long> getBrandLikeCountMap() {
        return brandLikeRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        like -> like.getBrand().getId(),
                        Collectors.counting()
                ));
    }

    // *********** //
    // 매칭 결과 조회 //
    // *********** //
    @Override
    public MatchBrandResponseDto getMatchingBrands(String userId, BrandSortType sortBy, CategoryType category, List<String> tags) {
        Long userIdLong = Long.parseLong(userId);

        List<MatchBrandHistory> brandHistories = matchBrandHistoryRepository.findByUserIdAndIsDeprecatedFalse(userIdLong);

        if (brandHistories.isEmpty()) {
            LOG.warn("No match brand history found in DB. userId={}", userId);
            return MatchBrandResponseDto.builder()
                    .count(0)
                    .brands(List.of())
                    .build();
        }

        Set<Long> likedBrandIds = brandLikeRepository.findByUserId(userIdLong).stream()
                .map(like -> like.getBrand().getId())
                .collect(Collectors.toSet());

        Set<Long> recruitingBrandIds = getRecruitingBrandIds();
        Map<Long, Long> brandLikeCountMap = getBrandLikeCountMap();

        // BrandDescribeTag 조회하여 Map으로 변환
        List<Long> brandIds = brandHistories.stream()
                .map(h -> h.getBrand().getId())
                .toList();
        Map<Long, List<String>> brandDescribeTagMap = brandIds.stream()
                .collect(Collectors.toMap(
                        brandId -> brandId,
                        brandId -> brandDescribeTagRepository.findAllByBrandId(brandId).stream()
                                .map(BrandDescribeTag::getBrandDescribeTag)
                                .toList()
                ));

        List<MatchBrandResponseDto.BrandDto> matchedBrands = brandHistories.stream()
                .filter(history -> filterBrandByCategory(history.getBrand(), category))
                .sorted(getBrandHistoryComparator(sortBy, brandLikeCountMap))
                .limit(TOP_MATCH_COUNT)
                .map(history -> toMatchBrandDtoFromHistory(history, likedBrandIds, recruitingBrandIds, brandDescribeTagMap))
                .toList();

        return MatchBrandResponseDto.builder()
                .count(matchedBrands.size())
                .brands(matchedBrands)
                .build();
    }

    @Override
    public MatchCampaignResponseDto getMatchingCampaigns(
            String userId,
            String keyword,
            CampaignSortType sortBy,
            CategoryType category,
            List<String> tags,
            int page,
            int size
    ) {
        Long userIdLong = Long.parseLong(userId);

        PageRequest pageable = PageRequest.of(page, size);
        Page<MatchCampaignHistory> historyPage = matchCampaignHistoryRepository
                .searchCampaigns(userIdLong, keyword, category, sortBy, tags, pageable);

        if (historyPage.isEmpty()) {
            LOG.info("No match campaign history found for user. userId={}", userId);
            return MatchCampaignResponseDto.empty();
        }

        Set<Long> likedCampaignIds = campaignLikeRepository.findByUserId(userIdLong).stream()
                .map(like -> like.getCampaign().getId())
                .collect(Collectors.toSet());

        List<Long> pageCampaignIds = historyPage.getContent().stream()
                .map(h -> h.getCampaign().getId())
                .toList();
        Map<Long, Long> applyCountMap = getApplyCountMapForCampaignIds(pageCampaignIds);

        List<MatchCampaignResponseDto.CampaignDto> brands = historyPage.getContent().stream()
                .map(h -> toCampaignCardDto(h, likedCampaignIds, applyCountMap))
                .toList();

        return MatchCampaignResponseDto.builder()
                .brands(brands)
                .count((int) historyPage.getTotalElements())
                .build();
    }

    private BrandDto toBrandDto(BrandMatchResult result, Brand brand) {
        return BrandDto.builder()
                .brandId(result.brandDoc().getBrandId())
                .brandName(result.brandDoc().getBrandName())
                .logoUrl(brand != null ? brand.getLogoUrl() : null)
                .matchingRatio(result.matchScore())
                .build();
    }

    private Set<Long> getRecruitingBrandIds() {
        return campaignRepository.findRecruitingBrandIds(LocalDateTime.now());
    }

    /**
     * 지정한 캠페인 ID별 지원 건수 (현재 페이지/대상 목록만 조회).
     */
    private Map<Long, Long> getApplyCountMapForCampaignIds(List<Long> campaignIds) {
        if (campaignIds == null || campaignIds.isEmpty()) {
            return Map.of();
        }
        return campaignApplyRepository.countByCampaignIdIn(campaignIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    private int safeSize(Set<?> set) {
        return set != null ? set.size() : 0;
    }

    // ************************* //
    // DB History 기반 헬퍼 메서드 //
    // ************************* //
    private boolean filterBrandByCategory(Brand brand, CategoryType category) {
        if (category == null || category == CategoryType.ALL) {
            return true;
        }
        if (brand.getIndustryType() == null) {
            return true;
        }
        return brand.getIndustryType().name().equalsIgnoreCase(category.name());
    }

    private Comparator<MatchBrandHistory> getBrandHistoryComparator(BrandSortType sortBy, Map<Long, Long> likeCountMap) {
        return switch (sortBy) {
            case POPULARITY -> Comparator.comparingLong(
                    (MatchBrandHistory h) -> likeCountMap.getOrDefault(h.getBrand().getId(), 0L)
            ).reversed();
            case NEWEST -> Comparator.comparingLong(
                    (MatchBrandHistory h) -> h.getBrand().getId()
            ).reversed();
            default -> Comparator.comparingLong(
                    (MatchBrandHistory h) -> h.getMatchingRatio() != null ? h.getMatchingRatio() : 0L
            ).reversed();
        };
    }

    private MatchBrandResponseDto.BrandDto toMatchBrandDtoFromHistory(
            MatchBrandHistory history, Set<Long> likedBrandIds, Set<Long> recruitingBrandIds,
            Map<Long, List<String>> brandDescribeTagMap) {
        Brand brand = history.getBrand();
        return MatchBrandResponseDto.BrandDto.builder()
                .brandId(brand.getId())
                .brandName(brand.getBrandName())
                .brandLogoUrl(brand.getLogoUrl())
                .brandMatchingRatio(history.getMatchingRatio() != null ? history.getMatchingRatio().intValue() : 0)
                .brandIsLiked(likedBrandIds.contains(brand.getId()))
                .brandIsRecruiting(recruitingBrandIds.contains(brand.getId()))
                .brandTags(brandDescribeTagMap.getOrDefault(brand.getId(), List.of()))
                .build();
    }

    /**
     * 캠페인 검색/목록 결과용 DTO 변환
     */
    private MatchCampaignResponseDto.CampaignDto toCampaignCardDto(
            MatchCampaignHistory history,
            Set<Long> likedCampaignIds,
            Map<Long, Long> applyCountMap
    ) {
        Campaign campaign = history.getCampaign();
        Brand brand = campaign.getBrand();
        int dDay = campaign.getRecruitEndDate() != null
                ? (int) ChronoUnit.DAYS.between(LocalDate.now(), campaign.getRecruitEndDate().toLocalDate())
                : 0;
        boolean isRecruiting = campaign.getRecruitEndDate() == null
                || campaign.getRecruitEndDate().isAfter(LocalDateTime.now());
        Integer matchRatio = history.getMatchingRatio() != null ? history.getMatchingRatio().intValue() : 0;
        return MatchCampaignResponseDto.CampaignDto.builder()
                .brandId(brand != null ? brand.getId() : null)
                .brandName(brand != null ? brand.getBrandName() : null)
                .brandLogoUrl(brand != null ? brand.getLogoUrl() : null)
                .brandMatchingRatio(matchRatio)
                .brandIsLiked(likedCampaignIds.contains(campaign.getId()))
                .brandIsRecruiting(isRecruiting)
                .campaignId(campaign.getId())
                .campaignManuscriptFee(campaign.getRewardAmount() != null ? campaign.getRewardAmount().intValue() : null)
                .campaignName(campaign.getTitle())
                .campaignDDay(Math.max(dDay, 0))
                .campaignIsLiked(likedCampaignIds.contains(campaign.getId()))
                .campaignTotalRecruit(campaign.getQuota())
                .campaignTotalCurrentRecruit(applyCountMap.getOrDefault(campaign.getId(), 0L).intValue())
                .build();
    }

    /**
     * 사용자의 마지막 매칭 상세 정보를 저장합니다 By 고경수
     */
    private void replaceUserMatchingDetail(Long userId, MatchRequestDto requestDto, String userType) {

        // 1) 기존 활성 detail 폐기
        userMatchingDetailRepository.findByUserIdAndIsDeprecatedFalse(userId)
                .ifPresent(detail -> {
                    detail.deprecated();
                    userMatchingDetailRepository.save(detail); // 명시적으로
                });

        // 2) 새 detail 생성 (requestDto → entity 매핑)
        UserMatchingDetail newDetail = UserMatchingDetail.from(userId, requestDto);

        // 3) 결과 저장(원하면)
        newDetail.setMatchingResult(userType);

        // 4) 저장
        userMatchingDetailRepository.save(newDetail);
    }

    private record BrandMatchResult(
            BrandTagDocument brandDoc,
            int matchScore,
            boolean isLiked,
            boolean isRecruiting
    ) {
    }

    private record CampaignMatchResult(
            CampaignTagDocument campaignDoc,
            int matchScore,
            boolean isLiked,
            int currentApplyCount
    ) {
    }
}
