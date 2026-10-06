package com.lunchpool.service;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PoolCalculatorTest {
    @Test
    void evenSplit() {
        var r = PoolCalculator.split(900, List.of(1L, 2L, 3L), 5);
        assertEquals(List.of(300, 300, 300), r.shares().stream().map(PoolCalculator.Share::costMinor).toList());
    }

    @Test
    void unevenRemainderRotatesAndSums() {
        var r = PoolCalculator.split(10, List.of(1L, 2L, 3L), 1);
        assertEquals(10, r.shares().stream().mapToLong(PoolCalculator.Share::costMinor).sum());
        assertEquals(List.of(3, 4, 3), r.shares().stream().map(PoolCalculator.Share::costMinor).toList());
    }

    @Test
    void nobodyOrderedFlags() {
        var r = PoolCalculator.split(500, List.of(), 1);
        assertTrue(r.flagged());
        assertEquals(0, r.orderingCount());
    }

    @Test
    void zeroBill() {
        assertTrue(PoolCalculator.split(0, List.of(1L, 2L), 1).shares().stream().allMatch(s -> s.costMinor() == 0));
    }

    @Test
    void workedExample() {
        var days = List.of(
                new PoolCalculator.DayInput(List.of(new PoolCalculator.ShareInput(1, 300),
                        new PoolCalculator.ShareInput(2, 300), new PoolCalculator.ShareInput(3, 300))),
                new PoolCalculator.DayInput(List.of(new PoolCalculator.ShareInput(2, 200),
                        new PoolCalculator.ShareInput(3, 200), new PoolCalculator.ShareInput(4, 200))),
                new PoolCalculator.DayInput(
                        List.of(new PoolCalculator.ShareInput(1, 250), new PoolCalculator.ShareInput(4, 250))));
        assertEquals(Map.of(1L, 550, 2L, 500, 3L, 500, 4L, 450), PoolCalculator.monthlySpent(days));
    }
}
