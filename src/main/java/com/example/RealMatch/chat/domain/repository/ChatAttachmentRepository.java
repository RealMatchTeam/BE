package com.example.RealMatch.chat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.chat.domain.entity.ChatAttachment;

public interface ChatAttachmentRepository extends JpaRepository<ChatAttachment, Long> {
}
