package com.lunchpool.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "friends")
public class Friend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private boolean active = true;
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
    @Column(name = "starting_amount_minor", nullable = false)
    private int startingAmountMinor;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Friend() {
    }

    public Friend(String name, int sortOrder) {
        this.name = name;
        this.sortOrder = sortOrder;
        this.startingAmountMinor = 0;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String v) {
        name = v;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean v) {
        active = v;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int v) {
        sortOrder = v;
    }

    public int getStartingAmountMinor() {
        return startingAmountMinor;
    }

    public void setStartingAmountMinor(int v) {
        startingAmountMinor = v;
    }
}
