package com.example.RealMatch.user.application.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.match.application.service.MatchService;
import com.example.RealMatch.match.presentation.dto.request.MatchRequestDto;
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

    private final UserMatchingDetailRepository userMatchingDetailRepository;
    private final MatchService matchService;

    public MyFeatureResponseDto getMyFeatures(Long userId) {

        // UserMatchingDetail 조회 (삭제되지 않은 데이터만 조회)
        UserMatchingDetail detail = userMatchingDetailRepository.findByUserIdAndIsDeprecatedFalse(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_MATCHING_DETAIL_NOT_FOUND));

        // 각 영역별로 데이터 존재 여부 확인 및 조회
        MyFeatureResponseDto.BeautyType beautyType = buildBeautyType(detail);
        MyFeatureResponseDto.FashionType fashionType = buildFashionType(detail);
        MyFeatureResponseDto.ContentsType contentsType = buildContentsType(detail);

        return new MyFeatureResponseDto(beautyType, fashionType, contentsType);
    }

    /**
     *  프론트가 MatchRequestDto 형태(정수 id 태그)로 보내는 PATCH 요청
     * - patch(부분)만 보내도 서버에서 기존값과 merge해서 완성본 만들고
     * - matchService.match() 호출 (기존 detail 폐기 + 새 detail 저장 + matchingResult 저장)
     */
    @Transactional
    public void updateMyFeatures(Long userId, MatchRequestDto patchRequest) {
        if (patchRequest == null) {
            throw new CustomException(UserErrorCode.TRAIT_UPDATE_FAILED);
        }

        UserMatchingDetail current = userMatchingDetailRepository.findByUserIdAndIsDeprecatedFalse(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_MATCHING_DETAIL_NOT_FOUND));

        // 1) current(detail) -> 기존 매칭 요청 DTO로 복원
        MatchRequestDto currentRequest = toMatchRequestDto(current);

        // 2) patch merge (보낸 값만 덮고 나머지는 유지)
        MatchRequestDto merged = mergeMatchRequest(currentRequest, patchRequest);

        // 3) 매칭 재실행 (MatchService 내부에서 detail 교체 + 결과 저장)
        matchService.match(userId, merged);

        log.info("특성 PATCH 후 매칭 재실행 완료: userId={}", userId);
    }

    /**
     * DB(UserMatchingDetail) -> MatchRequestDto (id 기반)
     * 전제: UserMatchingDetail의 태그 관련 값이 "1,2,3" 처럼 id 콤마 문자열로 저장되어 있음
     */
    private MatchRequestDto toMatchRequestDto(UserMatchingDetail d) {

        MatchRequestDto.BeautyDto beauty = null;
        if (hasAny(d.getInterestCategories(), d.getInterestFunctions(), d.getSkinType(), d.getSkinBrightness(), d.getMakeupStyle())) {
            beauty = MatchRequestDto.BeautyDto.builder()
                    .interestStyleTags(parseIntList(d.getInterestCategories()))
                    .prefferedFunctionTags(parseIntList(d.getInterestFunctions()))
                    .skinTypeTags(parseFirstInt(d.getSkinType()))
                    .skinToneTags(parseFirstInt(d.getSkinBrightness()))
                    .makeupStyleTags(parseFirstInt(d.getMakeupStyle()))
                    .build();
        }

        MatchRequestDto.FashionDto fashion = null;
        if (hasAny(d.getInterestFields(), d.getInterestStyles(), d.getInterestBrands(), d.getHeight(), d.getBodyShape(), d.getTopSize(), d.getBottomSize())) {
            fashion = MatchRequestDto.FashionDto.builder()
                    .interestStyleTags(parseIntList(d.getInterestFields()))
                    .preferredItemTags(parseIntList(d.getInterestStyles()))
                    .preferredBrandTags(parseIntList(d.getInterestBrands()))
                    .heightTag(parseFirstInt(d.getHeight()))
                    .weightTypeTag(parseFirstInt(d.getBodyShape()))
                    .topSizeTag(parseFirstInt(d.getTopSize()))
                    .bottomSizeTag(parseFirstInt(d.getBottomSize()))
                    .build();
        }

        MatchRequestDto.ContentDto content = null;
        if (hasAny(d.getSnsUrl(), d.getViewerGender(), d.getViewerAge(), d.getAvgVideoLength(), d.getAvgViews(),
                d.getContentFormats(), d.getContentTones(), d.getDesiredInvolvement(), d.getDesiredUsageScope())) {

            MatchRequestDto.MainAudienceDto mainAudience = null;
            if (hasAny(d.getViewerGender(), d.getViewerAge())) {
                mainAudience = MatchRequestDto.MainAudienceDto.builder()
                        .genderTags(parseIntList(d.getViewerGender()))
                        .ageTags(parseIntList(d.getViewerAge()))
                        .build();
            }

            MatchRequestDto.AverageAudienceDto averageAudience = null;
            if (hasAny(d.getAvgVideoLength(), d.getAvgViews())) {
                // DB가 "228,229" 형태로 저장되어 있어야 함
                averageAudience = MatchRequestDto.AverageAudienceDto.builder()
                        .videoLengthTags(parseIntList(d.getAvgVideoLength()))
                        .videoViewsTags(parseIntList(d.getAvgViews()))
                        .build();
            }

            MatchRequestDto.SnsDto sns = null;
            if (d.getSnsUrl() != null || mainAudience != null || averageAudience != null) {
                sns = MatchRequestDto.SnsDto.builder()
                        .url(d.getSnsUrl())
                        .mainAudience(mainAudience)
                        .averageAudience(averageAudience)
                        .build();
            }

            content = MatchRequestDto.ContentDto.builder()
                    .sns(sns)
                    .typeTags(parseIntList(d.getContentFormats()))
                    .toneTags(parseIntList(d.getContentTones()))
                    .prefferedInvolvementTags(parseIntList(d.getDesiredInvolvement()))
                    .prefferedCoverageTags(parseIntList(d.getDesiredUsageScope()))
                    .build();
        }

        return MatchRequestDto.builder()
                .beauty(beauty)
                .fashion(fashion)
                .content(content)
                .build();
    }

    /**
     * current + patch merge (PATCH 규칙)
     * - patch dto가 null이면 current 유지
     * - patch dto가 non-null이면 내부 필드도 null이면 유지 / non-null이면 덮기
     */
    private MatchRequestDto mergeMatchRequest(MatchRequestDto current, MatchRequestDto patch) {

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
        if (cur == null && p == null) {
            return null;
        }
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
        if (cur == null && p == null) {
            return null;
        }
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
        if (cur == null && p == null) {
            return null;
        }
        if (cur == null) {
            return p;
        }
        if (p == null) {
            return cur;
        }

        MatchRequestDto.SnsDto sns = mergeSns(cur.getSns(), p.getSns());

        return MatchRequestDto.ContentDto.builder()
                .sns(sns)
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
        if (cur == null && p == null) {
            return null;
        }
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

    private MatchRequestDto.MainAudienceDto mergeMainAudience(MatchRequestDto.MainAudienceDto cur, MatchRequestDto.MainAudienceDto p) {
        if (cur == null && p == null) {
            return null;
        }
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

    private MatchRequestDto.AverageAudienceDto mergeAverageAudience(MatchRequestDto.AverageAudienceDto cur, MatchRequestDto.AverageAudienceDto p) {
        if (cur == null && p == null) {
            return null;
        }
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

    // =========================
    // getMyFeatures()용 build 메서드 (기존 그대로)
    // =========================

    private MyFeatureResponseDto.BeautyType buildBeautyType(UserMatchingDetail detail) {

        if (detail.getSkinType() == null
                && detail.getSkinBrightness() == null
                && detail.getMakeupStyle() == null
                && detail.getInterestCategories() == null
                && detail.getInterestFunctions() == null) {

            throw new CustomException(UserErrorCode.BEAUTY_PROFILE_NOT_FOUND);
        }

        return new MyFeatureResponseDto.BeautyType(
                parseTagString(detail.getSkinType()),
                detail.getSkinBrightness(),
                parseTagString(detail.getMakeupStyle()),
                parseTagString(detail.getInterestCategories()),
                parseTagString(detail.getInterestFunctions())
        );
    }

    private MyFeatureResponseDto.FashionType buildFashionType(UserMatchingDetail detail) {

        if (detail.getHeight() == null
                && detail.getBodyShape() == null
                && detail.getTopSize() == null
                && detail.getBottomSize() == null
                && detail.getInterestFields() == null
                && detail.getInterestStyles() == null
                && detail.getInterestBrands() == null) {

            throw new CustomException(UserErrorCode.FASHION_PROFILE_NOT_FOUND);
        }

        return new MyFeatureResponseDto.FashionType(
                detail.getHeight(),
                detail.getBodyShape(),
                detail.getTopSize(),
                detail.getBottomSize(),
                parseTagString(detail.getInterestFields()),
                parseTagString(detail.getInterestStyles()),
                parseTagString(detail.getInterestBrands())
        );
    }

    private MyFeatureResponseDto.ContentsType buildContentsType(UserMatchingDetail detail) {

        if (detail.getViewerGender() == null
                && detail.getViewerAge() == null
                && detail.getAvgVideoLength() == null
                && detail.getAvgViews() == null
                && detail.getContentFormats() == null
                && detail.getContentTones() == null
                && detail.getDesiredInvolvement() == null
                && detail.getDesiredUsageScope() == null) {

            throw new CustomException(UserErrorCode.CONTENT_PROFILE_NOT_FOUND);
        }

        return new MyFeatureResponseDto.ContentsType(
                parseTagString(detail.getViewerGender()),
                parseTagString(detail.getViewerAge()),
                detail.getAvgVideoLength(),
                detail.getAvgViews(),
                parseTagString(detail.getContentFormats()),
                parseTagString(detail.getContentTones()),
                parseTagString(detail.getDesiredInvolvement()),
                parseTagString(detail.getDesiredUsageScope())
        );
    }

    private List<String> parseTagString(String tagString) {
        if (tagString == null || tagString.trim().isEmpty()) {
            return List.of();
        }

        return Arrays.stream(tagString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    // =========================
    // parsing helpers (id 콤마 문자열 -> 정수 리스트)
    // =========================

    private static List<Integer> parseIntList(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            List<Integer> list = Arrays.stream(s.split(","))
                    .map(String::trim)
                    .filter(v -> !v.isEmpty())
                    .map(Integer::parseInt)
                    .toList();
            return list.isEmpty() ? null : list;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer parseFirstInt(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        String first = s.contains(",") ? s.split(",")[0].trim() : s.trim();
        try {
            return Integer.parseInt(first);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean hasAny(Object... values) {
        for (Object v : values) {
            if (v == null) {
                continue;
            }
            if (v instanceof String s) {
                if (!s.isBlank()) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }
}
