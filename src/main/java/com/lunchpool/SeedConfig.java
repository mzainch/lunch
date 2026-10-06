package com.lunchpool;

import com.lunchpool.model.*;
import com.lunchpool.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import java.time.LocalDate;
import java.util.List;

@Configuration
@Profile("seed")
public class SeedConfig {
    @Bean
    CommandLineRunner seed(FriendRepository friends, LunchDayRepository days, DayEntryRepository entries) {
        return args -> {
            if (friends.count() > 0)
                return;
            var ana = friends.save(new Friend("Ana", 0));
            var ben = friends.save(new Friend("Ben", 1));
            var cleo = friends.save(new Friend("Cleo", 2));
            var dev = friends.save(new Friend("Dev", 3));
            add(days, entries, LocalDate.of(2024, 10, 5), 900, List.of(ana, ben, cleo), List.of(dev));
            add(days, entries, LocalDate.of(2024, 10, 6), 600, List.of(ben, cleo, dev), List.of(ana));
            add(days, entries, LocalDate.of(2024, 10, 7), 500, List.of(ana, dev), List.of(ben, cleo));
        };
    }

    private void add(LunchDayRepository days, DayEntryRepository entries, LocalDate date, int bill,
            List<Friend> ordered, List<Friend> other) {
        var day = days.save(new LunchDay(date, bill, "seed"));
        for (var f : ordered)
            entries.save(new DayEntry(day, f, Status.ORDERED_OUT));
        for (var f : other)
            entries.save(new DayEntry(day, f, Status.NO_LUNCH));
    }
}
