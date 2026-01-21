package com.example.RealMatch.chat.application.service.message;

import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatMatchedCampaignPayloadResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatProposalCardPayloadResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatProposalStatusNoticePayloadResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JacksonSystemMessagePayloadSerializer implements SystemMessagePayloadSerializer {

    private final ObjectMapper objectMapper;

    public JacksonSystemMessagePayloadSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String serialize(ChatSystemMessagePayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("System message payload is required.");
        }
        try {
            return objectMapper.writeValueAsString(payload);
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
            return objectMapper.readValue(rawPayload, resolvePayloadType(kind));
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to deserialize system message payload.", ex);
        }
    }

    private Class<? extends ChatSystemMessagePayload> resolvePayloadType(ChatSystemMessageKind kind) {
        return switch (kind) {
            case PROPOSAL_CARD -> ChatProposalCardPayloadResponse.class;
            case PROPOSAL_STATUS_NOTICE -> ChatProposalStatusNoticePayloadResponse.class;
            case MATCHED_CAMPAIGN_CARD -> ChatMatchedCampaignPayloadResponse.class;
        };
    }
}
