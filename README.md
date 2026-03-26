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
- UI: http://localhost:8080
- Customer submit page (friend mode): http://localhost:8080/customer

## Demo data

On startup, the app seeds sample cases.

Try tracking:
- Email: `john@example.com`
- Email: `priya@example.com`
- Email: `sara@example.com`

You can also use any ticket number displayed in the UI cards.

## API (quick)

- `POST /api/feedback`
- `POST /api/complaints`
- `GET  /api/cases/lookup?ticketNumber=...`
- `GET  /api/cases/my?email=...&limit=10`
- `PATCH /api/cases/{id}/status`
- `POST /api/cases/{id}/resolution`

