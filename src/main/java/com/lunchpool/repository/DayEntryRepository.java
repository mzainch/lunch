package com.lunchpool.repository;

import com.lunchpool.model.*;
import org.springframework.data.jpa.repository.*;
import java.time.LocalDate;
import java.util.*;

public interface DayEntryRepository extends JpaRepository<DayEntry, DayEntryId> {
    @Query("select e from DayEntry e join fetch e.day d join fetch e.friend f where d.date between :from and :to")
    List<DayEntry> findMonth(LocalDate from, LocalDate to);

    List<DayEntry> findByDay(LunchDay day);

    void deleteByDay(LunchDay day);
}
