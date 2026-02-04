package com.example.RealMatch.chat.application.idempotency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * 실패한 이벤트를 Dead Letter Queue에 저장하는 서비스.
 * Redis List를 사용하여 실패한 이벤트를 저장합니다.
 */
@Component
@RequiredArgsConstructor
public class FailedEventDlq {

    private static final Logger LOG = LoggerFactory.getLogger(FailedEventDlq.class);
    private static final String DLQ_KEY = "chat:dlq";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void enqueueFailedEvent(
            String eventType,
            String eventId,
            Long roomId,
            String error,
            Object additionalData
    ) {
        try {
            FailedEventDlqEntry entry = new FailedEventDlqEntry(
                    eventType,
                    eventId,
                    roomId,
                    error,
                    additionalData,
                    java.time.LocalDateTime.now().toString()
            );

            String json = objectMapper.writeValueAsString(entry);
            redisTemplate.opsForList().rightPush(DLQ_KEY, json);

            LOG.warn("[DLQ] Failed event enqueued. eventType={}, eventId={}, roomId={}, error={}",
                    eventType, eventId, roomId, error);

        } catch (JsonProcessingException ex) {
            LOG.error("[DLQ] Failed to serialize failed event. eventType={}, eventId={}, roomId={}",
                    eventType, eventId, roomId, ex);
        } catch (Exception ex) {
            LOG.error("[DLQ] Failed to enqueue failed event. eventType={}, eventId={}, roomId={}",
                    eventType, eventId, roomId, ex);
        }
    }

    public record FailedEventDlqEntry(
            String eventType,
            String eventId,
            Long roomId,
            String error,
            Object additionalData,
            String timestamp
    ) {
    }
}
