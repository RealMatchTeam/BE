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

        // UserMatchingDetail 조회 (삭제되지 않은 데이터만 조회)
        UserMatchingDetail detail = userMatchingDetailRepository.findByUserIdAndIsDeprecatedFalse(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_MATCHING_DETAIL_NOT_FOUND));

        log.info("사용자 프로필 조회 완료: userId={}, creatorType={}", userId, detail.getCreatorType());

        // 각 영역별로 데이터 존재 여부 확인 및 조회
        MyFeatureResponseDto.BeautyType beautyType = buildBeautyType(detail);
        MyFeatureResponseDto.FashionType fashionType = buildFashionType(detail);
        MyFeatureResponseDto.ContentsType contentsType = buildContentsType(detail);

        // creatorType(매칭 결과) 포함하여 반환
        return new MyFeatureResponseDto(detail.getCreatorType(), beautyType, fashionType, contentsType);
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
    }

    /**
     * 재검사 준비: 기존 데이터를 Soft Delete 처리하고 새로운 데이터 생성
     * 재검사 '시작' 버튼을 누르거나, 검사 결과를 제출하기 직전에 호출
     */
    @Transactional
    public void resetForReexamination(Long userId) { // matchService에서 호출 연동 예정
        // 1. 기존 활성 데이터 조회 및 삭제 처리 (Soft Delete)
        userMatchingDetailRepository.findByUserIdAndIsDeprecatedFalse(userId)
                .ifPresent(detail -> {
                    detail.deprecated(); // isDeprecated = true 설정
                    log.info("기존 사용자 매칭 정보 삭제 처리(Soft Delete): userId={}, detailId={}", userId, detail.getId());
                });

        // 2. 새로운 빈 프로필 생성 및 저장
        UserMatchingDetail newDetail = UserMatchingDetail.builder()
                .userId(userId)
                .build();

        userMatchingDetailRepository.save(newDetail);
        log.info("재검사를 위한 신규 프로필 생성 완료: userId={}", userId);
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
                detail.getSnsUrl(),
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
}
