package com.example.RealMatch.attachment.infrastructure.storage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class S3Config {

    private final S3Properties s3Properties;

    @Bean
    public S3Client s3Client() {
        String accessKeyId = s3Properties.getAccessKeyId();
        String secretAccessKey = s3Properties.getSecretAccessKey();
        
        if (!StringUtils.hasText(accessKeyId) || !StringUtils.hasText(secretAccessKey)) {
            throw new IllegalStateException("S3 자격증명이 설정되지 않았습니다. application.yml에 access-key-id와 secret-access-key를 설정해주세요.");
        }
        
        log.info("S3Client 생성 - accessKeyId: {}..., region: {}", 
                accessKeyId.substring(0, Math.min(10, accessKeyId.length())),
                s3Properties.getRegion());

        return S3Client.builder()
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                ))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        String accessKeyId = s3Properties.getAccessKeyId();
        String secretAccessKey = s3Properties.getSecretAccessKey();
        
        if (!StringUtils.hasText(accessKeyId) || !StringUtils.hasText(secretAccessKey)) {
            throw new IllegalStateException("S3 자격증명이 설정되지 않았습니다. application.yml에 access-key-id와 secret-access-key를 설정해주세요.");
        }

        return S3Presigner.builder()
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                ))
                .build();
    }
}
