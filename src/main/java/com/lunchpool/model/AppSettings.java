package com.lunchpool.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "settings")
public class AppSettings {
    @Id
    private Short id = 1;
    @Column(name = "pool_minor", nullable = false)
    private int poolMinor;
    @Column(nullable = false)
    private String currency;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected AppSettings() {
    }

    public int getPoolMinor() {
        return poolMinor;
    }

    public void setPoolMinor(int v) {
        poolMinor = v;
    }

    public String getCurrency() {
        return currency;
    }

    @PreUpdate
    void markUpdated() {
        updatedAt = Instant.now();
    }
}
