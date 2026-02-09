package com.example.RealMatch.notification.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.RealMatch.notification.domain.entity.enums.NotificationKind;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;

/**
 * NotificationChannelResolver 단위 테스트
 * Spring 없이 순수 로직만 테스트
 */
@DisplayName("NotificationChannelResolver 테스트")
class NotificationChannelResolverTest {

    private final NotificationChannelResolver resolver = new NotificationChannelResolver();

    @Test
    @DisplayName("PROPOSAL_RECEIVED는 PUSH와 EMAIL을 반환")
    void resolveChannels_shouldReturnPushAndEmail_whenProposalReceived() {
        // when
        Set<NotificationChannel> channels = resolver.resolveChannels(NotificationKind.PROPOSAL_RECEIVED);

        // then
        assertThat(channels).containsExactlyInAnyOrder(NotificationChannel.PUSH, NotificationChannel.EMAIL);
    }

    @Test
    @DisplayName("CAMPAIGN_MATCHED는 PUSH와 EMAIL 반환")
    void resolveChannels_shouldReturnPushAndEmail_whenCampaignMatched() {
        // when
        Set<NotificationChannel> channels = resolver.resolveChannels(NotificationKind.CAMPAIGN_MATCHED);

        // then
        assertThat(channels).containsExactlyInAnyOrder(NotificationChannel.PUSH, NotificationChannel.EMAIL);
    }

    @Test
    @DisplayName("AUTO_CONFIRMED는 EMAIL만 반환")
    void resolveChannels_shouldReturnEmailOnly_whenAutoConfirmed() {
        // when
        Set<NotificationChannel> channels = resolver.resolveChannels(NotificationKind.AUTO_CONFIRMED);

        // then
        assertThat(channels).containsExactly(NotificationChannel.EMAIL);
    }

    @Test
    @DisplayName("모든 NotificationKind에 대해 채널이 정의되어 있음")
    void resolveChannels_shouldReturnChannels_whenAnyKind() {
        // when & then
        for (NotificationKind kind : NotificationKind.values()) {
            Set<NotificationChannel> channels = resolver.resolveChannels(kind);
            assertThat(channels).isNotEmpty();
        }
    }
}
