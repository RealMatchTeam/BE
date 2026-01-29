package com.example.RealMatch.tag.presentation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.global.presentation.code.GeneralSuccessCode;
import com.example.RealMatch.tag.application.service.TagContentService;
import com.example.RealMatch.tag.application.service.TagService;
import com.example.RealMatch.tag.presentation.dto.response.ContentTagResponse;
import com.example.RealMatch.tag.presentation.dto.response.TagResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Tag(name = "Tag", description = "태그 조회 API")
public class TagController {

    private final TagService tagService;
    private final TagContentService tagContentService;

    @GetMapping("/fashion")
    @Operation(
            summary = "패션 태그 조회",
            description = "패션 태그 목록을 카테고리별로 조회합니다."
    )
    public CustomResponse<TagResponse> getFashionTags() {
        return CustomResponse.onSuccess(
                GeneralSuccessCode.FOUND,
                tagService.getFashionTags()
        );
    }

    @GetMapping("/beauty")
    @Operation(
            summary = "뷰티 태그 조회",
            description = "뷰티 태그 목록을 카테고리별로 조회합니다."
    )
    public CustomResponse<TagResponse> getBeautyTags() {
        return CustomResponse.onSuccess(
                GeneralSuccessCode.FOUND,
                tagService.getBeautyTags()
        );
    }

    @GetMapping("/{tagType}")
    @Operation(
            summary = "태그 타입별 조회",
            description = "지정된 태그 타입의 목록을 카테고리별로 조회합니다."
    )
    public CustomResponse<TagResponse> getTagsByType(
            @PathVariable String tagType
    ) {
        return CustomResponse.onSuccess(
                GeneralSuccessCode.FOUND,
                tagService.getTagsByType(tagType)
        );
    }

    @GetMapping("/content")
    @Operation(
            summary = "컨텐츠 태그 조회",
            description = "컨텐츠 태그 목록을 조회합니다."
    )
    public CustomResponse<ContentTagResponse> getContentTags() {

        return CustomResponse.onSuccess(
                GeneralSuccessCode.FOUND,
                tagContentService.getContentTags()
        );
    }
}
