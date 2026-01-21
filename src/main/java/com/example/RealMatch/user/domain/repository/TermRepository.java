package com.example.RealMatch.user.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.TermEntity;
import com.example.RealMatch.user.domain.entity.enums.TermName;

public interface TermRepository extends JpaRepository<TermEntity, Long> {

    Optional<TermEntity> findByName(TermName name);

    List<TermEntity> findByIsRequired(boolean isRequired);
}
