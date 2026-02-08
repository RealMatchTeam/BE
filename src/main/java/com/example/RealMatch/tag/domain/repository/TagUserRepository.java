package com.example.RealMatch.tag.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.tag.domain.entity.TagUser;

public interface TagUserRepository extends JpaRepository<TagUser, Long> {

    @Query("""
    select tu from TagUser tu
    join fetch tu.tag t
    where tu.user.id = :userId
    and tu.isDeprecated = false
    """)
    List<TagUser> findAllByUserIdWithTag(@Param("userId") Long userId);

    List<TagUser> findAllByUserId(Long userId);

    void deleteByUserId(Long userId);
}
