# Resume Bullets

Use these once you've actually run the project and can speak to how it
works (see LEARNING_GUIDE.md) — don't paste them in cold. Each line below is
paired with the file(s) that back it up, so you can point to exact code if
asked in an interview.

## Suggested project entry

**Campus Recruitment Platform | github.com/manogna2507/[your-repo-name]**
*Java, Spring Boot, Spring Security, JWT, PostgreSQL, AWS (S3, DynamoDB, EC2), Docker, JUnit, Mockito*

- Designed and built a Java/Spring Boot recruitment platform with JWT-based
  role authentication (student/recruiter/admin) and an explicit finite-state
  machine governing application status transitions (Applied → Under Review →
  Shortlisted → Selected/Rejected), preventing invalid state changes at the
  service layer.
  *(files: `statemachine/ApplicationStateMachine.java`, `security/JwtUtil.java`, `security/JwtAuthFilter.java`)*

- Integrated AWS S3 for resume storage with presigned URL generation for
  secure, time-limited recruiter access, and AWS DynamoDB for an append-only
  application-status audit trail, choosing DynamoDB's partition/sort-key
  model to match its write-heavy, single-access-pattern usage.
  *(files: `service/StorageService.java`, `service/ApplicationHistoryService.java`, `dynamo/ApplicationStatusHistoryItem.java`)*

- Implemented a scheduled background job to auto-close expired job postings
  and an async, thread-pool-backed notification service to alert students of
  status changes without blocking the request thread.
  *(files: `scheduler/JobExpiryScheduler.java`, `service/NotificationService.java`, `config/AsyncConfig.java`)*

- Wrote JUnit/Mockito unit tests covering the state machine's transition
  rules, JWT generation/validation, and the application-update workflow
  (including ownership authorization and illegal-transition rejection);
  containerized the service with Docker and set up GitHub Actions CI to run
  the suite on every push.
  *(files: `src/test/**`, `backend/Dockerfile`, `.github/workflows/ci.yml`)*

## How this maps to the JD gaps

| JD requirement (stated or preferred)          | Where it's now demonstrated                          |
|------------------------------------------------|--------------------------------------------------------|
| "Java (preferred)"                             | Entire backend is Java/Spring Boot, not Python         |
| "AWS engineering tools... S3, DynamoDB, EC2"   | S3 for resumes, DynamoDB for history, Docker image deployable to EC2 |
| "schedulers, workflows, state machines"        | `ApplicationStateMachine` + `JobExpiryScheduler`       |
| "multi-threading"                              | `@Async` notifications on a dedicated thread pool      |
| "best practices: design, testing, ... deployment" | Layered architecture, JUnit/Mockito, Docker, CI      |

## A note on honesty

Only claim what you actually understand and can defend live. If an
interviewer asks "why DynamoDB and not just another Postgres table for
history?" or "walk me through what happens when a recruiter tries an
invalid status change," you should be able to answer from real
understanding, not memorized bullet text — that's exactly what
LEARNING_GUIDE.md is for.
