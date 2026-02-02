package com.example.RealMatch.brand.application.service;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.brand.domain.entity.BrandCategory;
import com.example.RealMatch.brand.domain.repository.BrandCategoryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BrandDataInitializer implements CommandLineRunner {

    private final BrandCategoryRepository brandCategoryRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        List<String> categories = List.of("스킨케어", "메이크업", "의류");

        for (String categoryName : categories) {
            if (brandCategoryRepository.findByName(categoryName).isEmpty()) {
                BrandCategory newCategory = BrandCategory.builder()
                        .name(categoryName)
                        .build();
                brandCategoryRepository.save(newCategory);
            }
        }
    }
}
