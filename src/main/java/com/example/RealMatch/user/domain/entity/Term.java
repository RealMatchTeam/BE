package com.example.RealMatch.user.domain.entity;

import java.time.LocalDateTime;

import com.example.RealMatch.global.common.BaseEntity;
import com.example.RealMatch.user.domain.entity.enums.TermName;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "term")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Term extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TermName name;

    @Column(nullable = false, length = 50)
    private String version;

    @Column(name = "is_required", nullable = false)
    private boolean isRequired;

    @Builder
    public Term(TermName name, String version, boolean isRequired) {
        this.name = name;
        this.version = version;
        this.isRequired = isRequired;
    }

    public void updateVersion(String version) {
        this.version = version;
    }
}
