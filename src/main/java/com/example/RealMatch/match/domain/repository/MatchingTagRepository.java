package com.example.RealMatch.match.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.match.domain.entity.MatchingTag;
import com.example.RealMatch.match.domain.entity.enums.TagType;

public interface MatchingTagRepository extends JpaRepository<MatchingTag, Long> {

    List<MatchingTag> findByTestId(Long testId);

    List<MatchingTag> findByTestIdAndTagType(Long testId, TagType tagType);

    void deleteByTestId(Long testId);
}
