package com.manogna.recruitment.repository;

import com.manogna.recruitment.entity.Job;
import com.manogna.recruitment.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByStatus(JobStatus status);
    List<Job> findByPostedById(Long recruiterId);
    List<Job> findByStatusAndApplicationDeadlineBefore(JobStatus status, LocalDate date);
}
