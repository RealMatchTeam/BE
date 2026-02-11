package com.example.RealMatch.match.infrastructure.redis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.example.RealMatch.match.infrastructure.redis.document.BrandTagDocument;
import com.example.RealMatch.match.infrastructure.redis.document.CampaignTagDocument;
import com.example.RealMatch.match.infrastructure.redis.document.UserTagDocument;
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

    private static final String BRAND_INDEX = "com.example.RealMatch.match.infrastructure.redis.document.BrandTagDocumentIdx";
    private static final String CAMPAIGN_INDEX = "com.example.RealMatch.match.infrastructure.redis.document.CampaignTagDocumentIdx";

    private static final int SEARCH_LIMIT = 200;

    public List<BrandTagDocument> findCandidateBrands(UserTagDocument userDoc) {
        String query = buildTagOverlapQuery(
                userDoc.getFashionTags(), "preferredFashionTags",
                userDoc.getBeautyTags(), "preferredBeautyTags",
                userDoc.getContentTags(), "preferredContentTags"
        );
        return executeSearch(BRAND_INDEX, query, BrandTagDocument.class);
    }

    public List<CampaignTagDocument> findCandidateCampaigns(UserTagDocument userDoc) {
        String query = buildTagOverlapQuery(
                userDoc.getFashionTags(), "preferredFashionTags",
                userDoc.getBeautyTags(), "preferredBeautyTags",
                userDoc.getContentTags(), "preferredContentTags"
        );
        return executeSearch(CAMPAIGN_INDEX, query, CampaignTagDocument.class);
    }

    @Deprecated
    public List<BrandTagDocument> findAllBrandTagDocuments() {
        return findAllDocuments(BRAND_PREFIX, BrandTagDocument.class);
    }

    @Deprecated
    public List<CampaignTagDocument> findAllCampaignTagDocuments() {
        return findAllDocuments(CAMPAIGN_PREFIX, CampaignTagDocument.class);
    }

    private String buildTagOverlapQuery(
            Set<Integer> fashionTags, String fashionField,
            Set<Integer> beautyTags, String beautyField,
            Set<Integer> contentTags, String contentField
    ) {
        List<String> clauses = new ArrayList<>();

        String fashionClause = buildTagClause(fashionTags, fashionField);
        if (fashionClause != null) clauses.add(fashionClause);

        String beautyClause = buildTagClause(beautyTags, beautyField);
        if (beautyClause != null) clauses.add(beautyClause);

        String contentClause = buildTagClause(contentTags, contentField);
        if (contentClause != null) clauses.add(contentClause);

        if (clauses.isEmpty()) {
            return "*";
        }

        return String.join(" | ", clauses);
    }

    private String buildTagClause(Set<Integer> tags, String fieldName) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        String tagValues = tags.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("|"));
        return "(@" + fieldName + ":{" + tagValues + "})";
    }

    private <T> List<T> executeSearch(String indexName, String query, Class<T> clazz) {
        List<T> results = new ArrayList<>();

        try {
            Object rawResult = redisTemplate.execute((RedisConnection connection) ->
                    connection.execute("FT.SEARCH",
                            indexName.getBytes(),
                            query.getBytes(),
                            "LIMIT".getBytes(),
                            "0".getBytes(),
                            String.valueOf(SEARCH_LIMIT).getBytes()
                    )
            );

            if (rawResult == null) {
                log.debug("FT.SEARCH returned null. index={}, query={}", indexName, query);
                return results;
            }

            results = parseFtSearchResult(rawResult, clazz);
            log.info("FT.SEARCH found {} candidates. index={}, query={}", results.size(), indexName, query);
        } catch (Exception e) {
            log.error("FT.SEARCH failed. index={}, query={}, error={}", indexName, query, e.getMessage());
            // FT.SEARCH 실패 시 레거시 방식으로 fallback
            log.warn("Falling back to KEYS scan for index: {}", indexName);
            if (BRAND_INDEX.equals(indexName)) {
                @SuppressWarnings("unchecked")
                List<T> fallback = (List<T>) findAllDocuments(BRAND_PREFIX, BrandTagDocument.class);
                return fallback;
            } else {
                @SuppressWarnings("unchecked")
                List<T> fallback = (List<T>) findAllDocuments(CAMPAIGN_PREFIX, CampaignTagDocument.class);
                return fallback;
            }
        }

        return results;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> parseFtSearchResult(Object rawResult, Class<T> clazz) {
        List<T> results = new ArrayList<>();

        if (!(rawResult instanceof List<?> resultList) || resultList.size() < 2) {
            return results;
        }

        // 첫 번째 요소는 총 결과 수 (Long)
        // 이후 [docKey, [fields...]] 쌍으로 반복
        for (int i = 1; i < resultList.size(); i += 2) {
            if (i + 1 >= resultList.size()) break;

            Object fieldsObj = resultList.get(i + 1);
            if (!(fieldsObj instanceof List<?> fields)) continue;

            // JSON 인덱스: fields = ["$", "{...json...}"]
            for (int j = 0; j < fields.size() - 1; j += 2) {
                String fieldName = decodeBytes(fields.get(j));
                if ("$".equals(fieldName)) {
                    String jsonString = decodeBytes(fields.get(j + 1));
                    if (jsonString != null) {
                        try {
                            T doc = objectMapper.readValue(jsonString, clazz);
                            if (doc != null) {
                                results.add(doc);
                            }
                        } catch (Exception e) {
                            log.warn("Failed to deserialize FT.SEARCH result: {}", e.getMessage());
                        }
                    }
                }
            }
        }

        return results;
    }

    private String decodeBytes(Object obj) {
        if (obj instanceof byte[] bytes) {
            return new String(bytes);
        }
        if (obj instanceof String s) {
            return s;
        }
        return null;
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
