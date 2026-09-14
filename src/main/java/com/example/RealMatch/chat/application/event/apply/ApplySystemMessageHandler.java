package com.example.RealMatch.chat.application.event.apply;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.chat.application.service.message.ChatMessageCommandService;
import com.example.RealMatch.chat.application.service.room.ChatRoomCommandService;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatApplyStatusNoticePayloadResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class ApplySystemMessageHandler {

    private final ChatMessageCommandService messageService;
    private final ChatRoomCommandService roomService;

    public void handleApplySent(ApplySentEvent event) {
        messageService.saveSystemMessage(event.roomId(), event.eventId(),
                ChatSystemMessageKind.APPLY_CARD, event.payload());
    }

    public void handleApplyStatusChanged(ApplyStatusChangedEvent event) {
        Long roomId = roomService.createOrGetRoomSystem(event.brandUserId(), event.creatorUserId()).roomId();
        messageService.saveSystemMessage(roomId, event.eventId() + ":NOTICE",
                ChatSystemMessageKind.APPLY_STATUS_NOTICE,
                new ChatApplyStatusNoticePayloadResponse(event.applyId(), event.actorUserId(),
                        LocalDateTime.now(), event.newStatus()));
    }
}
