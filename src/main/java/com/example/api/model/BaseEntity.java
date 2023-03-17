package com.example.api.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@MappedSuperclass
public abstract class BaseEntity {
    private static final String ZONE_ID = "UTC+7";
    @Column @Getter @Setter
    protected ZonedDateTime createdAt;

    @Column @Getter @Setter
    protected ZonedDateTime updatedAt;

    @Column @Getter @Setter
    protected ZonedDateTime deletedAt;

    @PrePersist
    public void generateCreatedAt(){
        if (createdAt == null)
            this.createdAt = ZonedDateTime.now(ZoneId.of(ZONE_ID));
        this.updatedAt = ZonedDateTime.now(ZoneId.of(ZONE_ID));
    }

    @PreUpdate
    public void touchUpdatedAt(){
        this.updatedAt = ZonedDateTime.now(ZoneId.of(ZONE_ID));
    }
}
