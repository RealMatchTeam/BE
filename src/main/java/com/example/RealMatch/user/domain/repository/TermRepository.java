package com.example.RealMatch.user.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.Term;
import com.example.RealMatch.user.domain.entity.enums.TermName;

public interface TermRepository extends JpaRepository<Term, UUID> {

    Optional<Term> findByName(TermName name);

    List<Term> findByIsRequired(boolean isRequired);
}
