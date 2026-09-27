package com.manogna.recruitment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Campus Recruitment Platform backend.
 *
 * This service exposes REST APIs for:
 *  - Authentication (JWT) for students, recruiters and admins
 *  - Job postings (created by recruiters)
 *  - Applications with an explicit status state machine
 *      APPLIED -> UNDER_REVIEW -> SHORTLISTED -> SELECTED / REJECTED (+ WITHDRAWN)
 *  - Resume storage on AWS S3
 *  - Application status history persisted to AWS DynamoDB
 *  - A scheduled job that auto-closes postings past their deadline
 *  - Async (multi-threaded) notifications on status change
 */
@SpringBootApplication
public class RecruitmentApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecruitmentApplication.class, args);
    }
}
