# Learning Guide

You said you want to learn this project after it's built. Here's the order
to read it in, and the "why," not just the "what," for every piece. Don't
just skim the files — run the app, hit the endpoints in API_EXAMPLES.md, and
watch the console logs while you do it. Seeing the async log line print on a
different thread name, or the 409 error when you skip a status, will teach
you more than reading the code cold.

## Reading order

### 1. Start with the domain model (`entity/`)
Read `Role.java`, `JobStatus.java`, `ApplicationStatus.java`, then `User.java`,
`Company.java`, `Job.java`, `Application.java`. This is the shape of the
whole system. Notice: a `User` can be a `RECRUITER` (has a `Company`) or a
`STUDENT` (no company) or `ADMIN` — one table, one enum discriminator field,
rather than three separate tables. That's a common, deliberate simplification.

### 2. The state machine (`statemachine/ApplicationStateMachine.java`)
This is the single most "senior" piece of design in the project, and the
most resume-relevant. Read it alongside `ApplicationStateMachineTest.java`.

The key idea: **nowhere else in the codebase is anyone allowed to just set
`application.setStatus(x)` and save.** Every transition must pass through
`validateTransition()` first. This means the business rule ("you can't go
straight from APPLIED to SELECTED") lives in exactly one place, is trivially
unit-testable in isolation (no Spring context, no database — see how fast
those tests run), and can't be silently bypassed by a new controller someone
adds six months from now.

Try this exercise: add a new status, e.g. `INTERVIEW_SCHEDULED`, between
`SHORTLISTED` and `SELECTED`. You'll only need to touch the enum and the
static block in the state machine — nothing else in the codebase needs to
know the rule changed.

### 3. Security (`security/`, `config/SecurityConfig.java`)
Read in this order: `JwtUtil` (how a token is created and checked) →
`CustomUserDetailsService` (how a token's email gets turned into a real user
with a role) → `JwtAuthFilter` (how this runs on *every* request before your
controller ever sees it) → `SecurityConfig` (which URLs are public, and how
the filter gets wired in).

The comment in `JwtAuthFilter` about *not* using `@Component` is worth
understanding properly — it's a common Spring Boot gotcha: any bean that
implements `Filter` gets auto-registered as a servlet filter a second time,
so if you'd also wired it manually via `addFilterBefore`, it would run
twice per request. Constructing it as a plain object instead of a bean
sidesteps that entirely.

### 4. AWS integration (`config/AwsConfig.java`, `service/StorageService.java`, `service/ApplicationHistoryService.java`, `dynamo/ApplicationStatusHistoryItem.java`)
Two different AWS services, used for two different reasons — this
distinction is worth being able to explain out loud in an interview:

- **S3** stores the actual resume *file*. Files don't belong in a relational
  database; object storage is what it's designed for. We only store the S3
  *key* (a string) in Postgres/H2.
- **DynamoDB** stores the status-change history. This is a write-heavy,
  append-only, simple-access-pattern dataset ("give me every history row for
  application X, ordered by time") — exactly what DynamoDB's partition key +
  sort key model is built for, and arguably a worse fit for a relational
  table than a document/key-value store.

Notice both AWS calls in `ApplicationService` are wrapped in `try/catch` that
just logs a warning. That's a deliberate resilience choice for a student
project without a permanent AWS account: the *core* workflow (apply, review,
decide) still works even if AWS credentials aren't configured. In a real
production system you'd think harder about whether that's the right
trade-off (maybe resume upload failing *should* fail the request) — this is
a good thing to be able to discuss in an interview: "I made X resilient
on purpose, here's the trade-off I considered."

### 5. Scheduling and async (`scheduler/JobExpiryScheduler.java`, `service/NotificationService.java`, `config/AsyncConfig.java`, `config/SchedulingConfig.java`)
These map directly to two "preferred qualifications" in the JD: schedulers
and multi-threading. Run the app, change the cron to something like
`*/10 * * * * *` (every 10 seconds) temporarily, and watch the log line
appear. Then look at `NotificationService` and notice the log line prints a
*different thread name* (`notif-async-1`, etc.) than the HTTP request thread
— that's the actual proof of "multi-threaded," not just a buzzword.

### 6. Services, then controllers
Read `ApplicationService.java` last among the services — it's the one that
ties everything above together (state machine + S3 + DynamoDB + async, all
in `updateStatus()`). Then skim the controllers; by this point they should
read as "thin" — just translating HTTP in and out, with the real logic
living in the services.

### 7. Tests
Re-read `ApplicationServiceTest.java` once you've read the service itself.
Notice it uses a **real** `ApplicationStateMachine`, not a mock — this is
intentional: mocking it would mean the test can no longer catch a real bug
where an illegal transition slips through.

## Things to try extending (good practice, good interview stories)

- Add a `Notification` audit log to DynamoDB too (not just status history) —
  reuse the same `DynamoDbEnhancedClient` pattern.
- Add pagination to `GET /api/jobs` (Spring Data's `Pageable` is one line).
- Add a `POST /api/applications/{id}/resume-url` endpoint that returns the
  presigned S3 download URL (`StorageService.generatePresignedDownloadUrl`
  already exists but isn't wired to a controller yet — small, satisfying
  exercise).
- Swap `ddl-auto: update` for a real migration tool (Flyway or Liquibase) —
  a very common "next step" a recruiter would expect you to know about.
- Wire the existing `frontend/` clock-demo React app into a real UI that
  calls these APIs.
