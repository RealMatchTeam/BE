package com.example.RealMatch.attachment.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.attachment.domain.entity.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}
