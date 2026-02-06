package com.example.RealMatch.user.application.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.match.application.service.MatchService;
import com.example.RealMatch.match.presentation.dto.request.MatchRequestDto;
import com.example.RealMatch.user.domain.entity.UserMatchingDetail;
import com.example.RealMatch.user.domain.repository.UserMatchingDetailRepository;
import com.example.RealMatch.user.presentation.code.UserErrorCode;
import com.example.RealMatch.user.presentation.dto.request.MyFeatureUpdateRequestDto;
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

        // creatorType(매칭 결과) 포함하여 반환
        return new MyFeatureResponseDto(beautyType, fashionType, contentsType);
    }

    /**
     * 사용자 특성 정보 부분 업데이트
     * - 엔티티의 비즈니스 메서드를 통해 업데이트
     * - 보낸 필드만 업데이트, 보내지 않은 필드는 기존 값 유지
     */
    @Transactional
    public void updateMyFeatures(Long userId, MyFeatureUpdateRequestDto request) {

        if (request == null) {
            throw new CustomException(UserErrorCode.TRAIT_UPDATE_FAILED);
        }

        // 삭제되지 않은 활성 프로필 조회
        UserMatchingDetail detail = userMatchingDetailRepository.findByUserIdAndIsDeprecatedFalse(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_MATCHING_DETAIL_NOT_FOUND));

        // 뷰티 특성 업데이트
        if (request.beautyType() != null) {
            MyFeatureUpdateRequestDto.BeautyTypeUpdate beautyType = request.beautyType();
            detail.updateBeautyFeatures(
                    joinTagList(beautyType.skinType()),
                    joinTagList(beautyType.skinBrightness()),
                    joinTagList(beautyType.makeupStyle()),
                    joinTagList(beautyType.interestCategories()),
                    joinTagList(beautyType.interestFunctions())
            );
            log.debug("뷰티 특성 업데이트 완료: userId={}", userId);
        }

        // 패션 특성 업데이트
        if (request.fashionType() != null) {
            MyFeatureUpdateRequestDto.FashionTypeUpdate fashionType = request.fashionType();
            detail.updateFashionFeatures(
                    fashionType.height(),
                    fashionType.bodyShape(),
                    fashionType.topSize(),
                    fashionType.bottomSize(),
                    joinTagList(fashionType.interestFields()),
                    joinTagList(fashionType.interestStyles()),
                    joinTagList(fashionType.interestBrands())
            );
            log.debug("패션 특성 업데이트 완료: userId={}", userId);
        }

        // 콘텐츠 특성 업데이트
        if (request.contentsType() != null) {
            MyFeatureUpdateRequestDto.ContentsTypeUpdate contentsType = request.contentsType();
            detail.updateContentsFeatures(
                    contentsType.snsUrl(),
                    joinTagList(contentsType.viewerGender()),
                    joinTagList(contentsType.viewerAge()),
                    contentsType.avgVideoLength(),
                    contentsType.avgViews(),
                    joinTagList(contentsType.contentFormats()),
                    joinTagList(contentsType.contentTones()),
                    joinTagList(contentsType.desiredInvolvement()),
                    joinTagList(contentsType.desiredUsageScope())
            );
            log.debug("콘텐츠 특성 업데이트 완료: userId={}", userId);
        }

        // JPA 더티 체킹으로 자동 저장
        log.info("사용자 특성 정보 업데이트 완료: userId={}", userId);

        // 특성 업데이트 후 자동으로 매칭 재실행
        try {
            log.info("특성 업데이트 후 매칭 재실행 시작: userId={}", userId);

            // 업데이트된 UserMatchingDetail을 MatchRequestDto로 변환
            MatchRequestDto matchRequest = convertToMatchRequest(detail);

            // 매칭 재실행 (이전 매칭 결과는 MatchService에서 자동으로 폐기)
            matchService.match(userId, matchRequest);

            log.info("특성 업데이트 후 매칭 재실행 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("매칭 재실행 중 오류 발생: userId={}, error={}", userId, e.getMessage(), e);
            // 매칭 실패는 특성 업데이트에 영향을 주지 않음 (로그만 기록)
        }
    }

    /**
     * 매칭 결과(CreatorType) 저장
     * MatchService에서 매칭 계산이 끝난 후 호출
     */
    @Transactional
    public void updateMatchingResult(Long userId, String creatorType) { // matchService에서 호출 연동 예정
        UserMatchingDetail detail = userMatchingDetailRepository.findByUserIdAndIsDeprecatedFalse(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_MATCHING_DETAIL_NOT_FOUND));

        detail.setMatchingResult(creatorType);
        log.info("매칭 결과 저장 완료: userId={}, creatorType={}", userId, creatorType);
    }

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

    /**
     * 쉼표로 구분된 문자열을 List로 변환
     * 예: "스킨케어,메이크업,향수" -> ["스킨케어", "메이크업", "향수"]
     */
    private List<String> parseTagString(String tagString) {
        if (tagString == null || tagString.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(tagString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * List를 쉼표로 구분된 문자열로 변환
     * 예: ["스킨케어", "메이크업", "향수"] -> "스킨케어,메이크업,향수"
     */
    private String joinTagList(List<String> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            return null;
        }

        return tagList.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(","));
    }

    /**
     * UserMatchingDetail을 MatchRequestDto로 변환
     * 매칭 재실행을 위해 사용자의 현재 특성 정보를 DTO로 변환
     */
    private MatchRequestDto convertToMatchRequest(UserMatchingDetail detail) {
        try {
            MatchRequestDto requestDto = new MatchRequestDto();

            // Beauty 정보 설정
            if (hasBeautyData(detail)) {
                setField(requestDto, "beauty", createBeautyDto(detail));
            }

            // Fashion 정보 설정
            if (hasFashionData(detail)) {
                setField(requestDto, "fashion", createFashionDto(detail));
            }

            // Content 정보 설정
            if (hasContentData(detail)) {
                setField(requestDto, "content", createContentDto(detail));
            }

            return requestDto;
        } catch (Exception e) {
            log.error("MatchRequestDto 변환 중 오류 발생", e);
            throw new CustomException(UserErrorCode.TRAIT_UPDATE_FAILED);
        }
    }

    /**
     * Reflection을 사용하여 private 필드에 값 설정
     */
    private void setField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * Beauty 데이터 존재 여부 확인
     */
    private boolean hasBeautyData(UserMatchingDetail detail) {
        return detail.getSkinType() != null
                || detail.getSkinBrightness() != null
                || detail.getMakeupStyle() != null
                || detail.getInterestCategories() != null
                || detail.getInterestFunctions() != null;
    }

    /**
     * Fashion 데이터 존재 여부 확인
     */
    private boolean hasFashionData(UserMatchingDetail detail) {
        return detail.getHeight() != null
                || detail.getBodyShape() != null
                || detail.getTopSize() != null
                || detail.getBottomSize() != null
                || detail.getInterestFields() != null
                || detail.getInterestStyles() != null
                || detail.getInterestBrands() != null;
    }

    /**
     * Content 데이터 존재 여부 확인
     */
    private boolean hasContentData(UserMatchingDetail detail) {
        return detail.getSnsUrl() != null
                || detail.getViewerGender() != null
                || detail.getViewerAge() != null
                || detail.getAvgVideoLength() != null
                || detail.getAvgViews() != null
                || detail.getContentFormats() != null
                || detail.getContentTones() != null
                || detail.getDesiredInvolvement() != null
                || detail.getDesiredUsageScope() != null;
    }

    /**
     * BeautyDto 생성
     */
    private MatchRequestDto.BeautyDto createBeautyDto(UserMatchingDetail detail) {
        try {
            MatchRequestDto.BeautyDto beautyDto = new MatchRequestDto.BeautyDto();

            // interestStyleTags: interestCategories (카테고리 관심사)
            if (detail.getInterestCategories() != null) {
                setField(beautyDto, "interestStyleTags", convertToIntegerList(detail.getInterestCategories()));
            }

            // prefferedFunctionTags: interestFunctions (기능 관심사)
            if (detail.getInterestFunctions() != null) {
                setField(beautyDto, "prefferedFunctionTags", convertToIntegerList(detail.getInterestFunctions()));
            }

            // skinTypeTags: skinType (피부 타입)
            if (detail.getSkinType() != null) {
                setField(beautyDto, "skinTypeTags", parseFirstInteger(detail.getSkinType()));
            }

            // skinToneTags: skinBrightness (피부 톤)
            if (detail.getSkinBrightness() != null) {
                setField(beautyDto, "skinToneTags", parseFirstInteger(detail.getSkinBrightness()));
            }

            // makeupStyleTags: makeupStyle (메이크업 스타일)
            if (detail.getMakeupStyle() != null) {
                setField(beautyDto, "makeupStyleTags", parseFirstInteger(detail.getMakeupStyle()));
            }

            return beautyDto;
        } catch (Exception e) {
            log.error("BeautyDto 생성 중 오류 발생", e);
            return new MatchRequestDto.BeautyDto();
        }
    }

    /**
     * FashionDto 생성
     */
    private MatchRequestDto.FashionDto createFashionDto(UserMatchingDetail detail) {
        try {
            MatchRequestDto.FashionDto fashionDto = new MatchRequestDto.FashionDto();

            // interestStyleTags: interestFields (관심 분야)
            if (detail.getInterestFields() != null) {
                setField(fashionDto, "interestStyleTags", convertToIntegerList(detail.getInterestFields()));
            }

            // preferredItemTags: interestStyles (관심 스타일)
            if (detail.getInterestStyles() != null) {
                setField(fashionDto, "preferredItemTags", convertToIntegerList(detail.getInterestStyles()));
            }

            // preferredBrandTags: interestBrands (관심 브랜드)
            if (detail.getInterestBrands() != null) {
                setField(fashionDto, "preferredBrandTags", convertToIntegerList(detail.getInterestBrands()));
            }

            // heightTag: height (키)
            if (detail.getHeight() != null) {
                setField(fashionDto, "heightTag", parseFirstInteger(detail.getHeight()));
            }

            // weightTypeTag: bodyShape (체형)
            if (detail.getBodyShape() != null) {
                setField(fashionDto, "weightTypeTag", parseFirstInteger(detail.getBodyShape()));
            }

            // topSizeTag: topSize (상의 사이즈)
            if (detail.getTopSize() != null) {
                setField(fashionDto, "topSizeTag", parseFirstInteger(detail.getTopSize()));
            }

            // bottomSizeTag: bottomSize (하의 사이즈)
            if (detail.getBottomSize() != null) {
                setField(fashionDto, "bottomSizeTag", parseFirstInteger(detail.getBottomSize()));
            }

            return fashionDto;
        } catch (Exception e) {
            log.error("FashionDto 생성 중 오류 발생", e);
            return new MatchRequestDto.FashionDto();
        }
    }

    /**
     * ContentDto 생성
     */
    private MatchRequestDto.ContentDto createContentDto(UserMatchingDetail detail) {
        try {
            MatchRequestDto.ContentDto contentDto = new MatchRequestDto.ContentDto();

            // SNS 정보 설정
            if (detail.getSnsUrl() != null || detail.getViewerGender() != null
                    || detail.getViewerAge() != null || detail.getAvgVideoLength() != null
                    || detail.getAvgViews() != null) {
                setField(contentDto, "sns", createSnsDto(detail));
            }

            // typeTags: contentFormats (콘텐츠 형식)
            if (detail.getContentFormats() != null) {
                setField(contentDto, "typeTags", convertToIntegerList(detail.getContentFormats()));
            }

            // toneTags: contentTones (콘텐츠 톤)
            if (detail.getContentTones() != null) {
                setField(contentDto, "toneTags", convertToIntegerList(detail.getContentTones()));
            }

            // prefferedInvolvementTags: desiredInvolvement (원하는 관여도)
            if (detail.getDesiredInvolvement() != null) {
                setField(contentDto, "prefferedInvolvementTags", convertToIntegerList(detail.getDesiredInvolvement()));
            }

            // prefferedCoverageTags: desiredUsageScope (원하는 사용 범위)
            if (detail.getDesiredUsageScope() != null) {
                setField(contentDto, "prefferedCoverageTags", convertToIntegerList(detail.getDesiredUsageScope()));
            }

            return contentDto;
        } catch (Exception e) {
            log.error("ContentDto 생성 중 오류 발생", e);
            return new MatchRequestDto.ContentDto();
        }
    }

    /**
     * SnsDto 생성
     */
    private MatchRequestDto.SnsDto createSnsDto(UserMatchingDetail detail) {
        try {
            MatchRequestDto.SnsDto snsDto = new MatchRequestDto.SnsDto();

            // URL 설정
            if (detail.getSnsUrl() != null) {
                setField(snsDto, "url", detail.getSnsUrl());
            }

            // 주요 시청자 정보 설정
            if (detail.getViewerGender() != null || detail.getViewerAge() != null) {
                MatchRequestDto.MainAudienceDto mainAudience = new MatchRequestDto.MainAudienceDto();

                if (detail.getViewerGender() != null) {
                    setField(mainAudience, "genderTags", convertToIntegerList(detail.getViewerGender()));
                }

                if (detail.getViewerAge() != null) {
                    setField(mainAudience, "ageTags", convertToIntegerList(detail.getViewerAge()));
                }

                setField(snsDto, "mainAudience", mainAudience);
            }

            // 평균 시청 정보 설정
            if (detail.getAvgVideoLength() != null || detail.getAvgViews() != null) {
                MatchRequestDto.AverageAudienceDto averageAudience = new MatchRequestDto.AverageAudienceDto();

                if (detail.getAvgVideoLength() != null) {
                    setField(averageAudience, "videoLengthTags", convertToIntegerList(detail.getAvgVideoLength()));
                }

                if (detail.getAvgViews() != null) {
                    setField(averageAudience, "videoViewsTags", convertToIntegerList(detail.getAvgViews()));
                }

                setField(snsDto, "averageAudience", averageAudience);
            }

            return snsDto;
        } catch (Exception e) {
            log.error("SnsDto 생성 중 오류 발생", e);
            return new MatchRequestDto.SnsDto();
        }
    }

    /**
     * 쉼표로 구분된 문자열을 Integer List로 변환
     * 예: "1,2,3" -> [1, 2, 3]
     */
    private List<Integer> convertToIntegerList(String tagString) {
        if (tagString == null || tagString.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            return Arrays.stream(tagString.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            log.warn("태그 문자열을 Integer로 변환 실패: {}", tagString);
            return Collections.emptyList();
        }
    }

    /**
     * 문자열에서 첫 번째 Integer 값만 파싱
     * 예: "1,2,3" -> 1 또는 "medium" -> null
     */
    private Integer parseFirstInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            // 쉼표로 구분된 경우 첫 번째 값만 사용
            String firstValue = value.contains(",")
                    ? value.split(",")[0].trim()
                    : value.trim();
            return Integer.parseInt(firstValue);
        } catch (NumberFormatException e) {
            log.warn("문자열을 Integer로 변환 실패: {}", value);
            return null;
        }
    }
}
