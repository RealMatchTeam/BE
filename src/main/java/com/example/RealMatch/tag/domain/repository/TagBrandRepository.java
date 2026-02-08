package com.example.RealMatch.tag.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.tag.domain.entity.TagBrand;

public interface TagBrandRepository extends JpaRepository<TagBrand, Long> {

    @Query("""
        select tb
        from TagBrand tb
        join fetch tb.tag t
        where tb.brand.id = :brandId
    """)
    List<TagBrand> findAllByBrandIdWithTag(@Param("brandId") Long brandId);

    @Query("""
        select tb
        from TagBrand tb
        join fetch tb.tag t
        where tb.brand.id = :brandId
        and t.tagCategory = :tagCategory
    """)
    List<TagBrand> findAllByBrandIdAndTagCategory(@Param("brandId") Long brandId, @Param("tagCategory") String tagCategory);

    @Query("""
        select t.tagName
        from TagBrand tb
        join tb.tag t
        where tb.brand.id = :brandId
        and t.tagCategory = :tagCategory
    """)
    List<String> findTagNamesByBrandIdAndTagCategory(@Param("brandId") Long brandId, @Param("tagCategory") String tagCategory);

    List<TagBrand> findAllByBrandId(Long brandId);

    void deleteByBrandId(Long brandId);

    void deleteAllByBrand(Brand brand);
}
