package com.example.RealMatch.tag.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.tag.domain.enums.ContentTagType;
import com.example.RealMatch.tag.domain.enums.TagType;
import com.example.RealMatch.tag.domain.repository.TagRepository;
import com.example.RealMatch.tag.presentation.dto.response.ContentTagResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagContentService {
    private final TagRepository tagRepository;

    public ContentTagResponse getContentTags() {
        return new ContentTagResponse(
                findBy(ContentTagType.VIEWER_GENDER),
                findBy(ContentTagType.VIEWER_AGE),
                findBy(ContentTagType.AVG_VIDEO_LENGTH),
                findBy(ContentTagType.AVG_VIDEO_VIEWS),
                findBy(ContentTagType.FORMAT),
                findBy(ContentTagType.CATEGORY),
                findBy(ContentTagType.TONE),
                findBy(ContentTagType.INVOLVEMENT),
                findBy(ContentTagType.USAGE_RANGE)
        );
    }

    private List<ContentTagResponse.TagItemResponse> findBy(
            ContentTagType contentTagType
    ) {
        return tagRepository
                .findAllByTagTypeAndTagCategory(
                        TagType.CONTENT.getDescription(),
                        contentTagType.getKorName()
                )
                .stream()
                .map(tag -> new ContentTagResponse.TagItemResponse(
                        tag.getId(),
                        tag.getTagName()
                ))
                .toList();
    }
}
