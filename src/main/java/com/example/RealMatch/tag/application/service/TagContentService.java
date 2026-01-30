package com.example.RealMatch.tag.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.tag.domain.enums.ContentTagType;
import com.example.RealMatch.tag.domain.repository.TagContentRepository;
import com.example.RealMatch.tag.presentation.dto.response.ContentTagResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagContentService {
    private final TagContentRepository tagContentRepository;

    public ContentTagResponse getContentTags() {

        return new ContentTagResponse(
                findBy(ContentTagType.FORMAT),
                findBy(ContentTagType.CATEGORY),
                findBy(ContentTagType.TONE),
                findBy(ContentTagType.INVOLVEMENT),
                findBy(ContentTagType.USAGE_RANGE)
        );
    }

    private List<ContentTagResponse.TagItemResponse> findBy(
            ContentTagType tagType
    ) {
        return tagContentRepository
                .findByTagType(tagType)
                .stream()
                .map(tag -> new ContentTagResponse.TagItemResponse(
                        tag.getId(),
                        tag.getKorName()
                ))
                .toList();
    }
}
