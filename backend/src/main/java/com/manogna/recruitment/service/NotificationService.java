package com.manogna.recruitment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Simulates sending an email/SMS notification off the request thread, on the
 * bounded pool defined in AsyncConfig. In production this method body would
 * call SES/SNS instead of logging - the @Async contract (fire-and-forget,
 * runs on a worker thread) stays identical either way.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Async("taskExecutor")
    public void sendApplicationStatusChangeNotification(String studentEmail, String jobTitle, String newStatus) {
        try {
            // simulate network latency of an email/SMS provider call
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("[ASYNC on {}] Notified {} -> application for '{}' is now {}",
                Thread.currentThread().getName(), studentEmail, jobTitle, newStatus);
    }
}
