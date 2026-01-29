package com.example.RealMatch.tag.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.tag.domain.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findAllByTagType(String tagType);

    List<Tag> findAllByTagCategory(String tagCategory);

    List<Tag> findAllByTagTypeAndTagCategory(String tagType, String tagCategory);

    Optional<Tag> findByTagTypeAndTagNameAndTagCategory(String tagType, String tagName, String tagCategory);
}
