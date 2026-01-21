package com.example.RealMatch.chat.domain.entity;

import com.example.RealMatch.chat.domain.enums.ChatAttachmentStatus;
import com.example.RealMatch.chat.domain.enums.ChatAttachmentType;
import com.example.RealMatch.global.common.DeleteBaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "chat_attachment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatAttachment extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uploader_id", nullable = false)
    private Long uploaderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_type", nullable = false, length = 20)
    private ChatAttachmentType attachmentType;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "access_url", length = 1024)
    private String accessUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ChatAttachmentStatus status;
}
