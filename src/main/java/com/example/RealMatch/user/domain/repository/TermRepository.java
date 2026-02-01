package com.example.RealMatch.user.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.Term;
import com.example.RealMatch.user.domain.entity.enums.TermName;

public interface TermRepository extends JpaRepository<Term, Long> {

    List<Term> findByNameIn(List<TermName> names);

    List<Term> findByIsRequired(boolean isRequired);
}
