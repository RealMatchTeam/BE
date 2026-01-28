package com.example.RealMatch.attachment.presentation.swagger;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.example.RealMatch.attachment.domain.enums.AttachmentType;
import com.example.RealMatch.attachment.presentation.dto.response.AttachmentUploadResponse;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Attachment", description = "첨부파일 REST API")
@RequestMapping("/api/v1/attachments")
public interface AttachmentSwagger {

    @Operation(summary = "첨부파일 업로드 API",
            description = "첨부 파일을 업로드하고 메타 정보를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "첨부파일 업로드 성공"),
            @ApiResponse(responseCode = "COMMON400_1", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "COMMON401_1", description = "인증이 필요합니다."),
            @ApiResponse(responseCode = "ATTACHMENT400_1", description = "유효하지 않은 파일입니다."),
            @ApiResponse(responseCode = "ATTACHMENT400_2", description = "유효하지 않은 파일명입니다."),
            @ApiResponse(responseCode = "ATTACHMENT400_3", description = "유효하지 않은 파일 크기입니다."),
            @ApiResponse(responseCode = "ATTACHMENT400_4", description = "파일 크기가 제한을 초과했습니다."),
            @ApiResponse(responseCode = "ATTACHMENT400_5", description = "지원하지 않는 이미지 형식입니다."),
            @ApiResponse(responseCode = "ATTACHMENT500_1", description = "파일 업로드에 실패했습니다.")
    })
    CustomResponse<AttachmentUploadResponse> uploadAttachment(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "첨부 파일 타입 (IMAGE 또는 FILE)") @Valid @RequestParam("attachmentType") AttachmentType attachmentType,
            @Parameter(description = "업로드할 파일") @RequestPart(value = "file", required = true) MultipartFile file
    ) throws IOException;
}
