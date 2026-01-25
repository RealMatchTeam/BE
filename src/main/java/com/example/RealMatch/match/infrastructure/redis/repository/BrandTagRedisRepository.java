package com.example.RealMatch.match.infrastructure.redis.repository;

import java.util.List;
import java.util.Optional;

import com.redis.om.spring.repository.RedisDocumentRepository;

import com.example.RealMatch.match.infrastructure.redis.document.BrandTagDocument;

public interface BrandTagRedisRepository extends RedisDocumentRepository<BrandTagDocument, String> {

    Optional<BrandTagDocument> findByBrandId(Long brandId);

    List<BrandTagDocument> findByIndustryType(String industryType);

    List<BrandTagDocument> findByPreferredFashionTagsContaining(String tag);

    List<BrandTagDocument> findByPreferredBeautyTagsContaining(String tag);

    List<BrandTagDocument> findByPreferredContentTagsContaining(String tag);
}
