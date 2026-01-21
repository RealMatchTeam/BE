package com.example.RealMatch.brand.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.brand.domain.entity.BrandTagParent;

public interface BrandTagParentRepository extends JpaRepository<BrandTagParent, Long> {

    List<BrandTagParent> findByBrandId(Long brandId);
}
