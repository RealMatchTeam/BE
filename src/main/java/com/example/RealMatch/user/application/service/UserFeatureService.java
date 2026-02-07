package com.example.RealMatch.user.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.match.application.service.MatchService;
import com.example.RealMatch.match.presentation.dto.request.MatchRequestDto;
import com.example.RealMatch.tag.domain.entity.Tag;
import com.example.RealMatch.tag.domain.entity.UserTag;
import com.example.RealMatch.tag.domain.enums.ContentTagType;
import com.example.RealMatch.tag.domain.enums.TagCategory;
import com.example.RealMatch.tag.domain.enums.TagType;
import com.example.RealMatch.tag.domain.repository.UserTagRepository;
import com.example.RealMatch.user.domain.entity.UserMatchingDetail;
import com.example.RealMatch.user.domain.repository.UserMatchingDetailRepository;
import com.example.RealMatch.user.presentation.code.UserErrorCode;
import com.example.RealMatch.user.presentation.dto.response.MyFeatureResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFeatureService {

    private final UserTagRepository userTagRepository;
    private final UserMatchingDetailRepository userMatchingDetailRepository;
    private final MatchService matchService;

    /**
     *  내 특성 조회
     * - UserTag에서 tagType/tagCategory 기반으로 그룹핑해서 반환
     * - "키"는 현재 DB에서 tag_category="키" 로 태그화되어 있으므로 그대로 태그로 조회
     */
    public MyFeatureResponseDto getMyFeatures(Long userId) {
        List<UserTag> userTags = userTagRepository.findAllByUserIdWithTag(userId);
        log.info("userId={}, userTags.size={}", userId, userTags.size());

        // (tagType|tagCategory) -> tagId 리스트
        Map<String, List<Integer>> grouped = groupTagIds(userTags);

        // Beauty
        MyFeatureResponseDto.BeautyType beautyType = new MyFeatureResponseDto.BeautyType(
                get(grouped, TagType.BEAUTY.getDescription(), TagCategory.BEAUTY_SKIN_TYPE.getDescription()),
                get(grouped, TagType.BEAUTY.getDescription(), TagCategory.BEAUTY_SKIN_BRIGHTNESS.getDescription()),
                get(grouped, TagType.BEAUTY.getDescription(), TagCategory.BEAUTY_MAKEUP_STYLE.getDescription()),
                get(grouped, TagType.BEAUTY.getDescription(), TagCategory.BEAUTY_INTEREST_STYLE.getDescription()),
                get(grouped, TagType.BEAUTY.getDescription(), TagCategory.BEAUTY_INTEREST_FUNCTION.getDescription())
        );

        // Fashion
        MyFeatureResponseDto.FashionType fashionType = new MyFeatureResponseDto.FashionType(
                get(grouped, TagType.FASHION.getDescription(), TagCategory.FASHION_BODY_HEIGHT.getDescription()),
                get(grouped, TagType.FASHION.getDescription(), TagCategory.FASHION_BODY_WEIGHT.getDescription()),
                get(grouped, TagType.FASHION.getDescription(), TagCategory.FASHION_BODY_TOP.getDescription()),
                get(grouped, TagType.FASHION.getDescription(), TagCategory.FASHION_BODY_BOTTOM.getDescription()),
                get(grouped, TagType.FASHION.getDescription(), TagCategory.FASHION_INTEREST_ITEM.getDescription()),
                get(grouped, TagType.FASHION.getDescription(), TagCategory.FASHION_INTEREST_STYLE.getDescription()),
                get(grouped, TagType.FASHION.getDescription(), TagCategory.FASHION_INTEREST_TYPE.getDescription())
        );

        // Content
        MyFeatureResponseDto.ContentsType contentsType = new MyFeatureResponseDto.ContentsType(
                get(grouped, TagType.CONTENT.getDescription(), ContentTagType.VIEWER_GENDER.getKorName()),
                get(grouped, TagType.CONTENT.getDescription(), ContentTagType.VIEWER_AGE.getKorName()),
                get(grouped, TagType.CONTENT.getDescription(), ContentTagType.AVG_VIDEO_LENGTH.getKorName()),
                get(grouped, TagType.CONTENT.getDescription(), ContentTagType.AVG_VIDEO_VIEWS.getKorName()),
                get(grouped, TagType.CONTENT.getDescription(), ContentTagType.FORMAT.getKorName()),
                get(grouped, TagType.CONTENT.getDescription(), ContentTagType.TONE.getKorName()),
                get(grouped, TagType.CONTENT.getDescription(), ContentTagType.INVOLVEMENT.getKorName()),
                get(grouped, TagType.CONTENT.getDescription(), ContentTagType.USAGE_RANGE.getKorName())
        );

        return new MyFeatureResponseDto(beautyType, fashionType, contentsType);
    }

    /**
     *  내 특성 수정 (PATCH)
     * - 현재 UserTag들을 MatchRequestDto로 복원
     * - patchRequest와 머지 후 match 재실행
     */
    @Transactional
    public void updateMyFeatures(Long userId, MatchRequestDto patchRequest) {
        if (patchRequest == null) {
            throw new CustomException(UserErrorCode.TRAIT_UPDATE_FAILED);
        }

        List<UserTag> existingUserTags = userTagRepository.findAllByUserIdWithTag(userId);
        MatchRequestDto currentRequest = toMatchRequestDtoFromUserTags(existingUserTags, userId);
        MatchRequestDto merged = mergeMatchRequest(currentRequest, patchRequest);

        matchService.match(userId, merged);
        log.info("특성 PATCH 후 매칭 재실행 완료: userId={}", userId);
    }

    // =====================================================
    // 그룹핑 헬퍼 (TagServiceImpl 스타일)
    // =====================================================

    private static Map<String, List<Integer>> groupTagIds(List<UserTag> userTags) {
        if (userTags == null || userTags.isEmpty()) {
            return Map.of();
        }

        return userTags.stream()
                .map(UserTag::getTag)
                .filter(t -> t != null && !t.isDeleted())
                .filter(t -> t.getTagType() != null && t.getTagCategory() != null)
                .collect(Collectors.groupingBy(
                        t -> key(t.getTagType(), t.getTagCategory()),
                        Collectors.mapping(t -> t.getId().intValue(), Collectors.toList())
                ));
    }

    private static String key(String tagType, String tagCategory) {
        return tagType + "|" + tagCategory;
    }

    private static List<Integer> get(Map<String, List<Integer>> grouped, String type, String category) {
        return grouped.getOrDefault(key(type, category), List.of());
    }

    private MatchRequestDto toMatchRequestDtoFromUserTags(List<UserTag> userTags, Long userId) {
        // ===== Beauty =====
        List<Integer> beautyInterestStyleTags = new ArrayList<>();
        List<Integer> beautyPreferredFunctionTags = new ArrayList<>();
        Integer skinTypeTag = null;
        Integer skinBrightnessTag = null;
        Integer makeupStyleTag = null;

        // ===== Fashion =====
        List<Integer> fashionInterestStyleTags = new ArrayList<>();
        List<Integer> fashionPreferredItemTags = new ArrayList<>();
        List<Integer> fashionPreferredBrandTypeTags = new ArrayList<>();
        Integer heightTag = null;
        Integer weightTypeTag = null;
        Integer topSizeTag = null;
        Integer bottomSizeTag = null;

        // ===== Content =====
        List<Integer> genderTags = new ArrayList<>();
        List<Integer> ageTags = new ArrayList<>();
        List<Integer> videoLengthTags = new ArrayList<>();
        List<Integer> videoViewsTags = new ArrayList<>();
        List<Integer> typeTags = new ArrayList<>();
        List<Integer> toneTags = new ArrayList<>();
        List<Integer> preferredInvolvementTags = new ArrayList<>();
        List<Integer> preferredCoverageTags = new ArrayList<>();

        if (userTags != null) {
            for (UserTag ut : userTags) {
                Tag tag = ut.getTag();
                if (tag == null || tag.isDeleted() || tag.getTagType() == null || tag.getTagCategory() == null) {
                    continue;
                }

                String type = tag.getTagType();
                String category = tag.getTagCategory();
                Integer tagId = tag.getId().intValue();

                // ===== Beauty =====
                if (TagType.BEAUTY.getDescription().equals(type)) {
                    if (TagCategory.BEAUTY_SKIN_TYPE.getDescription().equals(category)) {
                        skinTypeTag = tagId;
                    } else if (TagCategory.BEAUTY_SKIN_BRIGHTNESS.getDescription().equals(category)) {
                        skinBrightnessTag = tagId;
                    } else if (TagCategory.BEAUTY_MAKEUP_STYLE.getDescription().equals(category)) {
                        makeupStyleTag = tagId;
                    } else if (TagCategory.BEAUTY_INTEREST_STYLE.getDescription().equals(category)) {
                        beautyInterestStyleTags.add(tagId);
                    } else if (TagCategory.BEAUTY_INTEREST_FUNCTION.getDescription().equals(category)) {
                        beautyPreferredFunctionTags.add(tagId);
                    }
                    continue;
                }

                // ===== Fashion =====
                if (TagType.FASHION.getDescription().equals(type)) {
                    if (TagCategory.FASHION_BODY_HEIGHT.getDescription().equals(category)) {
                        heightTag = tagId;
                    } else if (TagCategory.FASHION_BODY_WEIGHT.getDescription().equals(category)) {
                        weightTypeTag = tagId;
                    } else if (TagCategory.FASHION_BODY_TOP.getDescription().equals(category)) {
                        topSizeTag = tagId;
                    } else if (TagCategory.FASHION_BODY_BOTTOM.getDescription().equals(category)) {
                        bottomSizeTag = tagId;
                    } else if (TagCategory.FASHION_INTEREST_ITEM.getDescription().equals(category)) {
                        fashionPreferredItemTags.add(tagId);
                    } else if (TagCategory.FASHION_INTEREST_STYLE.getDescription().equals(category)) {
                        fashionInterestStyleTags.add(tagId);
                    } else if (TagCategory.FASHION_INTEREST_TYPE.getDescription().equals(category)) {
                        fashionPreferredBrandTypeTags.add(tagId);
                    }
                    continue;
                }

                // ===== Content =====
                if (TagType.CONTENT.getDescription().equals(type)) {
                    if (ContentTagType.VIEWER_GENDER.getKorName().equals(category)) {
                        genderTags.add(tagId);
                    } else if (ContentTagType.VIEWER_AGE.getKorName().equals(category)) {
                        ageTags.add(tagId);
                    } else if (ContentTagType.AVG_VIDEO_LENGTH.getKorName().equals(category)) {
                        videoLengthTags.add(tagId);
                    } else if (ContentTagType.AVG_VIDEO_VIEWS.getKorName().equals(category)) {
                        videoViewsTags.add(tagId);
                    } else if (ContentTagType.FORMAT.getKorName().equals(category)) {
                        typeTags.add(tagId);
                    } else if (ContentTagType.TONE.getKorName().equals(category)) {
                        toneTags.add(tagId);
                    } else if (ContentTagType.INVOLVEMENT.getKorName().equals(category)) {
                        preferredInvolvementTags.add(tagId);
                    } else if (ContentTagType.USAGE_RANGE.getKorName().equals(category)) {
                        preferredCoverageTags.add(tagId);
                    }
                }

            }
        }

        MatchRequestDto.BeautyDto beauty = MatchRequestDto.BeautyDto.builder()
                .interestStyleTags(beautyInterestStyleTags.isEmpty() ? null : beautyInterestStyleTags)
                .prefferedFunctionTags(beautyPreferredFunctionTags.isEmpty() ? null : beautyPreferredFunctionTags)
                .skinTypeTags(skinTypeTag)
                .skinToneTags(skinBrightnessTag)
                .makeupStyleTags(makeupStyleTag)
                .build();

        MatchRequestDto.FashionDto fashion = MatchRequestDto.FashionDto.builder()
                .interestStyleTags(fashionInterestStyleTags.isEmpty() ? null : fashionInterestStyleTags)
                .preferredItemTags(fashionPreferredItemTags.isEmpty() ? null : fashionPreferredItemTags)
                .preferredBrandTags(fashionPreferredBrandTypeTags.isEmpty() ? null : fashionPreferredBrandTypeTags)
                .heightTag(heightTag)
                .weightTypeTag(weightTypeTag)
                .topSizeTag(topSizeTag)
                .bottomSizeTag(bottomSizeTag)
                .build();

        UserMatchingDetail detail = userMatchingDetailRepository.findByUserIdAndIsDeprecatedFalse(userId).orElse(null);
        String snsUrl = (detail != null) ? detail.getSnsUrl() : null;

        MatchRequestDto.SnsDto sns = MatchRequestDto.SnsDto.builder()
                .url(snsUrl)
                .mainAudience(MatchRequestDto.MainAudienceDto.builder()
                        .genderTags(genderTags.isEmpty() ? null : genderTags)
                        .ageTags(ageTags.isEmpty() ? null : ageTags)
                        .build())
                .averageAudience(MatchRequestDto.AverageAudienceDto.builder()
                        .videoLengthTags(videoLengthTags.isEmpty() ? null : videoLengthTags)
                        .videoViewsTags(videoViewsTags.isEmpty() ? null : videoViewsTags)
                        .build())
                .build();

        return MatchRequestDto.builder()
                .beauty(beauty)
                .fashion(fashion)
                .content(MatchRequestDto.ContentDto.builder()
                        .sns(sns)
                        .typeTags(typeTags.isEmpty() ? null : typeTags)
                        .toneTags(toneTags.isEmpty() ? null : toneTags)
                        .prefferedInvolvementTags(preferredInvolvementTags.isEmpty() ? null : preferredInvolvementTags)
                        .prefferedCoverageTags(preferredCoverageTags.isEmpty() ? null : preferredCoverageTags)
                        .build())
                .build();
    }

    // =====================================================
    //  merge 메소드들
    // =====================================================

    private MatchRequestDto mergeMatchRequest(MatchRequestDto current, MatchRequestDto patch) {
        if (current == null) {
            return patch;
        }
        if (patch == null) {
            return current;
        }

        return MatchRequestDto.builder()
                .beauty(mergeBeauty(current.getBeauty(), patch.getBeauty()))
                .fashion(mergeFashion(current.getFashion(), patch.getFashion()))
                .content(mergeContent(current.getContent(), patch.getContent()))
                .build();
    }

    private MatchRequestDto.BeautyDto mergeBeauty(MatchRequestDto.BeautyDto cur, MatchRequestDto.BeautyDto p) {
        if (cur == null) {
            return p;
        }
        if (p == null) {
            return cur;
        }

        return MatchRequestDto.BeautyDto.builder()
                .interestStyleTags(p.getInterestStyleTags() != null ? p.getInterestStyleTags() : cur.getInterestStyleTags())
                .prefferedFunctionTags(p.getPrefferedFunctionTags() != null ? p.getPrefferedFunctionTags() : cur.getPrefferedFunctionTags())
                .skinTypeTags(p.getSkinTypeTags() != null ? p.getSkinTypeTags() : cur.getSkinTypeTags())
                .skinToneTags(p.getSkinToneTags() != null ? p.getSkinToneTags() : cur.getSkinToneTags())
                .makeupStyleTags(p.getMakeupStyleTags() != null ? p.getMakeupStyleTags() : cur.getMakeupStyleTags())
                .build();
    }

    private MatchRequestDto.FashionDto mergeFashion(MatchRequestDto.FashionDto cur, MatchRequestDto.FashionDto p) {
        if (cur == null) {
            return p;
        }
        if (p == null) {
            return cur;
        }

        return MatchRequestDto.FashionDto.builder()
                .interestStyleTags(p.getInterestStyleTags() != null ? p.getInterestStyleTags() : cur.getInterestStyleTags())
                .preferredItemTags(p.getPreferredItemTags() != null ? p.getPreferredItemTags() : cur.getPreferredItemTags())
                .preferredBrandTags(p.getPreferredBrandTags() != null ? p.getPreferredBrandTags() : cur.getPreferredBrandTags())
                .heightTag(p.getHeightTag() != null ? p.getHeightTag() : cur.getHeightTag())
                .weightTypeTag(p.getWeightTypeTag() != null ? p.getWeightTypeTag() : cur.getWeightTypeTag())
                .topSizeTag(p.getTopSizeTag() != null ? p.getTopSizeTag() : cur.getTopSizeTag())
                .bottomSizeTag(p.getBottomSizeTag() != null ? p.getBottomSizeTag() : cur.getBottomSizeTag())
                .build();
    }

    private MatchRequestDto.ContentDto mergeContent(MatchRequestDto.ContentDto cur, MatchRequestDto.ContentDto p) {
        if (cur == null) {
            return p;
        }
        if (p == null) {
            return cur;
        }

        return MatchRequestDto.ContentDto.builder()
                .sns(mergeSns(cur.getSns(), p.getSns()))
                .typeTags(p.getTypeTags() != null ? p.getTypeTags() : cur.getTypeTags())
                .toneTags(p.getToneTags() != null ? p.getToneTags() : cur.getToneTags())
                .prefferedInvolvementTags(p.getPrefferedInvolvementTags() != null ? p.getPrefferedInvolvementTags() : cur.getPrefferedInvolvementTags())
                .prefferedCoverageTags(p.getPrefferedCoverageTags() != null ? p.getPrefferedCoverageTags() : cur.getPrefferedCoverageTags())
                .build();
    }

    private MatchRequestDto.SnsDto mergeSns(MatchRequestDto.SnsDto cur, MatchRequestDto.SnsDto p) {
        if (cur == null) {
            return p;
        }
        if (p == null) {
            return cur;
        }

        return MatchRequestDto.SnsDto.builder()
                .url(p.getUrl() != null ? p.getUrl() : cur.getUrl())
                .mainAudience(mergeMainAudience(cur.getMainAudience(), p.getMainAudience()))
                .averageAudience(mergeAverageAudience(cur.getAverageAudience(), p.getAverageAudience()))
                .build();
    }

    private MatchRequestDto.MainAudienceDto mergeMainAudience(
            MatchRequestDto.MainAudienceDto cur,
            MatchRequestDto.MainAudienceDto p
    ) {
        if (cur == null) {
            return p;
        }
        if (p == null) {
            return cur;
        }

        return MatchRequestDto.MainAudienceDto.builder()
                .genderTags(p.getGenderTags() != null ? p.getGenderTags() : cur.getGenderTags())
                .ageTags(p.getAgeTags() != null ? p.getAgeTags() : cur.getAgeTags())
                .build();
    }

    private MatchRequestDto.AverageAudienceDto mergeAverageAudience(
            MatchRequestDto.AverageAudienceDto cur,
            MatchRequestDto.AverageAudienceDto p
    ) {
        if (cur == null) {
            return p;
        }
        if (p == null) {
            return cur;
        }

        return MatchRequestDto.AverageAudienceDto.builder()
                .videoLengthTags(p.getVideoLengthTags() != null ? p.getVideoLengthTags() : cur.getVideoLengthTags())
                .videoViewsTags(p.getVideoViewsTags() != null ? p.getVideoViewsTags() : cur.getVideoViewsTags())
                .build();
    }
}
