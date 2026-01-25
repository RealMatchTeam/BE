package com.example.RealMatch.tag.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.tag.domain.entity.TagBeauty;
import com.example.RealMatch.tag.domain.enums.ContentTagType;

public interface TagBeautyRepository extends JpaRepository<TagBeauty, Long> {
    List<TagBeauty> findByTagTypeOrderByDisplayOrderAsc(
            ContentTagType tagType
    );
}
