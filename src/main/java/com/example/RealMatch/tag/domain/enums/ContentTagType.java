package com.example.RealMatch.tag.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentTagType {
    VIEWER_GENDER("시청자 성별"),
    VIEWER_AGE("시청자 나이대"),
    AVG_VIDEO_LENGTH("평균 영상 길이"),
    AVG_VIDEO_VIEWS("영상 조회수"),
    FORMAT("콘텐츠 유형"),
    CATEGORY("콘텐츠 종류"),
    TONE("콘텐츠 톤"),
    INVOLVEMENT("콘텐츠 희망 관여도"),
    USAGE_RANGE("콘텐츠 희망 활용 범위"),
    ETC("기타");

    private final String korName;
}
