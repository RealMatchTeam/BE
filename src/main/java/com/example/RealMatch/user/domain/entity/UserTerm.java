package com.example.RealMatch.user.domain.entity;

import java.time.LocalDateTime;

import com.example.RealMatch.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_term")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTerm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id", nullable = false)
    private Term term;

    @Column(name = "is_agreed", nullable = false)
    private boolean isAgreed;

    @Column(name = "agreed_at")
    private LocalDateTime agreedAt;

    @Builder
    public UserTerm(User user, Term term, boolean isAgreed) {
        this.user = user;
        this.term = term;
        this.isAgreed = isAgreed;
        if (isAgreed) {
            this.agreedAt = LocalDateTime.now();
        }
    }

    public void agree() {
        this.isAgreed = true;
        this.agreedAt = LocalDateTime.now();
    }

    public void disagree() {
        this.isAgreed = false;
        this.agreedAt = null;
    }
}
