package com.example.RealMatch.match.infrastructure.redis.repository;

import java.util.List;
import java.util.Optional;

import com.example.RealMatch.match.infrastructure.redis.document.BrandTagDocument;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface BrandTagRedisRepository extends RedisDocumentRepository<BrandTagDocument, String> {

    Optional<BrandTagDocument> findByBrandId(Long brandId);

    List<BrandTagDocument> findByPreferredFashionTagsContaining(String tag);

    List<BrandTagDocument> findByPreferredBeautyTagsContaining(String tag);

    List<BrandTagDocument> findByPreferredContentTagsContaining(String tag);
}
