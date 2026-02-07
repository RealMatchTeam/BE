package com.example.RealMatch.user.application.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.match.application.service.MatchService;
import com.example.RealMatch.match.presentation.dto.request.MatchRequestDto;
import com.example.RealMatch.tag.domain.entity.Tag;
import com.example.RealMatch.tag.domain.entity.UserTag;
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

    public MyFeatureResponseDto getMyFeatures(Long userId) {

        List<UserTag> userTags = userTagRepository.findAllByUserIdWithTag(userId);
        log.info("userId={}, userTags.size={}", userId, userTags.size());

        userTags.stream()
                .limit(30)
                .forEach(ut -> log.info("utId={}, dep={}, tagId={}, type={}, category={}, deleted={}",
                        ut.getId(),
                        ut.isDeprecated(),
                        ut.getTag() != null ? ut.getTag().getId() : null,
                        ut.getTag() != null ? ut.getTag().getTagType() : null,
                        ut.getTag() != null ? ut.getTag().getTagCategory() : null,
                        ut.getTag() != null ? ut.getTag().isDeleted() : null
                ));


        MyFeatureResponseDto.BeautyType beautyType = new MyFeatureResponseDto.BeautyType(
                tagIds(userTags, "뷰티", "피부타입"),
                tagIds(userTags, "뷰티", "피부 밝기"),
                tagIds(userTags, "뷰티", "메이크업 스타일"),
                tagIds(userTags, "뷰티", "관심 카테고리"),
                tagIds(userTags, "뷰티", "관심 기능")
        );

        MyFeatureResponseDto.FashionType fashionType = new MyFeatureResponseDto.FashionType(
                tagIds(userTags, "패션", "키"),
                tagIds(userTags, "패션", "체형 실루엣"),
                tagIds(userTags, "패션", "상의 사이즈"),
                tagIds(userTags, "패션", "하의 사이즈"),
                tagIds(userTags, "패션", "관심 분야"),
                tagIds(userTags, "패션", "관심 스타일"),
                tagIds(userTags, "패션", "관심 브랜드")
        );

        MyFeatureResponseDto.ContentsType contentsType = new MyFeatureResponseDto.ContentsType(
                tagIds(userTags, "콘텐츠", "주 시청자 성별"),
                tagIds(userTags, "콘텐츠", "주 시청자 나이대"),
                tagIds(userTags, "콘텐츠", "평균 영상 길이"),
                tagIds(userTags, "콘텐츠", "평균 조회수"),
                tagIds(userTags, "콘텐츠", "콘텐츠 형식"),
                tagIds(userTags, "콘텐츠", "콘텐츠 톤"),
                tagIds(userTags, "콘텐츠", "희망 관여도"),
                tagIds(userTags, "콘텐츠", "희망 활용 범위")
        );

        return new MyFeatureResponseDto(beautyType, fashionType, contentsType);
    }

    /**
     * 프론트가 MatchRequestDto 형태(정수 id 태그)로 보내는 PATCH 요청
     * - patch(부분)만 보내도 서버에서 기존값과 merge해서 완성본 만들고
     * - matchService.match() 호출 (UserTag 업데이트 + 매칭 재실행)
     */
    @Transactional
    public void updateMyFeatures(Long userId, MatchRequestDto patchRequest) {
        if (patchRequest == null) {
            throw new CustomException(UserErrorCode.TRAIT_UPDATE_FAILED);
        }

        // 1) 기존 UserTag 조회
        List<UserTag> existingUserTags = userTagRepository.findAllByUserIdWithTag(userId);

        // 2) 기존 UserTag -> MatchRequestDto 복원
        MatchRequestDto currentRequest = toMatchRequestDtoFromUserTags(existingUserTags, userId);

        // 3) patch merge (보낸 값만 덮고 나머지는 유지)
        MatchRequestDto merged = mergeMatchRequest(currentRequest, patchRequest);

        // 4) 매칭 재실행 (MatchService 내부에서 UserTag 업데이트도 같이 해야 함)
        matchService.match(userId, merged);

        log.info("특성 PATCH 후 매칭 재실행 완료: userId={}", userId);
    }

    // =====================================================
    // 🔧 helpers (UserTag -> DTO)
    // =====================================================

    private static List<Integer> tagIds(List<UserTag> userTags, String tagType, String tagCategory) {
        return userTags.stream()
                .map(UserTag::getTag)
                .filter(t -> t != null)
                .filter(t -> !t.isDeleted())
                .filter(t -> tagType.equals(t.getTagType()))   // 원래는 getTagType 이어야 함
                .filter(t -> tagCategory.equals(t.getTagCategory()))   // 원래는 getTagCategory 이어야 함
                .map(t -> t.getId().intValue())
                .toList();
    }

    // =====================================================
    // 🔧 helpers (UserTag -> MatchRequestDto 복원)
    // =====================================================

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

        for (UserTag ut : userTags) {
            Tag tag = ut.getTag();
            if (tag == null || tag.isDeleted() || tag.getTagType() == null || tag.getTagCategory() == null) {
                continue;
            }

            String type = tag.getTagType();
            String category = tag.getTagCategory();
            Integer tagId = tag.getId().intValue();

            // ---- Beauty ----
            if ("뷰티".equals(type)) {
                switch (category) {
                    case "피부타입" -> skinTypeTag = tagId;
                    case "피부 밝기" -> skinBrightnessTag = tagId;
                    case "메이크업 스타일" -> makeupStyleTag = tagId;
                    case "관심 카테고리" -> beautyInterestStyleTags.add(tagId);
                    case "관심 기능" -> beautyPreferredFunctionTags.add(tagId);
                    default -> {
                    }
                }
                continue;
            }

            // ---- Fashion ----
            if ("패션".equals(type)) {
                switch (category) {
                    case "키" -> heightTag = tagId;
                    case "체형 실루엣" -> weightTypeTag = tagId;
                    case "상의 사이즈" -> topSizeTag = tagId;
                    case "하의 사이즈" -> bottomSizeTag = tagId;
                    case "관심 분야" -> fashionPreferredItemTags.add(tagId);
                    case "관심 스타일" -> fashionInterestStyleTags.add(tagId);
                    case "관심 브랜드" -> fashionPreferredBrandTypeTags.add(tagId);
                    default -> {
                    }
                }
                continue;
            }

            // ---- Content ----
            if ("콘텐츠".equals(type)) {
                switch (category) {
                    case "주 시청자 성별" -> genderTags.add(tagId);
                    case "주 시청자 나이대" -> ageTags.add(tagId);
                    case "평균 영상 길이" -> videoLengthTags.add(tagId);
                    case "평균 조회수" -> videoViewsTags.add(tagId);
                    case "콘텐츠 형식" -> typeTags.add(tagId);
                    case "콘텐츠 톤" -> toneTags.add(tagId);
                    case "희망 관여도" -> preferredInvolvementTags.add(tagId);
                    case "희망 활용 범위" -> preferredCoverageTags.add(tagId);
                    default -> {
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

        // snsUrl은 UserMatchingDetail에서 가져오는 정책 유지
        UserMatchingDetail detail = userMatchingDetailRepository.findByUserIdAndIsDeprecatedFalse(userId)
                .orElse(null);
        String snsUrl = (detail != null) ? detail.getSnsUrl() : null;

        MatchRequestDto.MainAudienceDto mainAudience = MatchRequestDto.MainAudienceDto.builder()
                .genderTags(genderTags.isEmpty() ? null : genderTags)
                .ageTags(ageTags.isEmpty() ? null : ageTags)
                .build();

        MatchRequestDto.AverageAudienceDto averageAudience = MatchRequestDto.AverageAudienceDto.builder()
                .videoLengthTags(videoLengthTags.isEmpty() ? null : videoLengthTags)
                .videoViewsTags(videoViewsTags.isEmpty() ? null : videoViewsTags)
                .build();

        MatchRequestDto.SnsDto sns = MatchRequestDto.SnsDto.builder()
                .url(snsUrl)
                .mainAudience(mainAudience)
                .averageAudience(averageAudience)
                .build();

        MatchRequestDto.ContentDto content = MatchRequestDto.ContentDto.builder()
                .sns(sns)
                .typeTags(typeTags.isEmpty() ? null : typeTags)
                .toneTags(toneTags.isEmpty() ? null : toneTags)
                .prefferedInvolvementTags(preferredInvolvementTags.isEmpty() ? null : preferredInvolvementTags)
                .prefferedCoverageTags(preferredCoverageTags.isEmpty() ? null : preferredCoverageTags)
                .build();

        return MatchRequestDto.builder()
                .beauty(beauty)
                .fashion(fashion)
                .content(content)
                .build();
    }

    // =====================================================
    // 🔧 helpers (PATCH merge)
    // =====================================================

    private MatchRequestDto mergeMatchRequest(MatchRequestDto current, MatchRequestDto patch) {
        if (current == null) {
            return patch;
        }
        if (patch == null) {
            return current;
        }

        MatchRequestDto.BeautyDto mergedBeauty = mergeBeauty(current.getBeauty(), patch.getBeauty());
        MatchRequestDto.FashionDto mergedFashion = mergeFashion(current.getFashion(), patch.getFashion());
        MatchRequestDto.ContentDto mergedContent = mergeContent(current.getContent(), patch.getContent());

        return MatchRequestDto.builder()
                .beauty(mergedBeauty)
                .fashion(mergedFashion)
                .content(mergedContent)
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

        MatchRequestDto.SnsDto mergedSns = mergeSns(cur.getSns(), p.getSns());

        return MatchRequestDto.ContentDto.builder()
                .sns(mergedSns)
                .typeTags(p.getTypeTags() != null ? p.getTypeTags() : cur.getTypeTags())
                .toneTags(p.getToneTags() != null ? p.getToneTags() : cur.getToneTags())
                .prefferedInvolvementTags(p.getPrefferedInvolvementTags() != null
                        ? p.getPrefferedInvolvementTags()
                        : cur.getPrefferedInvolvementTags())
                .prefferedCoverageTags(p.getPrefferedCoverageTags() != null
                        ? p.getPrefferedCoverageTags()
                        : cur.getPrefferedCoverageTags())
                .build();
    }

    private MatchRequestDto.SnsDto mergeSns(MatchRequestDto.SnsDto cur, MatchRequestDto.SnsDto p) {
        if (cur == null) {
            return p;
        }
        if (p == null) {
            return cur;
        }

        MatchRequestDto.MainAudienceDto main = mergeMainAudience(cur.getMainAudience(), p.getMainAudience());
        MatchRequestDto.AverageAudienceDto avg = mergeAverageAudience(cur.getAverageAudience(), p.getAverageAudience());

        return MatchRequestDto.SnsDto.builder()
                .url(p.getUrl() != null ? p.getUrl() : cur.getUrl())
                .mainAudience(main)
                .averageAudience(avg)
                .build();
    }

    private MatchRequestDto.MainAudienceDto mergeMainAudience(MatchRequestDto.MainAudienceDto cur,
                                                              MatchRequestDto.MainAudienceDto p) {
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

    private MatchRequestDto.AverageAudienceDto mergeAverageAudience(MatchRequestDto.AverageAudienceDto cur,
                                                                    MatchRequestDto.AverageAudienceDto p) {
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
