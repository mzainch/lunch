package com.lunchpool.repository;

import com.lunchpool.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.*;

public interface DueRepository extends JpaRepository<Due, Long> {
    List<Due> findByMonth(LocalDate month);

    Optional<Due> findByMonthAndFriend(LocalDate month, Friend friend);

    List<Due> findByFriend(Friend friend);
}