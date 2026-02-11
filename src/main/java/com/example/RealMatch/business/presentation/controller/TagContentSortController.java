package com.example.RealMatch.business.presentation.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.business.application.service.TagContentSortQueryService;
import com.example.RealMatch.business.presentation.dto.response.TagContentSortResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Business-ContentTag", description = "비즈니스 캠페인 콘텐츠 태그")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tag-contents/sort")
public class TagContentSortController {

    private final TagContentSortQueryService tagContentSortQueryService;

    @Operation(
            summary = "콘텐츠 태그 정렬 기준별 조회",
            description = """
                    콘텐츠 태그를 정렬 기준(ContentTagSort)별로 그룹화하여 조회합니다.
                    
                    [응답 구조]
                    - sort: 태그 정렬 기준 (FORMAT, CATEGORY, TONE, INVOLVEMENT, USAGE_RANGE)
                    - sortKorName: 정렬 기준 한글명 (형식, 종류, 톤, 관여도, 활용 범위)
                    - tags: 해당 정렬 기준에 속하는 태그 목록
                      - id: 태그 ID
                      - name: 태그 한글명
                    """
    )
    @GetMapping
    public List<TagContentSortResponse> getAll() {
        return tagContentSortQueryService.getAllGroupedBySort();
    }
}


