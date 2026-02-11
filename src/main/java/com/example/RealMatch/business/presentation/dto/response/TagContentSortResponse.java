package com.example.RealMatch.business.presentation.dto.response;

import java.util.List;

import com.example.RealMatch.business.domain.enums.ContentTagSort;
import com.example.RealMatch.tag.domain.entity.TagContent;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TagContentSortResponse {

    private ContentTagSort sort;
    private String sortKorName;
    private List<TagItemResponse> tags;

    public static TagContentSortResponse from(
            ContentTagSort sort,
            List<TagContent> tagContents
    ) {
        return TagContentSortResponse.builder()
                .sort(sort)
                .sortKorName(sort.getKorName())
                .tags(tagContents.stream()
                        .map(tag -> new TagItemResponse(
                                tag.getId(),
                                tag.getKorName()
                        ))
                        .toList())
                .build();
    }
}


