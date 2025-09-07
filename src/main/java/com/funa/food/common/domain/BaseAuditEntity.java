package com.funa.food.common.domain;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * Common audit fields for all entities.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseAuditEntity {

    @CreationTimestamp
    private OffsetDateTime createdAt;

    private String createdId;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    private String updatedId;
}
