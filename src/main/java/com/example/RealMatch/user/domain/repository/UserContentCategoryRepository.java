package com.example.RealMatch.user.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.UserContentCategory;

public interface UserContentCategoryRepository extends JpaRepository<UserContentCategory, UUID> {
}
