package com.example.RealMatch.notification.application.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationMessageTemplateService {

    /**
     * PROPOSAL_RECEIVED 알림 메시지 생성
     */
    public MessageTemplate createProposalReceivedMessage(String brandName) {
        String title = "새 캠페인 제안";
        String body = String.format("[%s]에서 새로운 캠페인 제안을 보냈어요. 지금 확인해보세요!", brandName);
        return new MessageTemplate(title, body);
    }

    /**
     * PROPOSAL_SENT 알림 메시지 생성 (수락/거절)
     */
    public MessageTemplate createProposalSentMessage(String brandName, boolean isAccepted) {
        String title = "제안 결과";
        String body = isAccepted
                ? String.format("[%s] 제안이 수락되었어요.", brandName)
                : String.format("[%s] 제안이 거절되었어요.", brandName);
        return new MessageTemplate(title, body);
    }

    /**
     * CAMPAIGN_APPLIED 알림 메시지 생성
     */
    public MessageTemplate createCampaignAppliedMessage(String creatorName) {
        String title = "캠페인 지원";
        String body = String.format("[%s]님이 캠페인에 지원했어요.", creatorName);
        return new MessageTemplate(title, body);
    }

    /**
     * CAMPAIGN_MATCHED 알림 메시지 생성
     */
    public MessageTemplate createCampaignMatchedMessage(String brandName) {
        String title = "캠페인 매칭";
        String body = String.format("[%s]과의 캠페인이 매칭되었어요! 캠페인 상세 내용을 확인해 주세요.", brandName);
        return new MessageTemplate(title, body);
    }

    /**
     * CAMPAIGN_COMPLETED 알림 메시지 생성
     */
    public MessageTemplate createCampaignCompletedMessage(String brandName) {
        String title = "캠페인 완료";
        String body = String.format("[%s] 캠페인이 완료되었어요.", brandName);
        return new MessageTemplate(title, body);
    }

    /**
     * SETTLEMENT_READY 알림 메시지 생성
     */
    public MessageTemplate createSettlementReadyMessage(String brandName) {
        String title = "정산 출금 가능";
        String body = String.format("[%s] 캠페인이 완료되었어요. 정산 금액을 출금 신청할 수 있어요!", brandName);
        return new MessageTemplate(title, body);
    }

    /**
     * AUTO_CONFIRMED 알림 메시지 생성
     */
    public MessageTemplate createAutoConfirmedMessage(String brandName) {
        String title = "자동 확정";
        String body = String.format("[%s] 캠페인이 자동으로 확정되었어요.", brandName);
        return new MessageTemplate(title, body);
    }

    /**
     * CHAT_MESSAGE 알림 메시지 생성
     */
    public MessageTemplate createChatMessage(String senderName, String messagePreview) {
        String title = "새 메시지";
        String body = messagePreview != null && !messagePreview.isEmpty()
                ? String.format("[%s]: %s", senderName, messagePreview)
                : String.format("[%s]님으로부터 채팅 메시지가 도착했어요.", senderName);
        return new MessageTemplate(title, body);
    }

    /**
     * 메시지 템플릿 결과 DTO
     */
    public record MessageTemplate(String title, String body) {
    }
}
