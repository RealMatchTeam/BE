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

    // private 버킷
    private String bucketName;
    // PUBLIC 버킷 (CloudFront 원본)
    private String publicBucketName;
    // CloudFront 배포 도메인
    private String cloudfrontBaseUrl;

    private String region;
    private int presignedUrlExpirationSeconds = 86400;
    private long maxImageSizeBytes = 10485760L;
    private long maxFileSizeBytes = 52428800L;
    private String keyPrefix = "attachment";
    private String accessKeyId;
    private String secretAccessKey;
}
