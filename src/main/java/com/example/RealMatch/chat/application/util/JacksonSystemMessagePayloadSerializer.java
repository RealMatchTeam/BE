package com.example.RealMatch.chat.application.util;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.dto.response.ChatApplyCardPayloadResponse;
import com.example.RealMatch.chat.application.dto.response.ChatApplyStatusNoticePayloadResponse;
import com.example.RealMatch.chat.application.dto.response.ChatMatchedCampaignPayloadResponse;
import com.example.RealMatch.chat.application.dto.response.ChatProposalCardPayloadResponse;
import com.example.RealMatch.chat.application.dto.response.ChatProposalStatusNoticePayloadResponse;
import com.example.RealMatch.chat.application.dto.response.ChatSystemMessagePayload;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JacksonSystemMessagePayloadSerializer implements SystemMessagePayloadSerializer {

    private final ObjectMapper objectMapper;

    @Override
    public String serialize(ChatSystemMessagePayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("System message payload is required.");
        }
        try {
            return objectMapper.writeValueAsString(Map.of("schemaVersion", 1, "payload", payload));
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to serialize system message payload.", ex);
        }
    }

    @Override
    public ChatSystemMessagePayload deserialize(ChatSystemMessageKind kind, String rawPayload) {
        if (kind == null) {
            throw new IllegalArgumentException("System message kind is required.");
        }
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new IllegalArgumentException("System message payload is required.");
        }
        try {
            var root = objectMapper.readTree(rawPayload);
            if (root == null || !root.isObject()) {
                throw new IllegalArgumentException("System payload must be a JSON object");
            }
            if (root.has("schemaVersion")) {
                if (root.path("schemaVersion").asInt(-1) != 1 || !root.has("payload")) {
                    throw new IllegalArgumentException("Unsupported system payload schema");
                }
                root = root.get("payload");
            }
            if (!root.isObject()) {
                throw new IllegalArgumentException("System payload must be a JSON object");
            }
            return objectMapper.treeToValue(root, resolvePayloadType(kind));
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to deserialize system message payload.", ex);
        }
    }

    private Class<? extends ChatSystemMessagePayload> resolvePayloadType(ChatSystemMessageKind kind) {
        return switch (kind) {
            case PROPOSAL_CARD, RE_PROPOSAL_CARD -> ChatProposalCardPayloadResponse.class;
            case PROPOSAL_STATUS_NOTICE -> ChatProposalStatusNoticePayloadResponse.class;
            case MATCHED_CAMPAIGN_CARD -> ChatMatchedCampaignPayloadResponse.class;
            case APPLY_CARD -> ChatApplyCardPayloadResponse.class;
            case APPLY_STATUS_NOTICE -> ChatApplyStatusNoticePayloadResponse.class;
        };
    }
}
