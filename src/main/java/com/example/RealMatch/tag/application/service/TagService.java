package com.example.RealMatch.tag.application.service;

import com.example.RealMatch.tag.presentation.dto.response.TagResponse;

public interface TagService {

    TagResponse getTagsByType(String tagType);

    TagResponse getFashionTags();

    TagResponse getBeautyTags();
}
