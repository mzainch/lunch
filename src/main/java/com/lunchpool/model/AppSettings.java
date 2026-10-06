package com.lunchpool.model;

import jakarta.persistence.*;

@Entity
@Table(name = "settings")
public class AppSettings {
    @Id
    private Short id = 1;
    @Column(name = "pool_minor", nullable = false)
    private int poolMinor;
    @Column(nullable = false)
    private String currency;

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
}
