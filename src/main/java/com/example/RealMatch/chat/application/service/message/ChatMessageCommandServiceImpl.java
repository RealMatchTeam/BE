package com.example.RealMatch.chat.application.service.message;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.chat.application.mapper.ChatMessageResponseMapper;
import com.example.RealMatch.chat.application.service.room.ChatRoomUpdateService;
import com.example.RealMatch.chat.application.util.MessagePreviewGenerator;
import com.example.RealMatch.chat.application.util.SystemMessagePayloadSerializer;
import com.example.RealMatch.chat.domain.entity.ChatAttachment;
import com.example.RealMatch.chat.domain.entity.ChatMessage;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.domain.exception.ChatException;
import com.example.RealMatch.chat.domain.repository.ChatAttachmentRepository;
import com.example.RealMatch.chat.domain.repository.ChatMessageRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomMemberRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomRepository;
import com.example.RealMatch.chat.presentation.code.ChatErrorCode;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageCommand;

@Service
public class ChatMessageCommandServiceImpl implements ChatMessageCommandService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatAttachmentRepository chatAttachmentRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUpdateService chatRoomUpdateService;
    private final MessagePreviewGenerator messagePreviewGenerator;
    private final ChatMessageResponseMapper responseMapper;
    private final SystemMessagePayloadSerializer payloadSerializer;

    public ChatMessageCommandServiceImpl(
            ChatMessageRepository chatMessageRepository,
            ChatAttachmentRepository chatAttachmentRepository,
            ChatRoomMemberRepository chatRoomMemberRepository,
            ChatRoomRepository chatRoomRepository,
            ChatRoomUpdateService chatRoomUpdateService,
            MessagePreviewGenerator messagePreviewGenerator,
            ChatMessageResponseMapper responseMapper,
            SystemMessagePayloadSerializer payloadSerializer
    ) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatAttachmentRepository = chatAttachmentRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomUpdateService = chatRoomUpdateService;
        this.messagePreviewGenerator = messagePreviewGenerator;
        this.responseMapper = responseMapper;
        this.payloadSerializer = payloadSerializer;
    }

    @Override
    @Transactional
    @NonNull
    public ChatMessageResponse saveMessage(ChatSendMessageCommand command, Long senderId) {
        // Room 멤버 권한 검증
        validateRoomMembership(command.roomId(), senderId);

        // 첨부 파일 존재 및 소유권 검증
        Long attachmentId = command.attachmentId();
        if (attachmentId != null) {
            ChatAttachment attachment = chatAttachmentRepository.findById(attachmentId)
                    .orElseThrow(() -> new ChatException(ChatErrorCode.ATTACHMENT_NOT_FOUND));
            if (!attachment.getUploaderId().equals(senderId)) {
                throw new ChatException(ChatErrorCode.ATTACHMENT_OWNERSHIP_MISMATCH);
            }
        }

        ChatMessage existing = chatMessageRepository
                .findByClientMessageIdAndSenderId(command.clientMessageId(), senderId)
                .orElse(null);
        if (existing != null) {
            // roomId 일치 검증
            if (!existing.getRoomId().equals(command.roomId())) {
                throw new ChatException(ChatErrorCode.INVALID_ROOM_FOR_MESSAGE);
            }
            ChatAttachment attachment = loadAttachmentIfNeeded(existing);
            return responseMapper.toResponse(existing, attachment);
        }

        ChatMessage message;
        try {
            message = ChatMessage.createUserMessage(
                    command.roomId(),
                    senderId,
                    command.messageType(),
                    command.content(),
                    command.attachmentId(),
                    command.clientMessageId()
            );
        } catch (IllegalArgumentException ex) {
            throw new ChatException(ChatErrorCode.INVALID_MESSAGE_FORMAT, ex.getMessage());
        }

        ChatMessage saved = chatMessageRepository.save(message);
        updateChatRoomLastMessage(saved);
        return responseMapper.toResponse(saved, loadAttachmentIfNeeded(saved));
    }

    @Override
    @Transactional
    @NonNull
    public ChatMessageResponse saveSystemMessage(
            Long roomId,
            ChatSystemMessageKind kind,
            ChatSystemMessagePayload payload
    ) {
        // 방 존재 여부 검증
        if (roomId == null) {
            throw new ChatException(ChatErrorCode.ROOM_NOT_FOUND);
        }
        chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.ROOM_NOT_FOUND));
        
        ChatMessage message;
        try {
            message = ChatMessage.createSystemMessage(
                    roomId,
                    kind,
                    payloadSerializer.serialize(payload)
            );
        } catch (IllegalArgumentException ex) {
            throw new ChatException(ChatErrorCode.INVALID_MESSAGE_FORMAT, ex.getMessage());
        }
        
        ChatMessage saved = chatMessageRepository.save(message);
        updateChatRoomLastMessage(saved);
        return responseMapper.toResponse(saved, null);
    }

    private ChatAttachment loadAttachmentIfNeeded(ChatMessage message) {
        Long attachmentId = message.getAttachmentId();
        if (attachmentId == null) {
            return null;
        }
        return chatAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.ATTACHMENT_NOT_FOUND));
    }

    private void validateRoomMembership(Long roomId, Long senderId) {
        ChatRoomMember member = chatRoomMemberRepository
                .findByRoomIdAndUserId(roomId, senderId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_ROOM_MEMBER));
        if (member.getLeftAt() != null) {
            throw new ChatException(ChatErrorCode.USER_LEFT_ROOM);
        }
    }

    private void updateChatRoomLastMessage(ChatMessage message) {
        String preview = messagePreviewGenerator.generate(message.getMessageType(), message.getContent());
        chatRoomUpdateService.updateLastMessage(
                message.getRoomId(),
                message.getId(),
                message.getCreatedAt(),
                message.getMessageType(),
                preview
        );
    }
}
