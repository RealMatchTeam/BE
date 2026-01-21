package com.example.RealMatch.user.domain.entity;

import java.time.LocalDateTime;

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
@Table(name = "p_term")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermEntity {

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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public TermEntity(TermName name, String version, boolean isRequired) {
        this.name = name;
        this.version = version;
        this.isRequired = isRequired;
        this.createdAt = LocalDateTime.now();
    }

    public void updateVersion(String version) {
        this.version = version;
        this.updatedAt = LocalDateTime.now();
    }
}
