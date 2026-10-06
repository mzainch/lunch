package com.lunchpool.service;

import com.lunchpool.model.Friend;
import com.lunchpool.repository.AppSettingsRepository;
import com.lunchpool.repository.DayEntryRepository;
import com.lunchpool.repository.DueRepository;
import com.lunchpool.repository.FriendRepository;
import com.lunchpool.repository.LunchDayRepository;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class LunchServiceTest {
    @Test
    void cannotUpdateDueForInactiveFriend() {
        var friends = mock(FriendRepository.class);
        var dues = mock(DueRepository.class);
        var friend = new Friend("Inactive", 0);
        friend.setActive(false);
        when(friends.findById(7L)).thenReturn(Optional.of(friend));
        var service = new LunchService(friends, mock(LunchDayRepository.class), mock(DayEntryRepository.class),
                mock(AppSettingsRepository.class), dues);

        assertThrows(IllegalStateException.class,
                () -> service.updateDue(YearMonth.of(2026, 9), 7L, 100));
        verifyNoInteractions(dues);
    }
}