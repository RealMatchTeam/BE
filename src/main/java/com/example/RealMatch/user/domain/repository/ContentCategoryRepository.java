package com.example.RealMatch.user.domain.repository;

import java.util.UUID;

import com.example.RealMatch.user.domain.entity.ContentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentCategoryRepository extends JpaRepository<ContentCategory, UUID> {
}
