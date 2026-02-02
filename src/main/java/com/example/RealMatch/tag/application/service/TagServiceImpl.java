package com.example.RealMatch.tag.application.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.tag.domain.entity.Tag;
import com.example.RealMatch.tag.domain.enums.TagType;
import com.example.RealMatch.tag.domain.repository.TagRepository;
import com.example.RealMatch.tag.presentation.dto.response.TagResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    public TagResponse getTagsByType(String tagType) {
        List<Tag> tags = tagRepository.findAllByTagType(tagType);

        Map<String, List<TagResponse.TagItem>> categories = tags.stream()
                .collect(Collectors.groupingBy(
                        Tag::getTagCategory,
                        Collectors.mapping(
                                tag -> new TagResponse.TagItem(tag.getId(), tag.getTagName()),
                                Collectors.toList()
                        )
                ));

        return new TagResponse(tagType, categories);
    }

    @Override
    public TagResponse getFashionTags() {
        return getTagsByType(TagType.FASHION.getDescription());
    }

    @Override
    public TagResponse getBeautyTags() {
        return getTagsByType(TagType.BEAUTY.getDescription());
    }

    @Override
    public TagResponse getContentTags() {
        return getTagsByType(TagType.CONTENT.getDescription());
    }
}
