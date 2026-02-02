package com.example.RealMatch.tag.presentation.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ContentTagResponse {
    private List<TagItemResponse> viewerGenders;   // VIEWER_GENDER (주 시청자 성별)
    private List<TagItemResponse> viewerAges;      // VIEWER_AGE (주 시청자 나이대)
    private List<TagItemResponse> avgVideoLengths; // AVG_VIDEO_LENGTH (평균 영상 길이)
    private List<TagItemResponse> avgVideoViews;   // AVG_VIDEO_VIEWS (평균 영상 조회수)
    private List<TagItemResponse> formats;         // FORMAT
    private List<TagItemResponse> categories;      // CATEGORY
    private List<TagItemResponse> tones;           // TONE
    private List<TagItemResponse> involvements;    // INVOLVEMENT
    private List<TagItemResponse> usageRanges;     // USAGE_RANGE

    public record TagItemResponse(
            Long id,
            String name        // kor_name
    ) {}
}
