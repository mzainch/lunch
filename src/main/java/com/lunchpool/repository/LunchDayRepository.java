package com.lunchpool.repository;

import com.lunchpool.model.LunchDay;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.*;

public interface LunchDayRepository extends JpaRepository<LunchDay, Long> {
    List<LunchDay> findByDateOrderByIdAsc(LocalDate date);

    List<LunchDay> findByDateBetweenOrderByDateAscIdAsc(LocalDate from, LocalDate to);
}
