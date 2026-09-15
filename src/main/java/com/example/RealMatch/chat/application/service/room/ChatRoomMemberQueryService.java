package com.example.RealMatch.chat.application.service.room;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.RealMatch.chat.application.repository.ChatRoomMemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberQueryService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;

    public List<Long> findActiveMemberUserIds(Long roomId) {
        return chatRoomMemberRepository.findActiveMembersByRoomId(roomId).stream()
                .map(member -> member.getUserId())
                .toList();
    }
}
