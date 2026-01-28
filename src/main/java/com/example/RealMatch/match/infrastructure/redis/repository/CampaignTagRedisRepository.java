package com.example.RealMatch.match.infrastructure.redis.repository;

import java.util.List;
import java.util.Optional;

import com.example.RealMatch.match.infrastructure.redis.document.CampaignTagDocument;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface CampaignTagRedisRepository extends RedisDocumentRepository<CampaignTagDocument, String> {

    Optional<CampaignTagDocument> findByCampaignId(Long campaignId);

    List<CampaignTagDocument> findByPreferredFashionTagsContaining(String tag);

    List<CampaignTagDocument> findByPreferredBeautyTagsContaining(String tag);

    List<CampaignTagDocument> findByPreferredContentTagsContaining(String tag);
}
