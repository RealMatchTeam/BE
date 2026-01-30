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
import com.example.RealMatch.user.presentation.dto.response.MyFeatureResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFeatureService {

    private final UserMatchingDetailRepository userMatchingDetailRepository;
    // 다른 필요한 리포지토리나 서비스 주입 예정

    public MyFeatureResponseDto getMyFeatures(Long userId) {

        // UserMatchingDetail 조회
        UserMatchingDetail detail = userMatchingDetailRepository.findByUserId(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND)); // 수정 예정

        log.info("사용자 프로필 조회 완료: userId={}", userId);

        // 각 영역별로 데이터 존재 여부 확인 및 조회
        MyFeatureResponseDto.BeautyType beautyType = buildBeautyType(detail);
        MyFeatureResponseDto.FashionType fashionType = buildFashionType(detail);
        MyFeatureResponseDto.ContentsType contentsType = buildContentsType(detail);

        return new MyFeatureResponseDto(beautyType, fashionType, contentsType);
    }

    private MyFeatureResponseDto.BeautyType buildBeautyType(UserMatchingDetail detail) {
        // 뷰티 데이터가 있는지 확인
        if (detail.getSkinType() == null && detail.getSkinBrightness() == null
                && detail.getMakeupStyle() == null) {
            log.warn("뷰티 프로필 정보 없음: userId={}", detail.getUserId());
            return null;  // 또는 예외를 던지거나, 빈 객체 반환
        }

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

        return new MyFeatureResponseDto.FashionType(
                detail.getHeight() + "/" + detail.getWeight(),  // 키/몸무게
                detail.getBodyShape(),                          // 체형
                detail.getUpperSize(),                          // 상의 사이즈
                detail.getLowerSize(),                          // 하의 사이즈
                Collections.emptyList(),  // 관심분야 - user_matching_detail에 필드가 없음
                Collections.emptyList(),  // 관심스타일 - user_matching_detail에 필드가 없음
                Collections.emptyList()   // 관심브랜드 - user_matching_detail에 필드가 없음
        );
    }

    private MyFeatureResponseDto.ContentsType buildContentsType(UserMatchingDetail detail) {
        // 콘텐츠 데이터가 있는지 확인
        if (detail.getViewerGender() == null && detail.getVideoLength() == null
                && detail.getViews() == null) {
            log.warn("콘텐츠 프로필 정보 없음: userId={}", detail.getUserId());
            return null;
        }

        return new MyFeatureResponseDto.ContentsType(
                parseTagString(detail.getViewerGender()),      // 시청자 성별
                parseTagString(detail.getViewerAge()),         // 시청자 연령대
                detail.getVideoLength(),                       // 영상 길이
                detail.getViews(),                             // 조회수
                parseTagString(detail.getContentFormats()),    // 콘텐츠 형식
                parseTagString(detail.getContentTones()),      // 콘텐츠 톤
                detail.getDesiredInvolvement(),                // 원하는 관여도
                detail.getDesiredUsageScope()                  // 원하는 활용 범위
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
                .collect(Collectors.toList());
    }
}
