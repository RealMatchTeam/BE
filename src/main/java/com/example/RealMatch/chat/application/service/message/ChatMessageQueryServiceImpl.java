package com.example.RealMatch.chat.application.service.message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.attachment.application.service.AttachmentQueryService;
import com.example.RealMatch.attachment.presentation.dto.response.AttachmentInfoResponse;
import com.example.RealMatch.chat.application.conversion.MessageCursor;
import com.example.RealMatch.chat.application.mapper.ChatMessageResponseMapper;
import com.example.RealMatch.chat.application.util.ChatRoomMemberValidator;
import com.example.RealMatch.chat.domain.entity.ChatMessage;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.exception.ChatException;
import com.example.RealMatch.chat.domain.repository.ChatMessageRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomMemberRepository;
import com.example.RealMatch.chat.presentation.code.ChatErrorCode;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageListResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageQueryServiceImpl implements ChatMessageQueryService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final AttachmentQueryService attachmentQueryService;
    private final ChatMessageResponseMapper responseMapper;

    @Override
    @Transactional
    public ChatMessageListResponse getMessages(
            Long userId,
            Long roomId,
            MessageCursor messageCursor,
            int size
    ) {
        if (roomId == null) {
            throw new ChatException(ChatErrorCode.ROOM_NOT_FOUND);
        }
        ChatRoomMember member = chatRoomMemberRepository
                .findMemberByRoomIdAndUserIdWithRoomCheck(roomId, userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_ROOM_MEMBER));
        
        // 활성 상태 검증
        ChatRoomMemberValidator.validateActiveMember(member);

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

        Map<Long, AttachmentInfoResponse> attachmentMap = attachmentQueryService.findAllById(attachmentIds);

        List<ChatMessageResponse> messageResponses = messages.stream()
                .map(message -> {
                    AttachmentInfoResponse attachment = message.getAttachmentId() != null
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

}
