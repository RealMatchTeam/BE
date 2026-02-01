package com.example.RealMatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableRedisDocumentRepositories(basePackages = "com.example.RealMatch.match.infrastructure.redis.repository")
public class RealMatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(RealMatchApplication.class, args);
    }

}
