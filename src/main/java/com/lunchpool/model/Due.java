package com.lunchpool.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(name = "dues", uniqueConstraints = @UniqueConstraint(name = "uk_dues_month_friend", columnNames = { "month",
        "friend_id" }))
public class Due {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDate month;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "friend_id", nullable = false)
    private Friend friend;
    @Column(name = "amount_minor", nullable = false)
    private int amountMinor;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected Due() {
    }

    public Due(LocalDate month, Friend friend, int amountMinor) {
        this.month = month;
        this.friend = friend;
        this.amountMinor = amountMinor;
    }

    public LocalDate getMonth() {
        return month;
    }

    public Friend getFriend() {
        return friend;
    }

    public int getAmountMinor() {
        return amountMinor;
    }

    public void setAmountMinor(int amountMinor) {
        this.amountMinor = amountMinor;
    }

    @PreUpdate
    void markUpdated() {
        updatedAt = Instant.now();
    }
}