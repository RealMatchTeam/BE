package com.example.RealMatch.user.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.user.domain.entity.UserContentCategory;

public interface UserContentCategoryRepository
        extends JpaRepository<UserContentCategory, Long> {

    @Query("""
        select ucc
        from UserContentCategory ucc
        join fetch ucc.contentCategory
        where ucc.user.id = :userId
    """)
    List<UserContentCategory> findByUserId(@Param("userId") Long userId);
}
