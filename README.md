# Airline Customer Feedback & Complaint Resolution System

This is a hackathon demo full-stack app:
- Java Spring Boot (REST + H2)
- “Awesome” single-page UI (served as static files)
- Automatic triage (category + sentiment + priority)
- Status timeline + admin status/resolution updates
- CSV export for case lists

## Run

```bash
cd /Users/y.prabhukiran/Desktop/FSAD_HACKATHON
mvn spring-boot:run
```

Open:
- Dashboard: https://bit.ly/Casesolver
- Customer Portal: https://bit.ly/skyResolver

## Demo data

On startup, the app seeds sample cases.

Try tracking:
- Email: `john@example.com`
- Email: `priya@example.com`
- Email: `sara@example.com`

You can also use any ticket number displayed in the UI cards.
