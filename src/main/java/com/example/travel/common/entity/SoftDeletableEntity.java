package com.example.travel.common.entity;

import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class SoftDeletableEntity extends BaseAuditEntity {

    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
