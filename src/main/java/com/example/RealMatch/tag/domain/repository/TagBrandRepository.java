package com.example.RealMatch.tag.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.tag.domain.entity.BrandTag;

public interface TagBrandRepository extends JpaRepository<BrandTag, Long> {

    @Query("""
        select bt
        from BrandTag bt
        join fetch bt.tag t
        where bt.brand.id = :brandId
    """)
    List<BrandTag> findAllByBrandIdWithTag(@Param("brandId") Long brandId);

    @Query("""
        select bt
        from BrandTag bt
        join fetch bt.tag t
        where bt.brand.id = :brandId
        and t.tagCategory = :tagCategory
    """)
    List<BrandTag> findAllByBrandIdAndTagCategory(@Param("brandId") Long brandId, @Param("tagCategory") String tagCategory);

    @Query("""
        select t.tagName
        from BrandTag bt
        join bt.tag t
        where bt.brand.id = :brandId
        and t.tagCategory = :tagCategory
    """)
    List<String> findTagNamesByBrandIdAndTagCategory(@Param("brandId") Long brandId, @Param("tagCategory") String tagCategory);

    List<BrandTag> findAllByBrandId(Long brandId);

    void deleteByBrandId(Long brandId);

    void deleteAllByBrand(Brand brand);
}
