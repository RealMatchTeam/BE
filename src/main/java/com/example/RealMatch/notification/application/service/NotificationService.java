package com.example.RealMatch.notification.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.notification.application.dto.CreateNotificationCommand;
import com.example.RealMatch.notification.domain.entity.Notification;
import com.example.RealMatch.notification.domain.repository.NotificationRepository;
import com.example.RealMatch.notification.exception.NotificationErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification create(CreateNotificationCommand command) {
        Notification notification = Notification.builder()
                .userId(command.getUserId())
                .kind(command.getKind())
                .title(command.getTitle())
                .body(command.getBody())
                .referenceType(command.getReferenceType())
                .referenceId(command.getReferenceId())
                .campaignId(command.getCampaignId())
                .proposalId(command.getProposalId())
                .build();

        return notificationRepository.save(notification);
    }

    public void markAsRead(Long userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUserId().equals(userId)) {
            throw new CustomException(NotificationErrorCode.NOTIFICATION_FORBIDDEN);
        }

        notification.markAsRead();
    }

    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsRead(userId);
    }
}
