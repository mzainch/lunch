package com.lunchpool.model;

import java.io.Serializable;

public class DayEntryId implements Serializable {
    private Long day;
    private Long friend;

    public DayEntryId() {
    }

    public DayEntryId(Long day, Long friend) {
        this.day = day;
        this.friend = friend;
    }

    public boolean equals(Object o) {
        return o instanceof DayEntryId x && java.util.Objects.equals(day, x.day)
                && java.util.Objects.equals(friend, x.friend);
    }

    public int hashCode() {
        return java.util.Objects.hash(day, friend);
    }
}
