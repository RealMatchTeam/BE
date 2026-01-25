package com.example.RealMatch.match.infrastructure.redis.repository;

import java.util.List;
import java.util.Optional;

import com.redis.om.spring.repository.RedisDocumentRepository;

import com.example.RealMatch.match.infrastructure.redis.document.CampaignTagDocument;

public interface CampaignTagRedisRepository extends RedisDocumentRepository<CampaignTagDocument, String> {

    Optional<CampaignTagDocument> findByCampaignId(Long campaignId);

    List<CampaignTagDocument> findByRequiredFashionTagsContaining(String tag);

    List<CampaignTagDocument> findByRequiredBeautyTagsContaining(String tag);

    List<CampaignTagDocument> findByRequiredContentTagsContaining(String tag);
}
