package com.example.RealMatch.user.application.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.global.exception.CustomException;
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

    public MyFeatureResponseDto getMyFeatures(Long userId) {

        // UserMatchingDetail 조회
        UserMatchingDetail detail = userMatchingDetailRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_MATCHING_DETAIL_NOT_FOUND));

        log.info("사용자 프로필 조회 완료: userId={}", userId);

        // 각 영역별로 데이터 존재 여부 확인 및 조회
        MyFeatureResponseDto.BeautyType beautyType = buildBeautyType(detail);
        MyFeatureResponseDto.FashionType fashionType = buildFashionType(detail);
        MyFeatureResponseDto.ContentsType contentsType = buildContentsType(detail);

        return new MyFeatureResponseDto(beautyType, fashionType, contentsType);
    }

    /**
     * 사용자 특성 정보 부분 업데이트
     * - 엔티티의 비즈니스 메서드를 통해 업데이트
     * - 보낸 필드만 업데이트, 보내지 않은 필드는 기존 값 유지
     */
    @Transactional
    public void updateMyFeatures(Long userId, MyFeatureUpdateRequestDto request) {
        UserMatchingDetail detail = userMatchingDetailRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_MATCHING_DETAIL_NOT_FOUND));

        // 뷰티 특성 업데이트
        if (request.beautyType() != null) {
            MyFeatureUpdateRequestDto.BeautyTypeUpdate beautyType = request.beautyType();
            detail.updateBeautyFeatures(
                    joinTagList(beautyType.skinType()),
                    beautyType.skinBrightness(),
                    joinTagList(beautyType.makeupStyle()),
                    joinTagList(beautyType.interestCategories()),
                    joinTagList(beautyType.interestFunctions())
            );
            log.debug("뷰티 특성 업데이트 완료: userId={}", userId);
        }

        // 패션 특성 업데이트
        if (request.fashionType() != null) {
            MyFeatureUpdateRequestDto.FashionTypeUpdate fashionType = request.fashionType();

            // bodyStats 파싱 (예: "165cm/50kg" -> height, weight)
            BodyStats bodyStats = parseBodyStats(fashionType.bodyStats());

            detail.updateFashionFeatures(
                    bodyStats.height(),
                    bodyStats.weight(),
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
    }

    private MyFeatureResponseDto.BeautyType buildBeautyType(UserMatchingDetail detail) {
        // 뷰티 데이터가 있는지 확인
        if (detail.getSkinType() == null && detail.getSkinBrightness() == null
                && detail.getMakeupStyle() == null) {
            log.warn("뷰티 프로필 정보 없음: userId={}", detail.getUserId());
            return null;  // 또는 예외를 던지거나, 빈 객체 반환
        }

        log.info("뷰티 프로필 조회 - 피부타입: {}, 메이크업스타일: {}, 관심카테고리: {}",
                detail.getSkinType(), detail.getMakeupStyle(), detail.getInterestCategories());

        return new MyFeatureResponseDto.BeautyType(
                parseTagString(detail.getSkinType()),           // 피부타입
                detail.getSkinBrightness(),                     // 피부 밝기
                parseTagString(detail.getMakeupStyle()),        // 메이크업 스타일
                parseTagString(detail.getInterestCategories()), // 관심 카테고리
                parseTagString(detail.getInterestFunctions())   // 관심 기능
        );
    }

    private MyFeatureResponseDto.FashionType buildFashionType(UserMatchingDetail detail) {
        // 패션 데이터가 있는지 확인
        if (detail.getHeight() == null && detail.getWeight() == null
                && detail.getBodyShape() == null) {
            log.warn("패션 프로필 정보 없음: userId={}", detail.getUserId());
            return null;
        }

        log.info("패션 프로필 조회 - 키/몸무게: {}/{}, 체형: {}, 관심분야: {}",
                detail.getHeight(), detail.getWeight(), detail.getBodyShape(), detail.getInterestFields());

        return new MyFeatureResponseDto.FashionType(
                detail.getHeight() + "/" + detail.getWeight(),  // 키/몸무게
                detail.getBodyShape(),                          // 체형
                detail.getTopSize(),                            // 상의 사이즈
                detail.getBottomSize(),                         // 하의 사이즈
                parseTagString(detail.getInterestFields()),     // 관심분야
                parseTagString(detail.getInterestStyles()),     // 관심스타일
                parseTagString(detail.getInterestBrands())      // 관심브랜드
        );
    }

    private MyFeatureResponseDto.ContentsType buildContentsType(UserMatchingDetail detail) {
        // 콘텐츠 데이터가 있는지 확인
        if (detail.getViewerGender() == null && detail.getAvgVideoLength() == null
                && detail.getAvgViews() == null) {
            log.warn("콘텐츠 프로필 정보 없음: userId={}", detail.getUserId());
            return null;
        }

        log.info("콘텐츠 프로필 조회 - 시청자성별: {}, 콘텐츠형식: {}, 콘텐츠톤: {}",
                detail.getViewerGender(), detail.getContentFormats(), detail.getContentTones());

        return new MyFeatureResponseDto.ContentsType(
                parseTagString(detail.getViewerGender()),         // 시청자 성별
                parseTagString(detail.getViewerAge()),            // 시청자 연령대
                detail.getAvgVideoLength(),                       // 평균 영상 길이
                detail.getAvgViews(),                             // 평균 조회수
                parseTagString(detail.getContentFormats()),       // 콘텐츠 형식
                parseTagString(detail.getContentTones()),         // 콘텐츠 톤
                parseTagString(detail.getDesiredInvolvement()),   // 원하는 관여도
                parseTagString(detail.getDesiredUsageScope())     // 원하는 활용 범위
        );
    }

    /**
     * bodyStats 파싱 (예: "165cm/50kg" -> height, weight)
     */
    private BodyStats parseBodyStats(String bodyStats) {
        if (bodyStats == null) {
            return new BodyStats(null, null);
        }

        String[] stats = bodyStats.split("/");
        if (stats.length == 2) {
            return new BodyStats(stats[0].trim(), stats[1].trim());
        }

        return new BodyStats(null, null);
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
     * Internal record for body stats parsing result
     */
    private record BodyStats(String height, String weight) {}
}