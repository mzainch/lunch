package com.lunchpool.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(WebController.class);
    @Value("${APP_PASSWORD:lunchandmunch}")
    String appPassword;
    private final ConcurrentHashMap<String, Attempt> failures = new ConcurrentHashMap<>();

    private record Attempt(int count, Instant firstFailure) {
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            public String encode(CharSequence raw) {
                return raw.toString();
            }

            public boolean matches(CharSequence raw, String encoded) {
                return MessageDigest.isEqual(raw.toString().getBytes(StandardCharsets.UTF_8),
                        encoded.getBytes(StandardCharsets.UTF_8));
            }
        };
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http, PasswordEncoder encoder) throws Exception {
        var user = User.withUsername("group").password(encoder.encode(appPassword)).roles("USER").build();
        log.info("Security configuration initialized with login password configured");
        http.authorizeHttpRequests(a -> a.requestMatchers("/login", "/ping", "/actuator/health", "/css/**").permitAll()
                .anyRequest().authenticated())
                .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/", true).failureHandler(failureHandler())
                        .permitAll())
                .logout(l -> l.logoutSuccessUrl("/login?logout")).csrf(c -> c.ignoringRequestMatchers("/ping"));
        return http.userDetailsService(new org.springframework.security.provisioning.InMemoryUserDetailsManager(user))
                .build();
    }

    @Bean
    AuthenticationFailureHandler failureHandler() {
        return (request, response, exception) -> {
            String ip = request.getRemoteAddr();
            Instant now = Instant.now();
            Attempt old = failures.get(ip);
            Attempt current = old == null || now.isAfter(old.firstFailure().plusSeconds(900))
                    ? new Attempt(1, now)
                    : new Attempt(old.count() + 1, old.firstFailure());
            failures.put(ip, current);
            if (current.count() > 5)
                response.sendError(429, "Too many login attempts. Try again later.");
            else
                response.sendRedirect("/login?error");
        };
    }
}
