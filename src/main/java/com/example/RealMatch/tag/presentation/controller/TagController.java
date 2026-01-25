package com.example.RealMatch.tag.presentation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.global.presentation.code.GeneralSuccessCode;
import com.example.RealMatch.tag.application.service.TagContentService;
import com.example.RealMatch.tag.presentation.dto.response.ContentTagResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Tag(name = "Tag", description = "태그 조회 API")
public class TagController {

    private final TagContentService tagContentService;

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
