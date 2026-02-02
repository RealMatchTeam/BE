package com.example.RealMatch.attachment.application.service;

import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.example.RealMatch.attachment.application.policy.AttachmentUploadPolicy;
import com.example.RealMatch.attachment.application.util.FileValidator;
import com.example.RealMatch.attachment.code.AttachmentErrorCode;
import com.example.RealMatch.attachment.domain.enums.AttachmentType;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentValidationService {

    private final AttachmentUploadPolicy uploadPolicy;
    private final FileValidator fileValidator;

    public String validateUploadRequest(
            String originalFilename,
            String contentType,
            long fileSize,
            AttachmentType attachmentType
    ) {
        String normalizedContentType = normalizeContentType(contentType);

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new CustomException(AttachmentErrorCode.INVALID_FILE_NAME);
        }
        if (normalizedContentType == null) {
            throw new CustomException(AttachmentErrorCode.INVALID_CONTENT_TYPE);
        }

        fileValidator.validateFileName(originalFilename);

        long maxSize = attachmentType == AttachmentType.IMAGE
                ? uploadPolicy.getMaxImageSizeBytes()
                : uploadPolicy.getMaxFileSizeBytes();
        fileValidator.validateFileSize(fileSize, maxSize);

        if (attachmentType == AttachmentType.IMAGE) {
            fileValidator.validateImageFile(
                    normalizedContentType,
                    originalFilename,
                    uploadPolicy.getAllowedImageContentTypes(),
                    uploadPolicy.getAllowedImageExtensions()
            );
        } else if (attachmentType == AttachmentType.FILE) {
            fileValidator.validateAttachmentFile(
                    normalizedContentType,
                    originalFilename,
                    uploadPolicy.getAllowedFileContentTypes(),
                    uploadPolicy.getAllowedFileExtensions()
            );
        }
        return normalizedContentType;
    }

    // DB·S3·허용목록 비교를 일관되게 하기 위해 type/subtype만 소문자로 통일. 파라미터(charset 등)는 유지.
    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return null;
        }
        String trimmed = contentType.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        int semicolon = trimmed.indexOf(';');
        String mainPart = semicolon < 0 ? trimmed : trimmed.substring(0, semicolon).trim();
        String paramsPart = semicolon < 0 ? "" : trimmed.substring(semicolon).trim();
        if (mainPart.isEmpty()) {
            return null;
        }
        String[] typeSubtype = mainPart.split("/", 2);
        String type = typeSubtype[0].trim().toLowerCase(Locale.ROOT);
        String subtype = typeSubtype.length > 1 ? typeSubtype[1].trim().toLowerCase(Locale.ROOT) : "";
        if (type.isEmpty() || subtype.isEmpty()) {
            return null;
        }
        String normalizedMain = type + "/" + subtype;
        if (paramsPart.isEmpty()) {
            return normalizedMain;
        }
        String params = collapseRepeatedSemicolons(paramsPart.trim());
        return normalizedMain + "; " + params;
    }

    private static final Pattern REPEATED_SEMICOLONS = Pattern.compile(";+");

    private static String collapseRepeatedSemicolons(String params) {
        return REPEATED_SEMICOLONS.matcher(params).replaceAll(";").replaceFirst("^;+", "").trim();
    }
}
