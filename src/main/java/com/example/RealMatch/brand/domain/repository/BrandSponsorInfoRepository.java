package com.example.RealMatch.brand.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.brand.domain.entity.BrandSponsorInfo;

public interface BrandSponsorInfoRepository extends JpaRepository<BrandSponsorInfo, Long> {

    @Query("SELECT DISTINCT si FROM BrandSponsorInfo si LEFT JOIN FETCH si.items WHERE si.sponsor.id = :sponsorId")
    Optional<BrandSponsorInfo> findBySponsorIdWithItems(@Param("sponsorId") Long sponsorId);

    @Query("SELECT DISTINCT si FROM BrandSponsorInfo si LEFT JOIN FETCH si.items WHERE si.sponsor.id IN :sponsorIds")
    List<BrandSponsorInfo> findBySponsorIdInWithItems(@Param("sponsorIds") List<Long> sponsorIds);
}
