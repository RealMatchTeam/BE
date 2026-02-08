package com.example.RealMatch.notification.application.service;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.example.RealMatch.notification.domain.entity.enums.NotificationKind;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;

/**
 * NotificationKind별 즉시 발송 채널을 결정한다.
 *
 * PRD §5.2 기준:
 * - PROPOSAL_RECEIVED : PUSH만 (이메일은 1일 후 스케줄러에서 별도 처리)
 * - PROPOSAL_SENT     : PUSH만
 * - CAMPAIGN_APPLIED  : PUSH만
 * - CAMPAIGN_MATCHED  : PUSH + EMAIL (매칭 직후 즉시 이메일)
 * - CAMPAIGN_COMPLETED: PUSH + EMAIL (데모데이 이후)
 * - SETTLEMENT_READY  : PUSH만
 * - AUTO_CONFIRMED    : EMAIL만  (데모데이 이후)
 * - CHAT_MESSAGE      : PUSH만
 */
@Component
public class NotificationChannelResolver {

    private static final Map<NotificationKind, Set<NotificationChannel>> CHANNEL_MAP;

    static {
        Map<NotificationKind, Set<NotificationChannel>> map = new EnumMap<>(NotificationKind.class);

        map.put(NotificationKind.PROPOSAL_RECEIVED,
                EnumSet.of(NotificationChannel.PUSH));
        map.put(NotificationKind.PROPOSAL_SENT,
                EnumSet.of(NotificationChannel.PUSH));
        map.put(NotificationKind.CAMPAIGN_APPLIED,
                EnumSet.of(NotificationChannel.PUSH));
        map.put(NotificationKind.CAMPAIGN_MATCHED,
                EnumSet.of(NotificationChannel.PUSH, NotificationChannel.EMAIL));
        map.put(NotificationKind.CAMPAIGN_COMPLETED,
                EnumSet.of(NotificationChannel.PUSH, NotificationChannel.EMAIL));
        map.put(NotificationKind.SETTLEMENT_READY,
                EnumSet.of(NotificationChannel.PUSH));
        map.put(NotificationKind.AUTO_CONFIRMED,
                EnumSet.of(NotificationChannel.EMAIL));
        map.put(NotificationKind.CHAT_MESSAGE,
                EnumSet.of(NotificationChannel.PUSH));

        CHANNEL_MAP = Collections.unmodifiableMap(map);
    }

    public Set<NotificationChannel> resolveChannels(NotificationKind kind) {
        return CHANNEL_MAP.getOrDefault(kind, Collections.emptySet());
    }
}
