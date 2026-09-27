package com.manogna.recruitment.scheduler;

import com.manogna.recruitment.entity.Job;
import com.manogna.recruitment.entity.JobStatus;
import com.manogna.recruitment.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Runs daily (configurable via scheduler.job-expiry.cron) and closes any OPEN
 * job whose applicationDeadline has passed, so students never see or apply to
 * a stale posting and recruiters don't have to close postings by hand.
 */
@Component
public class JobExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(JobExpiryScheduler.class);

    private final JobRepository jobRepository;

    public JobExpiryScheduler(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Scheduled(cron = "${scheduler.job-expiry.cron:0 0 1 * * *}")
    @Transactional
    public void closeExpiredJobs() {
        List<Job> expired = jobRepository.findByStatusAndApplicationDeadlineBefore(JobStatus.OPEN, LocalDate.now());
        if (expired.isEmpty()) {
            return;
        }
        expired.forEach(job -> job.setStatus(JobStatus.CLOSED));
        jobRepository.saveAll(expired);
        log.info("JobExpiryScheduler: auto-closed {} expired job posting(s)", expired.size());
    }
}
