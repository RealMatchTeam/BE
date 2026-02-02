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

    private MyFeatureResponseDto.BeautyType buildBeautyType(UserMatchingDetail detail, Map<String, List<String>> tagsByCategory) {
        Map<String, List<String>> tags = tagsByCategory != null ? tagsByCategory : Collections.emptyMap();

        if (detail.getSkinBrightness() == null && tags.isEmpty()) {
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
}
