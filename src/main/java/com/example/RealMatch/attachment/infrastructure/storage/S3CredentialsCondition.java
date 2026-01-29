package com.example.RealMatch.attachment.infrastructure.storage;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

public class S3CredentialsCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String accessKeyId = context.getEnvironment()
                .getProperty("app.s3.access-key-id");
        String secretAccessKey = context.getEnvironment()
                .getProperty("app.s3.secret-access-key");

        return StringUtils.hasText(accessKeyId) && StringUtils.hasText(secretAccessKey);
    }
}
