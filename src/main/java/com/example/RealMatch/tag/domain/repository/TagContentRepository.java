package com.example.RealMatch.tag.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.tag.domain.entity.TagContent;
import com.example.RealMatch.tag.domain.enums.ContentTagType;

public interface TagContentRepository extends JpaRepository<TagContent, UUID> {
    List<TagContent> findByTagType(
            ContentTagType tagType
    );
}
