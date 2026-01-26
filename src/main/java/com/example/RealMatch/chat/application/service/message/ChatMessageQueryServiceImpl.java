package com.example.RealMatch.chat.application.service.message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.chat.application.conversion.MessageCursor;
import com.example.RealMatch.chat.application.mapper.ChatMessageResponseMapper;
import com.example.RealMatch.chat.domain.entity.ChatAttachment;
import com.example.RealMatch.chat.domain.entity.ChatMessage;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.exception.ChatException;
import com.example.RealMatch.chat.domain.repository.ChatAttachmentRepository;
import com.example.RealMatch.chat.domain.repository.ChatMessageRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomMemberRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomRepository;
import com.example.RealMatch.chat.presentation.code.ChatErrorCode;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageListResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageQueryServiceImpl implements ChatMessageQueryService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatAttachmentRepository chatAttachmentRepository;
    private final ChatMessageResponseMapper responseMapper;

    @Override
    @Transactional
    public ChatMessageListResponse getMessages(
            Long userId,
            Long roomId,
            MessageCursor messageCursor,
            int size
    ) {
        ChatRoomMember member = validateRoomAndMembership(roomId, userId);

        Long cursorMessageId = messageCursor != null ? messageCursor.messageId() : null;
        List<ChatMessage> messages = chatMessageRepository.findMessagesByRoomId(roomId, cursorMessageId, size);

        boolean hasNext = messages.size() > size;
        if (hasNext) {
            messages = messages.subList(0, size);
        }

        if (messages.isEmpty()) {
            return new ChatMessageListResponse(List.of(), null, false);
        }

        if (cursorMessageId == null) {
            ChatMessage latestMessage = messages.get(0);
            updateLastReadMessage(member, latestMessage.getId());
        }

        List<Long> attachmentIds = messages.stream()
                .map(ChatMessage::getAttachmentId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        Map<Long, ChatAttachment> attachmentMap = attachmentIds.isEmpty()
                ? Map.of()
                : chatAttachmentRepository.findAllById(attachmentIds).stream()
                        .collect(Collectors.toMap(ChatAttachment::getId, attachment -> attachment));

        List<ChatMessageResponse> messageResponses = messages.stream()
                .map(message -> {
                    ChatAttachment attachment = message.getAttachmentId() != null
                            ? attachmentMap.get(message.getAttachmentId())
                            : null;
                    return responseMapper.toResponse(message, attachment);
                })
                .toList();

        MessageCursor nextCursor = null;
        if (hasNext) {
            ChatMessage lastMessage = messages.getLast();
            nextCursor = MessageCursor.of(lastMessage.getId());
        }

        return new ChatMessageListResponse(messageResponses, nextCursor, hasNext);
    }

    private void updateLastReadMessage(ChatRoomMember member, Long messageId) {
        if (member.getLastReadMessageId() == null || member.getLastReadMessageId() < messageId) {
            member.updateLastReadMessage(messageId, LocalDateTime.now());
        }
    }

    private ChatRoomMember validateRoomAndMembership(Long roomId, Long userId) {
        if (roomId == null) {
            throw new ChatException(ChatErrorCode.ROOM_NOT_FOUND);
        }
        if (!chatRoomRepository.existsById(roomId)) {
            throw new ChatException(ChatErrorCode.ROOM_NOT_FOUND);
        }

        ChatRoomMember member = chatRoomMemberRepository
                .findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_ROOM_MEMBER));
        if (member.getLeftAt() != null) {
            throw new ChatException(ChatErrorCode.USER_LEFT_ROOM);
        }
        return member;
    }
}
