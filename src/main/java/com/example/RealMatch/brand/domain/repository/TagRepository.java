package com.example.RealMatch.brand.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.brand.domain.entity.TagEntity;

public interface TagRepository extends JpaRepository<TagEntity, Long> {

    List<TagEntity> findByBrandIdAndIsDeletedFalse(Long brandId);

    List<TagEntity> findByUserIdAndIsDeletedFalse(Long userId);

    List<TagEntity> findByTagParentTagParentIdAndIsDeletedFalse(Long tagParentId);

    List<TagEntity> findByNameContainingAndIsDeletedFalse(String name);
}
