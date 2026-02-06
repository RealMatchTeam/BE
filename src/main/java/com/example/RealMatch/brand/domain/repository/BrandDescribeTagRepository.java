package com.example.RealMatch.brand.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.RealMatch.brand.domain.entity.BrandDescribeTag;

@Repository
public interface BrandDescribeTagRepository extends JpaRepository<BrandDescribeTag, Long> {

    List<BrandDescribeTag> findAllByBrandId(Long brandId);
}
