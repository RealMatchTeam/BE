package com.example.RealMatch.brand.application;

import com.example.RealMatch.brand.domain.entity.BrandLike;
import com.example.RealMatch.brand.presentation.dto.response.*;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.entity.BrandAvailableSponsor;
import com.example.RealMatch.brand.domain.repository.BrandAvailableSponsorRepository;
import com.example.RealMatch.brand.domain.repository.BrandCategoryViewRepository;
import com.example.RealMatch.brand.domain.repository.BrandLikeRepository;
import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.brand.domain.repository.BrandTagParentRepository;
import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.campaign.domain.repository.CampaignRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandService {

    private final BrandRepository brandRepository;
    private final BrandTagParentRepository brandTagParentRepository;
    private final BrandLikeRepository brandLikeRepository;
    private final BrandCategoryViewRepository brandCategoryViewRepository;
    private final CampaignRepository campaignRepository;
    private final BrandAvailableSponsorRepository brandAvailableSponsorRepository;
    private final UserRepository userRepository;
    // private final CategoryRepository categoryRepository; // Assuming these repositories exist
    // private final FunctionRepository functionRepository;
    // private final SkinTypeRepository skinTypeRepository;
    // private final MakeUpStyleRepository makeUpStyleRepository;

    public BrandDetailResponseDto getBrandDetail(Long brandId) {
        // ... (existing code)
        return null;
    }

    @Transactional
    public Boolean likeBrand(Long brandId) {
        // ... (existing code)
        return null;
    }

    public BrandFilterResponseDto getBrandFilters() {
        // List<CategoryDto> categories = categoryRepository.findAll().stream()
        //         .map(c -> new CategoryDto(c.getId(), c.getName()))
        //         .collect(Collectors.toList());

        // List<FunctionDto> functions = functionRepository.findAll().stream()
        //         .map(f -> new FunctionDto(f.getId(), f.getName()))
        //         .collect(Collectors.toList());

        // List<SkinTypeDto> skinTypes = skinTypeRepository.findAll().stream()
        //         .map(s -> new SkinTypeDto(s.getId(), s.getName()))
        //         .collect(Collectors.toList());

        // List<MakeUpStyleDto> makeUpStyles = makeUpStyleRepository.findAll().stream()
        //         .map(m -> new MakeUpStyleDto(m.getId(), m.getName()))
        //         .collect(Collectors.toList());

        // BeautyFilterDto beautyFilter = BeautyFilterDto.builder()
        //         .category(categories)
        //         .function(functions)
        //         .skinType(skinTypes)
        //         .makeUpStyle(makeUpStyles)
        //         .build();

        // return BrandFilterResponseDto.builder()
        //         .beautyFilter(beautyFilter)
        //         .build();

        // For now, returning empty lists as the repositories are not yet available.
        BeautyFilterDto beautyFilter = BeautyFilterDto.builder()
                .category(List.of())
                .function(List.of())
                .skinType(List.of())
                .makeUpStyle(List.of())
                .build();

        return BrandFilterResponseDto.builder()
                .beautyFilter(beautyFilter)
                .build();
    }
}
