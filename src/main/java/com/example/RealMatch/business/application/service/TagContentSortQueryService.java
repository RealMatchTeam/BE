package com.example.RealMatch.business.application.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.business.domain.enums.ContentTagSort;
import com.example.RealMatch.business.presentation.dto.response.TagContentSortResponse;
import com.example.RealMatch.tag.domain.entity.TagContent;
import com.example.RealMatch.tag.domain.repository.TagContentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagContentSortQueryService {

    private final TagContentRepository tagContentRepository;

    public List<TagContentSortResponse> getAllGroupedBySort() {

        List<TagContent> allTags = tagContentRepository.findAll();

        return Arrays.stream(ContentTagSort.values())
                .map(sort -> {
                    List<TagContent> filtered = allTags.stream()
                            .filter(tag ->
                                    tag.getTagType().name().equals(sort.name())
                            )
                            .toList();

                    return TagContentSortResponse.from(sort, filtered);
                })
                .toList();
    }
}





