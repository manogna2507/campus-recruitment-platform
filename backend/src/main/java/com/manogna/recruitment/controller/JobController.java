package com.manogna.recruitment.controller;

import com.manogna.recruitment.dto.JobRequest;
import com.manogna.recruitment.dto.JobResponse;
import com.manogna.recruitment.entity.Job;
import com.manogna.recruitment.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public List<JobResponse> listOpenJobs() {
        return jobService.listOpenJobs().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public JobResponse getJob(@PathVariable Long id) {
        return toResponse(jobService.getJobById(id));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('RECRUITER')")
    public List<JobResponse> myJobs(Authentication authentication) {
        return jobService.listJobsPostedBy(authentication.getName())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobRequest request, Authentication authentication) {
        Job job = jobService.createJob(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(job));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('RECRUITER')")
    public JobResponse closeJob(@PathVariable Long id, Authentication authentication) {
        return toResponse(jobService.closeJob(id, authentication.getName()));
    }

    private JobResponse toResponse(Job job) {
        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getLocation(),
                job.getSkills(),
                job.getApplicationDeadline(),
                job.getStatus().name(),
                job.getCompany().getName(),
                job.getPostedBy().getFullName(),
                job.getCreatedAt()
        );
    }
}
