package com.example.RealMatch.tag.presentation.dto.response;

import java.util.List;
import java.util.Map;

public record TagResponse(
        String tagType,
        Map<String, List<TagItem>> categories
) {
    public record TagItem(
            Long id,
            String name
    ) {}
}
