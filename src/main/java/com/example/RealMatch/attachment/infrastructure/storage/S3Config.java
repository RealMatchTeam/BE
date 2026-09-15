package com.example.RealMatch.attachment.infrastructure.storage;

import java.net.URI;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
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
@Conditional(S3CredentialsCondition.class)
public class S3Config {

    private final S3Properties s3Properties;

    private StaticCredentialsProvider createCredentialsProvider() {
        String accessKeyId = s3Properties.getAccessKeyId();
        String secretAccessKey = s3Properties.getSecretAccessKey();

        if (!StringUtils.hasText(accessKeyId) || !StringUtils.hasText(secretAccessKey)) {
            throw new IllegalStateException("S3 자격증명이 설정되지 않았습니다. application.yml에 access-key-id와 secret-access-key를 설정해주세요.");
        }

        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)
        );
    }

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .httpClientBuilder(software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(5))
                        .socketTimeout(Duration.ofSeconds(10)))
                .overrideConfiguration(config -> config
                        .apiCallTimeout(Duration.ofSeconds(30))
                        .apiCallAttemptTimeout(Duration.ofSeconds(10)))
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(createCredentialsProvider());
        if (StringUtils.hasText(s3Properties.getEndpoint())) {
            builder.endpointOverride(URI.create(s3Properties.getEndpoint()));
            builder.forcePathStyle(s3Properties.isPathStyleAccessEnabled());
        }
        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        var builder = S3Presigner.builder()
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(createCredentialsProvider());
        if (StringUtils.hasText(s3Properties.getEndpoint())) {
            builder.endpointOverride(URI.create(s3Properties.getEndpoint()));
        }
        return builder.build();
    }
}
