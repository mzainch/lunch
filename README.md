# Lunch Pool

A small, mobile-first shared monthly lunch pool for up to 20 friends. It uses Java 21, Spring Boot, PostgreSQL, Flyway, Thymeleaf, HTMX, and Tailwind CDN.

## Run locally

Prerequisites: JDK 21 and Maven 3.9 or newer. Verify with `java -version` and `mvn -version`; Maven must report Java 21, not the legacy Maven 3.0.4/Java 7 installation included in some older Windows environments.

1. Copy `.env.example` to `.env` and set `DATABASE_URL`, `DB_USER`, `DB_PASSWORD`, and `APP_PASSWORD`.
2. Start PostgreSQL, then run `mvn spring-boot:run`.
3. Open `http://localhost:8080` and sign in with the shared password.

Amounts are entered as normal currency amounts such as `92.00` and stored as integer minor units. Friends start with a zero balance. Payments recorded from the Dues page increase the selected month's balance, and each month's ending balance carries into the following month.

## Deploy on Render and Neon

1. Create a free Neon project and database.
2. Copy its connection details and convert the URL to JDBC form, for example `jdbc:postgresql://HOST/DB?sslmode=require`.
3. Push this repository to GitHub.
4. Create a Render web service from the repository. Render will use `render.yaml` and the Dockerfile.
5. Set `DATABASE_URL`, `DB_USER`, `DB_PASSWORD`, and `APP_PASSWORD` in Render. Deploy, then log in and add friends.

The first request after idle may take about a minute while Render and Neon wake. The app exposes `/ping` without database access for health checks. To avoid the 15-minute sleep, create a free UptimeRobot or cron-job.org monitor requesting `https://YOUR-APP.onrender.com/ping` every 10 minutes. This is an unofficial workaround; Render can still restart free instances, and the first real request after a quiet spell may wait while Neon wakes.

## Worked-example checklist

- [ ] Add Ana, Ben, Cleo, Dev.
- [ ] Enter Oct 5 bill `900.00` with Ana, Ben, Cleo ordered out: each is `300.00`.
- [ ] Enter Oct 6 bill `600.00` with Ben, Cleo, Dev ordered out: each is `200.00`.
- [ ] Enter Oct 7 bill `500.00` with Ana and Dev ordered out: each is `250.00`.
- [ ] Select October on Summary and verify spent values `55000`, `50000`, `50000`, `45000`.
- [ ] Record dues and verify that they increase the selected month's balances.
- [ ] Select the following month and verify that the previous month's ending balances carry forward.
- [ ] Try a bill with nobody ordering out and confirm the day is flagged.
- [ ] Verify `/ping` returns plain `ok` while unauthenticated.
