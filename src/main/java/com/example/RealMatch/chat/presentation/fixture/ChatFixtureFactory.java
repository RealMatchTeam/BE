package com.example.RealMatch.chat.presentation.fixture;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.RealMatch.chat.application.conversion.MessageCursor;
import com.example.RealMatch.chat.application.conversion.RoomCursor;
import com.example.RealMatch.chat.domain.enums.ChatAttachmentStatus;
import com.example.RealMatch.chat.domain.enums.ChatAttachmentType;
import com.example.RealMatch.chat.domain.enums.ChatMessageType;
import com.example.RealMatch.chat.domain.enums.ChatProposalDirection;
import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.enums.ChatProposalDecisionStatus;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomTab;
import com.example.RealMatch.chat.presentation.dto.enums.ChatSenderType;
import com.example.RealMatch.chat.presentation.dto.request.ChatAttachmentUploadRequest;
import com.example.RealMatch.chat.presentation.dto.response.ChatAttachmentInfoResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatAttachmentUploadResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatMatchedCampaignPayloadResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageListResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatProposalActionButtonResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatProposalActionButtonsResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatProposalCardPayloadResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatProposalStatusNoticePayloadResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCardResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCreateResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomDetailResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomListResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessageResponse;

public final class ChatFixtureFactory {

    private ChatFixtureFactory() {
    }

    public static ChatRoomCreateResponse sampleRoomCreateResponse() {
        return new ChatRoomCreateResponse(
                3001L,
                "direct:101:202",
                ChatProposalDirection.NONE,
                LocalDateTime.of(2025, 1, 1, 0, 0)
        );
    }

    public static ChatRoomListResponse sampleRoomListResponse() {
        ChatRoomCardResponse card = new ChatRoomCardResponse(
                3001L,
                202L,
                "라운드랩",
                "https://yt3.googleusercontent.com/ytc/AIdro_lLlKeDBBNPBO1FW7jkxvXpJyyM6CU2AR7NMx2GIjFFxQ=s900-c-k-c0x00ffffff-no-rj",
                ChatProposalStatus.REVIEWING,
                "안녕하세요!",
                ChatMessageType.TEXT,
                LocalDateTime.of(2025, 1, 1, 10, 0),
                3,
                ChatRoomTab.SENT
        );
        return new ChatRoomListResponse(
                5L,
                2L,
                7L,
                List.of(card),
                RoomCursor.of(LocalDateTime.of(2025, 1, 1, 9, 59, 59), 2999L),
                true
        );
    }

    public static ChatRoomDetailResponse sampleRoomDetailResponse(Long roomId) {
        return new ChatRoomDetailResponse(
                roomId,
                202L,
                "라운드랩",
                "https://yt3.googleusercontent.com/ytc/AIdro_lLlKeDBBNPBO1FW7jkxvXpJyyM6CU2AR7NMx2GIjFFxQ=s900-c-k-c0x00ffffff-no-rj",
                List.of("청정자극", "저자극", "심플한 감성"),
                ChatProposalStatus.REVIEWING,
                ChatProposalStatus.REVIEWING.labelOrNull()
        );
    }

    public static ChatMessageListResponse sampleMessageListResponse(Long roomId) {
        return new ChatMessageListResponse(
                List.of(
                        sampleMatchedCampaignMessage(roomId),
                        sampleProposalStatusNoticeMessage(roomId),
                        sampleSystemMessage(roomId),
                        sampleFileMessage(roomId),
                        sampleImageMessage(roomId),
                        sampleTextMessage(roomId)
                ),
                MessageCursor.of(6999L),
                true
        );
    }

    public static ChatMessageResponse sampleSystemMessageResponse(
            Long roomId,
            ChatSystemMessageKind kind,
            ChatSystemMessagePayload payload
    ) {
        if (kind == null) {
            throw new IllegalArgumentException("System message kind is required.");
        }
        ChatSystemMessagePayload resolvedPayload = payload != null ? payload : defaultSystemMessagePayload(kind);
        ChatSystemMessageResponse systemMessage = new ChatSystemMessageResponse(1, kind, resolvedPayload);
        return new ChatMessageResponse(
                systemMessageMessageId(kind),
                roomId,
                null,
                ChatSenderType.SYSTEM,
                ChatMessageType.SYSTEM,
                null,
                null,
                systemMessage,
                systemMessageCreatedAt(kind),
                null
        );
    }

    private static ChatMessageResponse sampleTextMessage(Long roomId) {
        return new ChatMessageResponse(
                7001L,
                roomId,
                202L,
                ChatSenderType.USER,
                ChatMessageType.TEXT,
                "안녕하세요!",
                null,
                null,
                LocalDateTime.of(2025, 1, 1, 10, 2),
                "11111111-1111-1111-1111-111111111111"
        );
    }

    private static ChatMessageResponse sampleImageMessage(Long roomId) {
        ChatAttachmentInfoResponse attachment = new ChatAttachmentInfoResponse(
                9001L,
                ChatAttachmentType.IMAGE,
                "image/png",
                "photo.png",
                204800L,
                "https://img.cjnews.cj.net/wp-content/uploads/2020/11/CJ%EC%98%AC%EB%A6%AC%EB%B8%8C%EC%98%81-%EC%98%AC%EB%A6%AC%EB%B8%8C%EC%98%81-%EC%83%88-BI-%EB%A1%9C%EA%B3%A0.jpg",
                ChatAttachmentStatus.READY
        );
        return new ChatMessageResponse(
                7002L,
                roomId,
                202L,
                ChatSenderType.USER,
                ChatMessageType.IMAGE,
                null,
                attachment,
                null,
                LocalDateTime.of(2025, 1, 1, 10, 3),
                "22222222-2222-2222-2222-222222222222"
        );
    }

    private static ChatMessageResponse sampleFileMessage(Long roomId) {
        ChatAttachmentInfoResponse attachment = new ChatAttachmentInfoResponse(
                9002L,
                ChatAttachmentType.FILE,
                "application/pdf",
                "proposal.pdf",
                102400L,
                "https://cdn.example.com/attachments/9002.pdf",
                ChatAttachmentStatus.READY
        );
        return new ChatMessageResponse(
                7003L,
                roomId,
                202L,
                ChatSenderType.USER,
                ChatMessageType.FILE,
                null,
                attachment,
                null,
                LocalDateTime.of(2025, 1, 1, 10, 4),
                "33333333-3333-3333-3333-333333333333"
        );
    }

    private static ChatMessageResponse sampleSystemMessage(Long roomId) {
        return sampleSystemMessageResponse(roomId, ChatSystemMessageKind.PROPOSAL_CARD, null);
    }

    private static ChatMessageResponse sampleProposalStatusNoticeMessage(Long roomId) {
        return sampleSystemMessageResponse(roomId, ChatSystemMessageKind.PROPOSAL_STATUS_NOTICE, null);
    }

    private static ChatMessageResponse sampleMatchedCampaignMessage(Long roomId) {
        return sampleSystemMessageResponse(roomId, ChatSystemMessageKind.MATCHED_CAMPAIGN_CARD, null);
    }

    private static ChatSystemMessagePayload defaultSystemMessagePayload(ChatSystemMessageKind kind) {
        return switch (kind) {
            case PROPOSAL_CARD -> new ChatProposalCardPayloadResponse(
                    5001L,
                    4001L,
                    "캠페인 A",
                    "캠페인 요약 문구",
                    ChatProposalDecisionStatus.PENDING,
                    ChatProposalDirection.BRAND_TO_CREATOR,
                    new ChatProposalActionButtonsResponse(
                            new ChatProposalActionButtonResponse(
                                    "제안 수락하기",
                                    true
                            ),
                            new ChatProposalActionButtonResponse(
                                    "거절하기",
                                    true
                            )
                    ),
                    null
            );
            case PROPOSAL_STATUS_NOTICE -> new ChatProposalStatusNoticePayloadResponse(
                    5001L,
                    202L,
                    LocalDateTime.of(2025, 1, 1, 10, 6)
            );
            case MATCHED_CAMPAIGN_CARD -> new ChatMatchedCampaignPayloadResponse(
                    4001L,
                    "캠페인 A",
                    150000L,
                    "KRW",
                    "ORDER-20250101-0001",
                    "캠페인이 매칭되었습니다. 협업을 시작해 주세요."
            );
        };
    }

    private static long systemMessageMessageId(ChatSystemMessageKind kind) {
        return switch (kind) {
            case PROPOSAL_CARD -> 7004L;
            case PROPOSAL_STATUS_NOTICE -> 7005L;
            case MATCHED_CAMPAIGN_CARD -> 7006L;
        };
    }

    private static LocalDateTime systemMessageCreatedAt(ChatSystemMessageKind kind) {
        return switch (kind) {
            case PROPOSAL_CARD -> LocalDateTime.of(2025, 1, 1, 10, 5);
            case PROPOSAL_STATUS_NOTICE -> LocalDateTime.of(2025, 1, 1, 10, 6);
            case MATCHED_CAMPAIGN_CARD -> LocalDateTime.of(2025, 1, 1, 10, 7);
        };
    }

    public static ChatAttachmentUploadResponse sampleAttachmentUploadResponse(
            ChatAttachmentUploadRequest request,
            MultipartFile file
    ) {
        return new ChatAttachmentUploadResponse(
                9001L,
                request.attachmentType(),
                file.getContentType(),
                file.getOriginalFilename(),
                file.getSize(),
                "https://cdn.example.com/attachments/9001",
                ChatAttachmentStatus.UPLOADED,
                LocalDateTime.of(2025, 1, 1, 10, 10)
        );
    }
}
