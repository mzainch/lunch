package com.lunchpool.web;

import com.lunchpool.model.LunchDay;
import com.lunchpool.model.Status;
import com.lunchpool.model.Due;
import com.lunchpool.service.LunchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.*;
import java.util.*;

@Controller
public class WebController {
    private static final Logger log = LoggerFactory.getLogger(WebController.class);
    private final LunchService service;
    private final MoneyFormatter money;

    public WebController(LunchService service, MoneyFormatter money) {
        this.service = service;
        this.money = money;
    }

    @GetMapping("/ping")
    @ResponseBody
    public String ping() {
        return "ok";
    }

    @GetMapping({ "/", "/summary" })
    public String summary(@RequestParam(required = false) String month, Model m) {
        YearMonth ym = month == null ? YearMonth.now() : YearMonth.parse(month);
        log.info("Loading summary for month {}", ym);
        m.addAttribute("summary", service.summary(ym));
        return "summary";
    }

    @GetMapping("/entry")
    public String entry(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long id, @RequestParam(defaultValue = "false") boolean view,
            Model m) {
        LocalDate d = date == null ? LocalDate.now() : date;
        log.info("Opening entry page for date {}", d);
        var monthDays = service.monthDays(YearMonth.from(d));
        var safeMonthDays = monthDays == null ? List.<LunchDay>of()
                : monthDays.stream().filter(Objects::nonNull).toList();
        m.addAttribute("date", d);
        m.addAttribute("today", LocalDate.now());
        m.addAttribute("friends", service.activeFriends());
        var daily = id == null ? null : service.daily(d, id);
        var currentStatuses = new HashMap<Long, String>();
        if (daily != null)
            daily.entries().forEach(e -> currentStatuses.put(e.getFriend().getId(), e.getStatus().name()));
        m.addAttribute("daily", daily);
        m.addAttribute("existingDayId", service.day(d).map(LunchDay::getId).orElse(null));
        m.addAttribute("currentStatuses", currentStatuses);
        m.addAttribute("viewOnly", view);
        m.addAttribute("monthDays", safeMonthDays);
        return "entry";
    }

    @GetMapping("/entry/view")
    public String viewEntry(@RequestParam LocalDate date, @RequestParam long id, Model m) {
        return entry(date, id, true, m);
    }

    @PostMapping("/entry")
    public String save(@RequestParam LocalDate date, @RequestParam(required = false) Long id, @RequestParam String bill,
            @RequestParam Map<String, String> allParams, @RequestParam(defaultValue = "") String updatedBy,
            RedirectAttributes redirect) {
        int billMinor = money.toAmount(bill);
        Map<Long, Status> statuses = new HashMap<>();
        for (var f : service.allFriends())
            statuses.put(f.getId(), Status.valueOf(allParams.getOrDefault("status_" + f.getId(), "NO_LUNCH")));
        log.info("Saving entry for {} bill={} updatedBy={} statuses={}", date, billMinor,
                updatedBy.isBlank() ? "unknown" : updatedBy, statuses.size());
        service.saveDay(date, id, billMinor, statuses, updatedBy.isBlank() ? "unknown" : updatedBy);
        redirect.addFlashAttribute("successMessage", "Daily entry saved successfully.");
        return "redirect:/entry?date=" + date;
    }

    @PostMapping("/entry/delete")
    public String delete(@RequestParam LocalDate date, @RequestParam(required = false) Long id,
            RedirectAttributes redirect) {
        if (id == null)
            service.deleteDay(date);
        else
            service.deleteDay(id);
        redirect.addFlashAttribute("successMessage", "Daily entry deleted successfully.");
        return "redirect:/entry?date=" + date;
    }

    @GetMapping("/summary/report")
    public String report(@RequestParam String month, @RequestParam long friendId, Model m) {
        var summary = service.summary(YearMonth.parse(month));
        var row = summary.rows().stream().filter(r -> r.friend().getId() == friendId).findFirst().orElseThrow();
        m.addAttribute("summary", summary);
        m.addAttribute("row", row);
        return "report";
    }

    @GetMapping("/settings")
    public String settings(Model m) {
        m.addAttribute("friends", service.allFriends());
        m.addAttribute("pool", service.pool());
        return "settings";
    }

    @PostMapping("/settings/pool")
    public String pool(@RequestParam String pool, RedirectAttributes redirect) {
        service.setPool(money.toAmount(pool));
        redirect.addFlashAttribute("successMessage", "Pool settings updated successfully.");
        return "redirect:/settings";
    }

    @PostMapping("/settings/friend")
    public String friend(@RequestParam String name, RedirectAttributes redirect) {
        if (!name.isBlank())
            service.addFriend(name);
        redirect.addFlashAttribute("successMessage", "Friend added successfully.");
        return "redirect:/settings";
    }

    @PostMapping("/settings/friend/{id}")
    public String editFriend(@PathVariable long id, @RequestParam String name,
            @RequestParam(defaultValue = "false") boolean active, RedirectAttributes redirect) {
        service.updateFriend(id, name, active);
        redirect.addFlashAttribute("successMessage", "Friend updated successfully.");
        return "redirect:/settings";
    }

    @GetMapping("/dues")
    public String dues(@RequestParam(required = false) String month, Model m) {
        var ym = month == null ? YearMonth.now() : YearMonth.parse(month);
        var summary = service.summary(ym);
        var paid = service.dues(ym).stream().collect(java.util.stream.Collectors.toMap(d -> d.getFriend().getId(),
                Due::getAmountMinor));
        m.addAttribute("month", ym);
        m.addAttribute("friends", service.activeFriends());
        m.addAttribute("paid", paid);
        m.addAttribute("balances", summary.rows().stream()
                .collect(java.util.stream.Collectors.toMap(r -> r.friend().getId(), r -> r.balance())));
        return "dues";
    }

    @PostMapping("/dues")
    public String updateDue(@RequestParam String month, @RequestParam long friendId,
            @RequestParam String amount, RedirectAttributes redirect) {
        service.updateDue(YearMonth.parse(month), friendId, money.toAmount(amount));
        redirect.addFlashAttribute("successMessage", "Amount saved successfully.");
        return "redirect:/dues?month=" + month;
    }
}
