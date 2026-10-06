package com.lunchpool.model;

import jakarta.persistence.*;
import java.time.LocalDate;

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
}