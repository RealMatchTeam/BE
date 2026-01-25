package com.example.RealMatch.user.domain.repository;

import java.util.UUID;

import com.example.RealMatch.user.domain.entity.UserContentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserContentCategoryRepository extends JpaRepository<UserContentCategory, UUID> {
}
