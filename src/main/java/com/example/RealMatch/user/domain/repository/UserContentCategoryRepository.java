package com.example.RealMatch.user.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.UserContentCategory;

public interface UserContentCategoryRepository extends JpaRepository<UserContentCategory, Long> {
    List<UserContentCategory> findByUserId(Long userId);
}
