package com.lunchpool.repository;

import com.lunchpool.model.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findAllByOrderBySortOrderAscNameAsc();

    List<Friend> findByActiveTrueOrderBySortOrderAscNameAsc();
}
