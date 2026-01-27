package com.example.RealMatch.chat.application.service.room;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.repository.ChatRoomMemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberQueryServiceImpl implements ChatRoomMemberQueryService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Override
    public List<ChatRoomMember> findActiveMembers(Long roomId) {
        return chatRoomMemberRepository.findByRoomId(roomId).stream()
                .filter(member -> !member.isDeleted() && member.getLeftAt() == null)
                .toList();
    }
}
