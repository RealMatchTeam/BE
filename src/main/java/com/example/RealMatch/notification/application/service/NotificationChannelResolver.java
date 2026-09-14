package com.example.RealMatch.notification.application.service;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.notification.domain.entity.enums.NotificationKind;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;
import com.example.RealMatch.user.domain.repository.NotificationSettingRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationChannelResolver {

    private final NotificationSettingRepository notificationSettingRepository;

    private static final Map<NotificationKind, Set<NotificationChannel>> CHANNEL_MAP;

    static {
        Map<NotificationKind, Set<NotificationChannel>> map = new EnumMap<>(NotificationKind.class);

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

    public Set<NotificationChannel> resolveChannels(NotificationKind kind, Long userId) {
        Set<NotificationChannel> channels = EnumSet.noneOf(NotificationChannel.class);
        notificationSettingRepository.findByUserId(userId).ifPresent(setting -> {
            for (NotificationChannel channel : CHANNEL_MAP.getOrDefault(kind, Collections.emptySet())) {
                if (setting.allows(channel)) {
                    channels.add(channel);
                }
            }
        });
        return channels;
    }

    public boolean isEnabled(Long userId, NotificationChannel channel) {
        return notificationSettingRepository.findByUserId(userId)
                .map(setting -> setting.allows(channel))
                .orElse(false);
    }
}
