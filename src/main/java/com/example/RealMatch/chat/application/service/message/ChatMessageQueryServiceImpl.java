package com.example.RealMatch.chat.application.service.message;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.attachment.application.dto.AttachmentDto;
import com.example.RealMatch.attachment.application.service.AttachmentQueryService;
import com.example.RealMatch.chat.application.conversion.MessageCursor;
import com.example.RealMatch.chat.application.mapper.ChatMessageResponseMapper;
import com.example.RealMatch.chat.application.service.room.ChatRoomMemberCommandService;
import com.example.RealMatch.chat.application.service.room.ChatRoomMemberService;
import com.example.RealMatch.chat.domain.entity.ChatMessage;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.exception.ChatException;
import com.example.RealMatch.chat.domain.repository.ChatMessageRepository;
import com.example.RealMatch.chat.presentation.code.ChatErrorCode;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageListResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageQueryServiceImpl implements ChatMessageQueryService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberService chatRoomMemberService;
    private final AttachmentQueryService attachmentQueryService;
    private final ChatMessageResponseMapper responseMapper;
    private final ChatRoomMemberCommandService chatRoomMemberCommandService;

    @Override
    @Transactional(readOnly = true)
    public ChatMessageListResponse getMessages(
            Long userId,
            Long roomId,
            MessageCursor messageCursor,
            int size
    ) {
        if (roomId == null) {
            throw new ChatException(ChatErrorCode.ROOM_NOT_FOUND);
        }
        
        // 멤버 검증 및 조회
        ChatRoomMember member = chatRoomMemberService.getActiveMemberOrThrow(roomId, userId);

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
            chatRoomMemberCommandService.updateLastReadMessage(member.getId(), latestMessage.getId());
        }

        List<Long> attachmentIds = messages.stream()
                .map(ChatMessage::getAttachmentId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        Map<Long, AttachmentDto> attachmentMap = attachmentQueryService.findAllById(attachmentIds);

        List<ChatMessageResponse> messageResponses = messages.stream()
                .map(message -> {
                    AttachmentDto attachment = message.getAttachmentId() != null
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

}
