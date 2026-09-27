# API Walkthrough (curl)

Assumes the app is running locally on port 8080 with the seeded dev data
(see README). Swap in real IDs from the responses as you go.

## 1. Login as the recruiter

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"recruiter@acme.com","password":"password123"}'
```

Copy the `token` from the response into a shell variable:

```bash
RECRUITER_TOKEN="paste-token-here"
```

## 2. Post a job

```bash
curl -s -X POST http://localhost:8080/api/jobs \
  -H "Authorization: Bearer $RECRUITER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Backend Engineer Intern",
        "description": "Build and ship REST APIs alongside a mentor.",
        "location": "Bengaluru, India",
        "skills": "Java, Spring Boot, SQL",
        "applicationDeadline": "2026-12-31"
      }'
```

Note the `id` in the response - that's the `jobId` you'll apply to next.

## 3. Login as the student

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"student@example.com","password":"password123"}'

STUDENT_TOKEN="paste-token-here"
```

## 4. Apply to the job (with an optional resume file)

```bash
curl -s -X POST "http://localhost:8080/api/applications?jobId=1" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -F "resume=@/path/to/resume.pdf"
```

Or without a resume:

```bash
curl -s -X POST "http://localhost:8080/api/applications?jobId=1" \
  -H "Authorization: Bearer $STUDENT_TOKEN"
```

Note the application `id` in the response.

## 5. Recruiter views applicants for their job

```bash
curl -s http://localhost:8080/api/applications/job/1 \
  -H "Authorization: Bearer $RECRUITER_TOKEN"
```

## 6. Recruiter moves the application forward

```bash
curl -s -X PATCH http://localhost:8080/api/applications/1/status \
  -H "Authorization: Bearer $RECRUITER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"newStatus": "UNDER_REVIEW"}'
```

Try skipping a step to see the state machine reject it:

```bash
curl -s -X PATCH http://localhost:8080/api/applications/1/status \
  -H "Authorization: Bearer $RECRUITER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"newStatus": "SELECTED"}'
# -> 409 Conflict: "Cannot transition application from APPLIED to SELECTED..."
```

## 7. View the status-change audit trail (DynamoDB)

```bash
curl -s http://localhost:8080/api/applications/1/history \
  -H "Authorization: Bearer $RECRUITER_TOKEN"
```

(Empty if AWS/DynamoDB isn't configured locally - see README for setup.)

## 8. Student checks their own applications

```bash
curl -s http://localhost:8080/api/applications/mine \
  -H "Authorization: Bearer $STUDENT_TOKEN"
```
