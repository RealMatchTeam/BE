package com.example.RealMatch.global.common;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.hibernate.annotations.Where;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Where(clause = "is_deleted = false")
public abstract class DeleteBaseEntity extends BaseEntity {

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    protected DeleteBaseEntity() {
        super();
    }

    /**
     * Soft delete 기록용.
     * 실제 DB row를 삭제하지 않고,
     * is_deleted 플래그와 deleted_at만 갱신한다.
     */
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
    }
}
