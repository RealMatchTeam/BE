package com.example.RealMatch.match.infrastructure.redis.repository;

import java.util.List;
import java.util.Optional;

import com.redis.om.spring.repository.RedisDocumentRepository;

import com.example.RealMatch.match.infrastructure.redis.document.UserTagDocument;

public interface UserTagRedisRepository extends RedisDocumentRepository<UserTagDocument, String> {

    Optional<UserTagDocument> findByUserId(Long userId);

    List<UserTagDocument> findByFashionTagsContaining(String tag);

    List<UserTagDocument> findByBeautyTagsContaining(String tag);

    List<UserTagDocument> findByContentTagsContaining(String tag);
}
