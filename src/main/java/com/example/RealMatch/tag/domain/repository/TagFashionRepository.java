package com.example.RealMatch.tag.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.tag.domain.entity.TagFashion;
import com.example.RealMatch.tag.domain.enums.ContentTagType;

public interface TagFashionRepository extends JpaRepository<TagFashion, UUID> {
    List<TagFashion> findByTagType(
            ContentTagType tagType
    );
}
