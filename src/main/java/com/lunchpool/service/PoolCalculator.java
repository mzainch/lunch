package com.lunchpool.service;

import java.util.*;

public final class PoolCalculator {
    private PoolCalculator() {
    }

    public record Share(long friendId, int costMinor) {
    }

    public record DayResult(int billMinor, List<Share> shares, int orderingCount, boolean flagged) {
    }

    public static DayResult split(int billMinor, List<Long> orderedFriendIds, int dayOfMonth) {
        if (billMinor < 0)
            throw new IllegalArgumentException("Bill cannot be negative");
        if (orderedFriendIds.isEmpty())
            return new DayResult(billMinor, List.of(), 0, billMinor > 0);
        int base = billMinor / orderedFriendIds.size();
        int remainder = (int) (billMinor % orderedFriendIds.size());
        List<Share> shares = new ArrayList<>();
        int start = Math.floorMod(dayOfMonth, orderedFriendIds.size());
        for (int i = 0; i < orderedFriendIds.size(); i++) {
            int offset = Math.floorMod(i - start, orderedFriendIds.size());
            shares.add(new Share(orderedFriendIds.get(i), base + (offset < remainder ? 1 : 0)));
        }
        return new DayResult(billMinor, List.copyOf(shares), orderedFriendIds.size(), false);
    }

    public static Map<Long, Integer> monthlySpent(List<DayInput> days) {
        Map<Long, Integer> result = new HashMap<>();
        for (DayInput day : days)
            for (ShareInput share : day.shares())
                result.merge(share.friendId(), share.costMinor(), Integer::sum);
        return result;
    }

    public record DayInput(List<ShareInput> shares) {
    }

    public record ShareInput(long friendId, int costMinor) {
    }

    public static int nextPayment(int pool, int spent) {
        return spent;
    }
}
