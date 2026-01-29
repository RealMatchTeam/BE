package com.example.RealMatch.attachment.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.s3")
public class S3Properties {

    private String bucketName;
    private String region;
    private boolean publicBucket = false;
    private int presignedUrlExpirationSeconds = 604800;
    private long maxImageSizeBytes = 10485760L;
    private long maxFileSizeBytes = 52428800L;
    private String keyPrefix = "attachment";
    private String accessKeyId;
    private String secretAccessKey;
}
