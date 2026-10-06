package com.lunchpool.model;

import jakarta.persistence.*;

@Entity
@Table(name = "day_entries")
@IdClass(DayEntryId.class)
public class DayEntry {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id")
    private LunchDay day;
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id")
    private Friend friend;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    protected DayEntry() {
    }

    public DayEntry(LunchDay day, Friend friend, Status status) {
        this.day = day;
        this.friend = friend;
        this.status = status;
    }

    public LunchDay getDay() {
        return day;
    }

    public Friend getFriend() {
        return friend;
    }

    public Status getStatus() {
        return status;
    }
}
