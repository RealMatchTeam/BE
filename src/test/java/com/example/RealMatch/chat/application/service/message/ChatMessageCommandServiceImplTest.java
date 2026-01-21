package com.example.RealMatch.chat.application.service.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.RealMatch.chat.application.service.room.ChatRoomUpdateService;
import com.example.RealMatch.chat.domain.entity.ChatAttachment;
import com.example.RealMatch.chat.domain.entity.ChatMessage;
import com.example.RealMatch.chat.domain.repository.ChatAttachmentRepository;
import com.example.RealMatch.chat.domain.repository.ChatMessageRepository;
import com.example.RealMatch.chat.presentation.dto.enums.ChatAttachmentStatus;
import com.example.RealMatch.chat.presentation.dto.enums.ChatAttachmentType;
import com.example.RealMatch.chat.presentation.dto.enums.ChatMessageType;
import com.example.RealMatch.chat.presentation.dto.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatMatchedCampaignPayloadResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageCommand;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("null")
class ChatMessageCommandServiceImplTest {

    private final ChatMessageRepository chatMessageRepository = Mockito.mock(ChatMessageRepository.class);
    private final ChatAttachmentRepository chatAttachmentRepository = Mockito.mock(ChatAttachmentRepository.class);
    private final ChatRoomUpdateService chatRoomUpdateService = Mockito.mock(ChatRoomUpdateService.class);
    private final MessagePreviewGenerator messagePreviewGenerator = Mockito.mock(MessagePreviewGenerator.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final SystemMessagePayloadSerializer payloadSerializer =
            new JacksonSystemMessagePayloadSerializer(objectMapper);
    private final ChatMessageCommandServiceImpl service =
            new ChatMessageCommandServiceImpl(
                    chatMessageRepository,
                    chatAttachmentRepository,
                    chatRoomUpdateService,
                    messagePreviewGenerator,
                    payloadSerializer
            );

    @Test
    @DisplayName("텍스트 메시지는 저장되고 응답을 반환한다")
    void saveTextMessage_returnsResponse() {
        ChatSendMessageCommand command = new ChatSendMessageCommand(3001L, ChatMessageType.TEXT, "안녕하세요", null,
                "11111111-1111-1111-1111-111111111111");
        ChatMessage saved = ChatMessage.createUserMessage(3001L, 202L, ChatMessageType.TEXT, "안녕하세요", null,
                command.clientMessageId());
        ReflectionTestUtils.setField(saved, "id", 7001L);

        given(chatMessageRepository.findByClientMessageIdAndSenderId(anyString(), anyLong()))
                .willReturn(Optional.empty());
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(saved);

        ChatMessageResponse response = service.saveMessage(command, 202L);

        assertThat(response.messageId()).isEqualTo(7001L);
        assertThat(response.messageType()).isEqualTo(ChatMessageType.TEXT);
        assertThat(response.attachment()).isNull();
    }

    @Test
    @DisplayName("이미지 메시지는 첨부 정보를 포함한다")
    void saveImageMessage_includesAttachment() {
        ChatSendMessageCommand command = new ChatSendMessageCommand(3001L, ChatMessageType.IMAGE, null, 9001L,
                "22222222-2222-2222-2222-222222222222");
        ChatMessage saved = ChatMessage.createUserMessage(3001L, 202L, ChatMessageType.IMAGE, null, 9001L,
                command.clientMessageId());
        ReflectionTestUtils.setField(saved, "id", 7002L);

        ChatAttachment attachment = Mockito.mock(ChatAttachment.class);
        given(attachment.getId()).willReturn(9001L);
        given(attachment.getAttachmentType()).willReturn(ChatAttachmentType.IMAGE);
        given(attachment.getContentType()).willReturn("image/png");
        given(attachment.getOriginalName()).willReturn("photo.png");
        given(attachment.getFileSize()).willReturn(204800L);
        given(attachment.getAccessUrl()).willReturn("https://example.com/9001");
        given(attachment.getStatus()).willReturn(ChatAttachmentStatus.UPLOADED);

        given(chatMessageRepository.findByClientMessageIdAndSenderId(anyString(), anyLong()))
                .willReturn(Optional.empty());
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(saved);
        given(chatAttachmentRepository.findById(9001L)).willReturn(Optional.of(attachment));

        ChatMessageResponse response = service.saveMessage(command, 202L);

        assertThat(response.attachment()).isNotNull();
        assertThat(response.attachment().attachmentType()).isEqualTo(ChatAttachmentType.IMAGE);
    }

    @Test
    @DisplayName("중복 클라이언트 메시지는 재저장하지 않는다")
    void saveMessage_whenDuplicate_returnsExisting() {
        ChatSendMessageCommand command = new ChatSendMessageCommand(3001L, ChatMessageType.TEXT, "안녕하세요", null,
                "11111111-1111-1111-1111-111111111111");
        ChatMessage existing = ChatMessage.createUserMessage(3001L, 202L, ChatMessageType.TEXT, "안녕하세요", null,
                command.clientMessageId());
        ReflectionTestUtils.setField(existing, "id", 7001L);

        given(chatMessageRepository.findByClientMessageIdAndSenderId(anyString(), anyLong()))
                .willReturn(Optional.of(existing));

        ChatMessageResponse response = service.saveMessage(command, 202L);

        assertThat(response.messageId()).isEqualTo(7001L);
        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("첨부 메시지는 attachmentId가 없으면 실패한다")
    void saveMessage_requiresAttachmentId() {
        ChatSendMessageCommand command = new ChatSendMessageCommand(3001L, ChatMessageType.IMAGE, null, null,
                "22222222-2222-2222-2222-222222222222");

        assertThatThrownBy(() -> service.saveMessage(command, 202L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("시스템 메시지는 저장되고 payload가 매핑된다")
    void saveSystemMessage_returnsResponse() {
        ChatMatchedCampaignPayloadResponse payload = new ChatMatchedCampaignPayloadResponse(
                4001L,
                "캠페인 A",
                150000L,
                "KRW",
                "ORDER-20250101-0001",
                "캠페인이 매칭되었습니다. 협업을 시작해 주세요."
        );

        given(chatMessageRepository.save(any(ChatMessage.class))).willAnswer(invocation -> {
            ChatMessage message = invocation.getArgument(0);
            ReflectionTestUtils.setField(message, "id", 8001L);
            return message;
        });

        ChatMessageResponse response = service.saveSystemMessage(
                3001L,
                ChatSystemMessageKind.MATCHED_CAMPAIGN_CARD,
                payload
        );

        assertThat(response.messageId()).isEqualTo(8001L);
        assertThat(response.messageType()).isEqualTo(ChatMessageType.SYSTEM);
        assertThat(response.systemMessage()).isNotNull();
        assertThat(response.systemMessage().kind()).isEqualTo(ChatSystemMessageKind.MATCHED_CAMPAIGN_CARD);
        assertThat(response.systemMessage().payload()).isInstanceOf(ChatMatchedCampaignPayloadResponse.class);
    }
}
