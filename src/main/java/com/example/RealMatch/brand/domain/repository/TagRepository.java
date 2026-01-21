package com.example.RealMatch.brand.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.brand.domain.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByBrandId(Long brandId);

    List<Tag> findByUserId(Long userId);

    List<Tag> findByTagParentTagParentId(Long tagParentId);

    List<Tag> findByNameContaining(String name);
}
