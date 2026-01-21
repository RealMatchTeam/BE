package com.example.RealMatch.brand.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.brand.domain.entity.BrandSponsorImage;

public interface BrandSponsorImageRepository extends JpaRepository<BrandSponsorImage, Long> {

    List<BrandSponsorImage> findBySponsorId(Long sponsorId);
}
