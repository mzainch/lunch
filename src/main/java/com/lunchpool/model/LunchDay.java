package com.lunchpool.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "days")
public class LunchDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "date", nullable = false)
    private LocalDate date;
    @Column(name = "bill_minor", nullable = false)
    private int billMinor;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    @Column(name = "updated_by")
    private String updatedBy;

    protected LunchDay() {
    }

    public LunchDay(LocalDate date, int billMinor, String updatedBy) {
        this.date = date;
        this.billMinor = billMinor;
        this.updatedBy = updatedBy;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate v) {
        date = v;
    }

    public int getBillMinor() {
        return billMinor;
    }

    public void setBillMinor(int v) {
        billMinor = v;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String v) {
        updatedBy = v;
    }

    public void touch() {
        updatedAt = Instant.now();
    }
}
