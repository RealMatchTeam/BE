package com.example.RealMatch.brand.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.brand.domain.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByBrandId(Long brandId);

    @Query("SELECT t FROM Tag t JOIN FETCH t.tagParent WHERE t.brand.id = :brandId")
    List<Tag> findAllByBrand_IdWithTagParent(@Param("brandId") Long brandId);

    List<Tag> findByUserId(Long userId);

    List<Tag> findByTagParentTagParentId(Long tagParentId);

    List<Tag> findByNameContaining(String name);
}
