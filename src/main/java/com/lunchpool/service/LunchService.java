package com.lunchpool.service;

import com.lunchpool.model.*;
import com.lunchpool.repository.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;

@Service
public class LunchService {
    private static final Logger log = LoggerFactory.getLogger(LunchService.class);
    private final FriendRepository friends;
    private final LunchDayRepository days;
    private final DayEntryRepository entries;
    private final AppSettingsRepository settings;
    private final DueRepository dues;

    public LunchService(FriendRepository friends, LunchDayRepository days, DayEntryRepository entries,
            AppSettingsRepository settings, DueRepository dues) {
        this.friends = friends;
        this.days = days;
        this.entries = entries;
        this.settings = settings;
        this.dues = dues;
    }

    public List<Friend> allFriends() {
        return friends.findAllByOrderBySortOrderAscNameAsc();
    }

    public List<Friend> activeFriends() {
        return friends.findByActiveTrueOrderBySortOrderAscNameAsc();
    }

    public int pool() {
        return settings.findById((short) 1).orElseThrow().getPoolMinor();
    }

    public Optional<LunchDay> day(LocalDate date) {
        return days.findByDateOrderByIdAsc(date).stream().findFirst();
    }

    public List<LunchDay> monthDays(YearMonth month) {
        return days.findByDateBetweenOrderByDateAscIdAsc(month.atDay(1), month.atEndOfMonth());
    }

    public record Daily(LunchDay day, List<DayEntry> entries, PoolCalculator.DayResult result) {
    }

    public Daily daily(LocalDate date) {
        return daily(date, null);
    }

    public Daily daily(LocalDate date, Long dayId) {
        var records = days.findByDateOrderByIdAsc(date);
        var d = dayId == null ? records.stream().reduce((first, second) -> second).orElse(null)
                : days.findById(dayId).orElse(null);
        if (d != null && !d.getDate().equals(date))
            d = null;
        if (d == null) {
            log.debug("No lunch day found for {}", date);
            return null;
        }
        var es = entries.findByDay(d);
        var ids = es.stream().filter(e -> e.getStatus() == Status.ORDERED_OUT).map(e -> e.getFriend().getId()).toList();
        var result = PoolCalculator.split(d.getBillMinor(), ids, date.getDayOfMonth());
        log.debug("Loaded daily entry for {} with {} ordered IDs and bill {}", date, ids.size(), d.getBillMinor());
        return new Daily(d, es, result);
    }

    @Transactional
    public void saveDay(LocalDate date, Long dayId, int billMinor, Map<Long, Status> statuses, String updatedBy) {
        if (date.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Entries cannot be added for a future date");
        if (billMinor < 0)
            throw new IllegalArgumentException("Bill cannot be negative");
        log.info("Saving day {} with bill {} by {} for {} statuses", date, billMinor, updatedBy, statuses.size());
        var d = dayId == null ? new LunchDay(date, billMinor, updatedBy) : days.findById(dayId).orElseThrow();
        d.setDate(date);
        d.setBillMinor(billMinor);
        d.setUpdatedBy(updatedBy);
        d.touch();
        d = days.save(d);
        entries.deleteByDay(d);
        for (var f : friends.findAllById(statuses.keySet()))
            entries.save(new DayEntry(d, f, statuses.getOrDefault(f.getId(), Status.NO_LUNCH)));
    }

    @Transactional
    public void deleteDay(LocalDate date) {
        log.info("Deleting day {}", date);
        days.findByDateOrderByIdAsc(date).forEach(days::delete);
    }

    @Transactional
    public void deleteDay(long dayId) {
        days.deleteById(dayId);
    }

    @Transactional
    public Friend addFriend(String name) {
        log.info("Adding friend {}", name);
        return friends.save(new Friend(name.trim(), allFriends().size()));
    }

    @Transactional
    public void updateFriend(long id, String name, boolean active) {
        var friend = friends.findById(id).orElseThrow();
        updateFriend(friend, name, active, friend.getStartingAmountMinor());
    }

    @Transactional
    public void updateFriend(long id, String name, boolean active, int startingAmountMinor) {
        var friend = friends.findById(id).orElseThrow();
        updateFriend(friend, name, active, startingAmountMinor);
    }

    private void updateFriend(Friend f, String name, boolean active, int startingAmountMinor) {
        if (startingAmountMinor < 0)
            throw new IllegalArgumentException("Amount cannot be negative");
        if (f.isActive() && !active && currentBalance(f) < 0)
            throw new IllegalStateException("A friend with an outstanding balance cannot be deactivated");
        f.setName(name.trim());
        f.setActive(active);
        f.setStartingAmountMinor(startingAmountMinor);
        friends.save(f);
    }

    @Transactional
    public Friend addFriend(String name, int startingAmountMinor) {
        if (startingAmountMinor < 0)
            throw new IllegalArgumentException("Amount cannot be negative");
        var friend = new Friend(name.trim(), allFriends().size());
        friend.setStartingAmountMinor(startingAmountMinor);
        return friends.save(friend);
    }

    @Transactional
    public void setPool(int minor) {
        if (minor < 0)
            throw new IllegalArgumentException("Pool cannot be negative");
        var s = settings.findById((short) 1).orElseThrow();
        s.setPoolMinor(minor);
        settings.save(s);
    }

    public List<Due> dues(YearMonth month) {
        return dues.findByMonth(month.atDay(1));
    }

    @Transactional
    public void updateDue(YearMonth month, long friendId, int amountMinor) {
        if (amountMinor < 0)
            throw new IllegalArgumentException("Amount cannot be negative");
        var friend = friends.findById(friendId).orElseThrow();
        if (!friend.isActive())
            throw new IllegalStateException("Cannot add an amount for an inactive friend");
        var due = dues.findByMonthAndFriend(month.atDay(1), friend)
                .orElseGet(() -> new Due(month.atDay(1), friend, 0));
        due.setAmountMinor(amountMinor);
        dues.save(due);
    }

    public record ReportDay(LocalDate date, int amount) {
    }

    public record SummaryRow(Friend friend, int balance, int spent, int lastPayment, List<ReportDay> report) {
    }

    public record Summary(YearMonth month, List<SummaryRow> rows, int collected, int spent, int remaining,
            List<LunchDay> flagged) {
    }

    public Summary summary(YearMonth month) {
        var fs = allFriends();
        var ds = monthDays(month);
        var monthEntries = entries.findMonth(month.atDay(1), month.atEndOfMonth());
        var entriesByDay = monthEntries.stream()
                .collect(java.util.stream.Collectors.groupingBy(e -> e.getDay().getId()));
        var input = new ArrayList<PoolCalculator.DayInput>();
        var flagged = new ArrayList<LunchDay>();
        var reports = new HashMap<Long, List<ReportDay>>();
        for (var d : ds) {
            var es = entriesByDay.getOrDefault(d.getId(), List.of());
            var ids = es.stream().filter(e -> e.getStatus() == Status.ORDERED_OUT).map(e -> e.getFriend().getId())
                    .toList();
            var result = PoolCalculator.split(d.getBillMinor(), ids, d.getDate().getDayOfMonth());
            if (result.flagged())
                flagged.add(d);
            for (var share : result.shares())
                reports.computeIfAbsent(share.friendId(), ignored -> new ArrayList<>())
                        .add(new ReportDay(d.getDate(), share.costMinor()));
            input.add(new PoolCalculator.DayInput(result.shares().stream()
                    .map(s -> new PoolCalculator.ShareInput(s.friendId(), s.costMinor())).toList()));
        }

        var spentMap = PoolCalculator.monthlySpent(input);
        var totalSpent = spentMap.values().stream().mapToInt(Integer::intValue).sum();
        var duesMap = dues(month).stream().collect(java.util.stream.Collectors.toMap(d -> d.getFriend().getId(),
                Due::getAmountMinor));
        var rows = fs.stream()
                .filter(Friend::isActive)
                .map(f -> {
                    var spent = spentMap.getOrDefault(f.getId(), 0);
                    var paid = duesMap.getOrDefault(f.getId(), 0);
                    var starting = f.getStartingAmountMinor();
                    var available = starting + paid;
                    return new SummaryRow(f, available - spent, spent, paid,
                            List.copyOf(reports.getOrDefault(f.getId(), List.of())));
                })
                .toList();
        var collected = rows.stream().mapToInt(r -> r.balance() + r.spent()).sum();
        return new Summary(month, rows, collected, totalSpent, collected - totalSpent, flagged);
    }

    private long currentBalance(Friend friend) {
        var allEntries = entries.findAll();
        var entriesByDay = allEntries.stream().collect(java.util.stream.Collectors.groupingBy(DayEntry::getDay));
        int spent = 0;
        for (var dayEntries : entriesByDay.entrySet()) {
            var day = dayEntries.getKey();
            var ids = dayEntries.getValue().stream()
                    .filter(e -> e.getStatus() == Status.ORDERED_OUT)
                    .map(e -> e.getFriend().getId()).toList();
            spent += PoolCalculator.split(day.getBillMinor(), ids, day.getDate().getDayOfMonth()).shares().stream()
                    .filter(s -> s.friendId() == friend.getId()).mapToInt(PoolCalculator.Share::costMinor).sum();
        }
        var paid = dues.findByFriend(friend).stream().mapToInt(Due::getAmountMinor).sum();
        return friend.getStartingAmountMinor() + paid - spent;
    }
}
