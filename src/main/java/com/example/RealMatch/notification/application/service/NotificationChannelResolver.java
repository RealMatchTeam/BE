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
 * <p>PRD §5.2 기준:
 * <ul>
 *   <li>PROPOSAL_RECEIVED : PUSH + EMAIL (현재는 즉시 발송. 데모데이 이후에는 이메일만 1일 후 스케줄러로 변경 예정)</li>
 *   <li>PROPOSAL_SENT     : PUSH만</li>
 *   <li>CAMPAIGN_APPLIED  : PUSH만</li>
 *   <li>CAMPAIGN_MATCHED  : PUSH + EMAIL (매칭 직후 즉시 이메일)</li>
 *   <li>CAMPAIGN_COMPLETED: PUSH + EMAIL (데모데이 이후)</li>
 *   <li>SETTLEMENT_READY  : PUSH만</li>
 *   <li>AUTO_CONFIRMED    : EMAIL만 (데모데이 이후)</li>
 *   <li>CHAT_MESSAGE      : PUSH만</li>
 * </ul>
 *
 * TODO(데모데이 이후): PROPOSAL_RECEIVED 이메일은 즉시 발송 대신, 1일 경과 + 미읽음(isRead=false)인 건만
 * 스케줄러에서 조회 후 이메일 발송하도록 변경. (PRD §5.2 이메일 ①)
 */
@Component
public class NotificationChannelResolver {

    private static final Map<NotificationKind, Set<NotificationChannel>> CHANNEL_MAP;

    static {
        Map<NotificationKind, Set<NotificationChannel>> map = new EnumMap<>(NotificationKind.class);

        // 현재: 제안 수신 시 푸시 + 이메일 즉시 발송. 데모데이 이후 이메일은 1일 후 스케줄러로 전환 예정(TODO)
        map.put(NotificationKind.PROPOSAL_RECEIVED,
                EnumSet.of(NotificationChannel.PUSH, NotificationChannel.EMAIL));
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
