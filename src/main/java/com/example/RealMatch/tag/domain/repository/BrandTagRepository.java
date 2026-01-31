package com.example.RealMatch.tag.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.tag.domain.entity.BrandTag;

public interface BrandTagRepository extends JpaRepository<BrandTag, Long> {

    @Query("""
        select bt
        from BrandTag bt
        join fetch bt.tag t
        where bt.brand.id = :brandId
    """)
    List<BrandTag> findAllByBrandIdWithTag(@Param("brandId") Long brandId);

    List<BrandTag> findAllByBrandId(Long brandId);

    void deleteByBrandId(Long brandId);

    void deleteAllByBrand(Brand brand);
}
