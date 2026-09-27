# Campus Recruitment Platform

A backend for a campus recruitment system: recruiters post jobs, students apply
(with resume upload), and applications move through an explicit status
pipeline (`APPLIED → UNDER_REVIEW → SHORTLISTED → SELECTED/REJECTED`, plus
`WITHDRAWN`) with a full audit history.

Built specifically to demonstrate: **Java + Spring Boot**, JWT authentication,
a real relational data model, **AWS S3** (resume storage) and **AWS DynamoDB**
(status-change audit trail), a **scheduled background job**, **async
(multi-threaded) notifications**, JUnit + Mockito tests, Docker, and CI.

> If you're new to this codebase, read **[LEARNING_GUIDE.md](LEARNING_GUIDE.md)**
> first — it gives you a reading order and explains every design decision.
> **[RESUME_BULLETS.md](RESUME_BULLETS.md)** has ready-to-use resume bullets
> mapped to the exact files that back them up.

---

## Architecture

```
                     ┌─────────────┐
   HTTP requests --> │ Controllers │  (Auth / Job / Application / Health)
                     └──────┬──────┘
                            │
                     ┌──────▼──────┐
                     │  Services   │  business rules, ownership checks
                     └──┬───┬───┬──┘
                        │   │   │
        ┌───────────────┘   │   └────────────────┐
        │                   │                    │
 ┌──────▼──────┐   ┌────────▼────────┐   ┌────────▼────────┐
 │  PostgreSQL │   │   AWS S3        │   │  AWS DynamoDB   │
 │  (H2 in dev)│   │ (resume files)  │   │ (status history)│
 │  users/jobs/│   └─────────────────┘   └─────────────────┘
 │ applications│
 └─────────────┘

 Every Application status change is validated first by
 ApplicationStateMachine (in-memory, no DB dependency), then persisted,
 then audited to DynamoDB, then notified asynchronously on a worker thread.

 A daily @Scheduled job auto-closes any job posting past its deadline.
```

## Tech stack

| Concern              | Choice                                              |
|----------------------|------------------------------------------------------|
| Language / runtime   | Java 17                                              |
| Framework            | Spring Boot 3.2 (Web, Security, Data JPA, Validation)|
| Auth                  | JWT (jjwt), BCrypt password hashing                 |
| Database (dev)        | H2 in-memory                                        |
| Database (prod)       | PostgreSQL                                          |
| Object storage         | AWS S3 (resumes)                                  |
| Audit trail            | AWS DynamoDB (status-change history)              |
| API docs              | springdoc-openapi (Swagger UI)                       |
| Build / CI            | Maven, GitHub Actions                                |
| Container             | Docker (multi-stage build)                           |
| Tests                 | JUnit 5, Mockito                                     |

## Project layout

```
backend/
  src/main/java/com/manogna/recruitment/
    entity/         User, Company, Job, Application + status enums
    repository/      Spring Data JPA repositories
    dto/             request/response objects for the API
    security/        JwtUtil, JwtAuthFilter, CustomUserDetailsService
    config/          SecurityConfig, AwsConfig, AsyncConfig, SchedulingConfig, OpenApiConfig, DevDataSeeder
    statemachine/    ApplicationStateMachine (the transition rules)
    service/         AuthService, JobService, ApplicationService, StorageService (S3),
                     ApplicationHistoryService (DynamoDB), NotificationService (async)
    scheduler/       JobExpiryScheduler
    controller/      AuthController, JobController, ApplicationController, HealthController
    exception/       Custom exceptions + GlobalExceptionHandler
  src/test/java/...   JUnit + Mockito tests mirroring the structure above
  Dockerfile
docker-compose.yml    Postgres + backend, for a local prod-like run
.github/workflows/ci.yml
```

## Running it locally (no AWS account needed)

The app runs fully offline out of the box: H2 in-memory DB, and resume
upload / DynamoDB history are wrapped so they degrade gracefully (they log a
warning and continue) if AWS isn't configured. You only need AWS credentials
once you want those two pieces to actually work end-to-end.

```bash
cd backend
mvn spring-boot:run
```

On first boot, `DevDataSeeder` creates three demo accounts (all password
`password123`):

| Role      | Email                  |
|-----------|------------------------|
| Recruiter | recruiter@acme.com     |
| Student   | student@example.com    |
| Admin     | admin@example.com      |

Swagger UI: http://localhost:8080/swagger-ui.html
H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:campusdb`)

See **[API_EXAMPLES.md](API_EXAMPLES.md)** for a full curl walkthrough (login →
post a job → apply → move status → view history).

## Connecting real AWS (S3 + DynamoDB)

1. Create an S3 bucket and a DynamoDB table (partition key `applicationId`
   (String), sort key `timestamp` (String)).
2. Set credentials the standard AWS SDK way — either `~/.aws/credentials`,
   or environment variables:
   ```bash
   export AWS_ACCESS_KEY_ID=...
   export AWS_SECRET_ACCESS_KEY=...
   export AWS_REGION=ap-south-1
   export S3_BUCKET=your-bucket-name
   export DYNAMODB_TABLE=your-table-name
   ```
3. Run the app again — resume uploads and status-history writes now hit real
   AWS instead of silently no-op'ing.

Prefer not to use a real AWS account yet? Run [LocalStack](https://localstack.cloud)
and set `S3_ENDPOINT=http://localhost:4566` / `DYNAMODB_ENDPOINT=http://localhost:4566` —
the code already supports an endpoint override for exactly this.

## Running the tests

```bash
cd backend
mvn test
```

Covers: state machine transition rules, JWT generation/validation, the
application-status update flow (including ownership checks and illegal
transitions) via Mockito, and the scheduled expiry job.

## Docker

```bash
docker compose up --build
```

Runs Postgres + the backend together (prod profile). See
`docker-compose.yml` for the environment variables it wires up.

## A note on what's deliberately NOT here yet

The existing `frontend/` (Vite + React clock demo) from the original scaffold
is untouched — this build focused entirely on the backend, since that's what
closes the specific gaps against the target job description (see
RESUME_BULLETS.md). Wiring a real frontend to these APIs is a natural next
step once you're comfortable with the backend.
