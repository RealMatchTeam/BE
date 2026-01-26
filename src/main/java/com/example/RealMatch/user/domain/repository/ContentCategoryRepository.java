package com.example.RealMatch.user.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.ContentCategory;

public interface ContentCategoryRepository extends JpaRepository<ContentCategory, Long> {
}
