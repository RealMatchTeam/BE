package com.example.RealMatch.brand.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.entity.enums.IndustryType;
import com.example.RealMatch.user.domain.entity.User;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findById(Long id);

    List<Brand> findAll();

    List<Brand> findByIndustryType(IndustryType industryType);

    List<Brand> findByBrandNameContaining(String brandName);

    Optional<Brand> findByCreatedBy(Long createdBy);

    List<Brand> findByCreatedByIn(List<Long> createdByList);

    Optional<Brand> findByUser(User user);

    @Query("""
        select b.id
        from Brand b
        where b.brandName like %:keyword%
    """)
    List<Long> findIdsByBrandNameContaining(@Param("keyword") String keyword);
}
