package com.manogna.recruitment.service;

import com.manogna.recruitment.dto.JobRequest;
import com.manogna.recruitment.entity.Job;
import com.manogna.recruitment.entity.JobStatus;
import com.manogna.recruitment.entity.Role;
import com.manogna.recruitment.entity.User;
import com.manogna.recruitment.exception.ResourceNotFoundException;
import com.manogna.recruitment.repository.JobRepository;
import com.manogna.recruitment.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public JobService(JobRepository jobRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Job createJob(JobRequest req, String recruiterEmail) {
        User recruiter = userRepository.findByEmail(recruiterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (recruiter.getRole() != Role.RECRUITER) {
            throw new AccessDeniedException("Only recruiters can post jobs");
        }
        if (recruiter.getCompany() == null) {
            throw new IllegalStateException("Recruiter account has no associated company");
        }

        Job job = new Job();
        job.setTitle(req.getTitle());
        job.setDescription(req.getDescription());
        job.setLocation(req.getLocation());
        job.setSkills(req.getSkills());
        job.setApplicationDeadline(req.getApplicationDeadline());
        job.setStatus(JobStatus.OPEN);
        job.setCompany(recruiter.getCompany());
        job.setPostedBy(recruiter);

        return jobRepository.save(job);
    }

    public List<Job> listOpenJobs() {
        return jobRepository.findByStatus(JobStatus.OPEN);
    }

    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id " + id));
    }

    public List<Job> listJobsPostedBy(String recruiterEmail) {
        User recruiter = userRepository.findByEmail(recruiterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return jobRepository.findByPostedById(recruiter.getId());
    }

    @Transactional
    public Job closeJob(Long jobId, String recruiterEmail) {
        Job job = getJobById(jobId);
        User recruiter = userRepository.findByEmail(recruiterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!job.getPostedBy().getId().equals(recruiter.getId())) {
            throw new AccessDeniedException("You do not own this job posting");
        }
        job.setStatus(JobStatus.CLOSED);
        return jobRepository.save(job);
    }
}
