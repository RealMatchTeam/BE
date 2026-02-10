package com.example.RealMatch.user.domain.entity;

import com.example.RealMatch.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_matching_detail")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMatchingDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 매칭에 필요한 최소 정보만 유지
    @Column(name = "sns_url")
    private String snsUrl;

    @Column(name = "creator_type")
    private String creatorType;

    @Column(name = "is_deprecated", nullable = false)
    private Boolean isDeprecated = false;

    @Builder
    public UserMatchingDetail(Long userId, String snsUrl) {
        this.userId = userId;
        this.snsUrl = snsUrl;
        this.isDeprecated = false;
    }

    public void setMatchingResult(String creatorType) {
        this.creatorType = creatorType;
    }

    public void deprecated() {
        this.isDeprecated = true;
    }

    // 인스타 아이디 업데이트 메서드
    public void updateSns(String snsAccount) {
        if (snsAccount != null && !snsAccount.isBlank()) {
            // URL 형식으로 저장 (기존 방식 유지)
            String cleanAccount = snsAccount.trim().replace("@", "");
            this.snsUrl = "https://www.instagram.com/" + cleanAccount + "/";
        } else {
            this.snsUrl = null;
        }
    }
}
