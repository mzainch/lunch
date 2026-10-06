package com.lunchpool.web;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String handle(Exception exception, HttpServletRequest request, RedirectAttributes redirect) {
        log.error("Unhandled application error on {} {}", request.getMethod(), request.getRequestURI(), exception);
        redirect.addFlashAttribute("errorMessage", messageFor(exception));
        return "redirect:" + returnPath(request);
    }

    private String messageFor(Exception exception) {
        if (exception instanceof IllegalArgumentException || exception instanceof IllegalStateException)
            return exception.getMessage() == null ? "The requested change could not be saved." : exception.getMessage();
        return "The change could not be saved. Please try again.";
    }

    private String returnPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        if ("/entry".equals(path)) {
            String date = request.getParameter("date");
            return date == null || date.isBlank() ? "/entry" : "/entry?date=" + date;
        }
        if ("/dues".equals(path)) {
            String month = request.getParameter("month");
            return month == null || month.isBlank() ? "/dues" : "/dues?month=" + month;
        }
        if (path.startsWith("/settings"))
            return "/settings";
        return "/";
    }
}