package com.example.RealMatch.match.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.match.domain.entity.MatchingTagEntity;
import com.example.RealMatch.match.domain.entity.enums.TagType;

public interface MatchingTagRepository extends JpaRepository<MatchingTagEntity, Long> {

    List<MatchingTagEntity> findByTestId(Long testId);

    List<MatchingTagEntity> findByTestIdAndTagType(Long testId, TagType tagType);

    void deleteByTestId(Long testId);
}
