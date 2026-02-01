package com.example.RealMatch.user.application.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.tag.domain.entity.Tag;
import com.example.RealMatch.tag.domain.entity.UserTag;
import com.example.RealMatch.tag.domain.repository.UserTagRepository;
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
    private final UserTagRepository userTagRepository;

    public MyFeatureResponseDto getMyFeatures(Long userId) {

        UserMatchingDetail detail = userMatchingDetailRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_MATCHING_DETAIL_NOT_FOUND));

        List<UserTag> userTags = userTagRepository.findAllByUserIdWithTag(userId);

        Map<String, Map<String, List<String>>> tagsByTypeAndCategory = userTags.stream()
                .map(UserTag::getTag)
                .collect(Collectors.groupingBy(
                        Tag::getTagType,
                        Collectors.groupingBy(
                                Tag::getTagCategory,
                                Collectors.mapping(Tag::getTagName, Collectors.toList())
                        )
                ));

        log.info("사용자 프로필 조회 완료: userId={}, 태그 수={}", userId, userTags.size());

        MyFeatureResponseDto.BeautyType beautyType = buildBeautyType(detail, tagsByTypeAndCategory.get("뷰티"));
        MyFeatureResponseDto.FashionType fashionType = buildFashionType(detail, tagsByTypeAndCategory.get("패션"));
        MyFeatureResponseDto.ContentsType contentsType = buildContentsType(detail, tagsByTypeAndCategory.get("콘텐츠"));

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

        try {
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
                String height = null;
                String weight = null;
                if (fashionType.bodyStats() != null) {
                    String[] stats = fashionType.bodyStats().split("/");
                    if (stats.length == 2) {
                        height = stats[0].trim();
                        weight = stats[1].trim();
                    }
                }

                detail.updateFashionFeatures(
                        height,
                        weight,
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
                        contentsType.desiredInvolvement(),
                        contentsType.desiredUsageScope()
                );
                log.debug("콘텐츠 특성 업데이트 완료: userId={}", userId);
            }

            // JPA 더티 체킹으로 자동 저장
            log.info("사용자 특성 정보 업데이트 완료: userId={}", userId);

        } catch (Exception e) {
            log.error("사용자 특성 정보 업데이트 실패: userId={}, error={}", userId, e.getMessage(), e);
            throw new CustomException(UserErrorCode.TRAIT_UPDATE_FAILED);
        }
    }

    private MyFeatureResponseDto.BeautyType buildBeautyType(UserMatchingDetail detail) {
        // 뷰티 데이터가 있는지 확인
        if (detail.getSkinType() == null && detail.getSkinBrightness() == null
                && detail.getMakeupStyle() == null) {
            log.warn("뷰티 프로필 정보 없음: userId={}", detail.getUserId());
            return null;
        }

        log.info("뷰티 프로필 조회 - userId={}, 태그 카테고리: {}", detail.getUserId(), tags.keySet());

        return new MyFeatureResponseDto.BeautyType(
                getTagsOrEmpty(tags, "피부타입"),
                detail.getSkinBrightness(),
                getTagsOrEmpty(tags, "메이크업스타일"),
                getTagsOrEmpty(tags, "관심카테고리"),
                getTagsOrEmpty(tags, "관심기능")
        );
    }

    private MyFeatureResponseDto.FashionType buildFashionType(UserMatchingDetail detail, Map<String, List<String>> tagsByCategory) {
        Map<String, List<String>> tags = tagsByCategory != null ? tagsByCategory : Collections.emptyMap();

        if (detail.getHeight() == null && detail.getWeight() == null
                && detail.getBodyShape() == null && tags.isEmpty()) {
            log.warn("패션 프로필 정보 없음: userId={}", detail.getUserId());
            return null;
        }

        log.info("패션 프로필 조회 - 키/몸무게: {}/{}, 체형: {}, 태그 카테고리: {}",
                detail.getHeight(), detail.getWeight(), detail.getBodyShape(), tags.keySet());

        return new MyFeatureResponseDto.FashionType(
                detail.getHeight() + "/" + detail.getWeight(),
                detail.getBodyShape(),
                detail.getUpperSize(),
                detail.getLowerSize(),
                getTagsOrEmpty(tags, "관심분야"),
                getTagsOrEmpty(tags, "관심스타일"),
                getTagsOrEmpty(tags, "관심브랜드")
        );
    }

    private MyFeatureResponseDto.ContentsType buildContentsType(UserMatchingDetail detail, Map<String, List<String>> tagsByCategory) {
        Map<String, List<String>> tags = tagsByCategory != null ? tagsByCategory : Collections.emptyMap();

        if (detail.getVideoLength() == null && detail.getViews() == null && tags.isEmpty()) {
            log.warn("콘텐츠 프로필 정보 없음: userId={}", detail.getUserId());
            return null;
        }

        log.info("콘텐츠 프로필 조회 - 영상길이: {}, 조회수: {}, 태그 카테고리: {}",
                detail.getVideoLength(), detail.getViews(), tags.keySet());

        return new MyFeatureResponseDto.ContentsType(
                getTagsOrEmpty(tags, "시청자성별"),
                getTagsOrEmpty(tags, "시청자연령대"),
                detail.getVideoLength(),
                detail.getViews(),
                getTagsOrEmpty(tags, "콘텐츠형식"),
                getTagsOrEmpty(tags, "콘텐츠톤"),
                detail.getDesiredInvolvement(),
                detail.getDesiredUsageScope()
        );
    }

    private List<String> getTagsOrEmpty(Map<String, List<String>> tags, String category) {
        return tags.getOrDefault(category, Collections.emptyList());
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
