package com.example.RealMatch.chat.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.chat.domain.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomRepositoryCustom {
    Optional<ChatRoom> findByRoomKey(String roomKey);
}
