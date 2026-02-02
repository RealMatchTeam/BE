package com.example.RealMatch.attachment.infrastructure.storage;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * S3 자격증명이 설정되지 않았을 때 true
 * NoOpS3FileUploadService 등록용
 */
public class S3CredentialsMissingCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String accessKeyId = context.getEnvironment()
                .getProperty("app.s3.access-key-id");
        String secretAccessKey = context.getEnvironment()
                .getProperty("app.s3.secret-access-key");

        return !StringUtils.hasText(accessKeyId) || !StringUtils.hasText(secretAccessKey);
    }
}
