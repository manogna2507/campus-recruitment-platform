package com.manogna.recruitment.scheduler;

import com.manogna.recruitment.entity.Job;
import com.manogna.recruitment.entity.JobStatus;
import com.manogna.recruitment.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobExpirySchedulerTest {

    @Mock
    private JobRepository jobRepository;

    private JobExpiryScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new JobExpiryScheduler(jobRepository);
    }

    @Test
    void closesAllJobsPastTheirDeadline() {
        Job expiredJob1 = new Job();
        expiredJob1.setId(1L);
        expiredJob1.setStatus(JobStatus.OPEN);
        expiredJob1.setApplicationDeadline(LocalDate.now().minusDays(1));

        Job expiredJob2 = new Job();
        expiredJob2.setId(2L);
        expiredJob2.setStatus(JobStatus.OPEN);
        expiredJob2.setApplicationDeadline(LocalDate.now().minusDays(5));

        when(jobRepository.findByStatusAndApplicationDeadlineBefore(eq(JobStatus.OPEN), any(LocalDate.class)))
                .thenReturn(List.of(expiredJob1, expiredJob2));

        scheduler.closeExpiredJobs();

        assertEquals(JobStatus.CLOSED, expiredJob1.getStatus());
        assertEquals(JobStatus.CLOSED, expiredJob2.getStatus());
        verify(jobRepository).saveAll(List.of(expiredJob1, expiredJob2));
    }

    @Test
    void doesNothingWhenNoJobsHaveExpired() {
        when(jobRepository.findByStatusAndApplicationDeadlineBefore(eq(JobStatus.OPEN), any(LocalDate.class)))
                .thenReturn(List.of());

        scheduler.closeExpiredJobs();

        verify(jobRepository, never()).saveAll(any());
    }
}
