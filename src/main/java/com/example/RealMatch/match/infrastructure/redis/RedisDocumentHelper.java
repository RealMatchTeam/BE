package com.example.RealMatch.match.infrastructure.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.example.RealMatch.match.infrastructure.redis.document.BrandTagDocument;
import com.example.RealMatch.match.infrastructure.redis.document.CampaignTagDocument;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDocumentHelper {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String BRAND_PREFIX = "com.example.RealMatch.match.infrastructure.redis.document.BrandTagDocument:";
    private static final String CAMPAIGN_PREFIX = "com.example.RealMatch.match.infrastructure.redis.document.CampaignTagDocument:";

    public List<BrandTagDocument> findAllBrandTagDocuments() {
        return findAllDocuments(BRAND_PREFIX, BrandTagDocument.class);
    }

    public List<CampaignTagDocument> findAllCampaignTagDocuments() {
        return findAllDocuments(CAMPAIGN_PREFIX, CampaignTagDocument.class);
    }

    private <T> List<T> findAllDocuments(String prefix, Class<T> clazz) {
        List<T> results = new ArrayList<>();

        try {
            Set<String> keys = redisTemplate.keys(prefix + "*");
            if (keys == null || keys.isEmpty()) {
                log.debug("No keys found with prefix: {}", prefix);
                return results;
            }

            for (String key : keys) {
                try {
                    T document = getJsonDocument(key, clazz);
                    if (document != null) {
                        results.add(document);
                    }
                } catch (Exception e) {
                    log.warn("Failed to read document from key {}: {}", key, e.getMessage());
                }
            }

            log.info("Found {} documents with prefix {}", results.size(), prefix);
        } catch (Exception e) {
            log.error("Failed to scan Redis keys with prefix {}: {}", prefix, e.getMessage());
        }

        return results;
    }

    private <T> T getJsonDocument(String key, Class<T> clazz) {
        try {
            String jsonString = redisTemplate.execute((RedisConnection connection) -> {

                Object result = connection.execute("JSON.GET", key.getBytes(), "$".getBytes());
                if (result instanceof byte[]) {
                    return new String((byte[]) result);
                }
                return null;
            });

            if (jsonString != null && !jsonString.isEmpty()) {
                if (jsonString.startsWith("[") && jsonString.endsWith("]")) {
                    jsonString = jsonString.substring(1, jsonString.length() - 1);
                }
                return objectMapper.readValue(jsonString, clazz);
            }
        } catch (Exception e) {
            log.warn("Failed to get JSON document from key {}: {}", key, e.getMessage());
        }
        return null;
    }
}
