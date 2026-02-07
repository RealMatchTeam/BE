package com.example.RealMatch.tag.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.tag.domain.entity.UserTag;

public interface UserTagRepository extends JpaRepository<UserTag, Long> {

    @Query("""
    select ut from UserTag ut
    join fetch ut.tag t
    where ut.user.id = :userId
    and ut.isDeprecated = false
    """)
    List<UserTag> findAllByUserIdWithTag(@Param("userId") Long userId);

    List<UserTag> findAllByUserId(Long userId);

    void deleteByUserId(Long userId);
}
