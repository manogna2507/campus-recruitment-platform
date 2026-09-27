package com.manogna.recruitment.service;

import com.manogna.recruitment.entity.*;
import com.manogna.recruitment.exception.ResourceNotFoundException;
import com.manogna.recruitment.repository.ApplicationRepository;
import com.manogna.recruitment.repository.JobRepository;
import com.manogna.recruitment.repository.UserRepository;
import com.manogna.recruitment.statemachine.ApplicationStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ApplicationStateMachine stateMachine;
    private final StorageService storageService;
    private final NotificationService notificationService;
    private final ApplicationHistoryService historyService;

    public ApplicationService(ApplicationRepository applicationRepository,
                               JobRepository jobRepository,
                               UserRepository userRepository,
                               ApplicationStateMachine stateMachine,
                               StorageService storageService,
                               NotificationService notificationService,
                               ApplicationHistoryService historyService) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.stateMachine = stateMachine;
        this.storageService = storageService;
        this.notificationService = notificationService;
        this.historyService = historyService;
    }

    @Transactional
    public Application apply(Long jobId, String studentEmail, MultipartFile resume) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id " + jobId));

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new IllegalStateException("This job posting is closed");
        }
        if (applicationRepository.findByJobIdAndStudentId(jobId, student.getId()).isPresent()) {
            throw new IllegalStateException("You have already applied to this job");
        }

        Application application = new Application();
        application.setJob(job);
        application.setStudent(student);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());

        if (resume != null && !resume.isEmpty()) {
            // Resume upload is best-effort: don't fail the whole application if
            // AWS isn't configured locally (e.g. no credentials during dev/testing).
            try {
                String key = storageService.uploadResume(resume, student.getId());
                application.setResumeS3Key(key);
            } catch (Exception e) {
                log.warn("Could not upload resume to S3 (is AWS configured?): {}", e.getMessage());
            }
        }

        return applicationRepository.save(application);
    }

    @Transactional
    public Application updateStatus(Long applicationId, ApplicationStatus newStatus, String actingUserEmail) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id " + applicationId));

        User actingUser = userRepository.findByEmail(actingUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (actingUser.getRole() == Role.RECRUITER
                && !application.getJob().getPostedBy().getId().equals(actingUser.getId())) {
            throw new AccessDeniedException("You do not have permission to modify this application");
        }

        ApplicationStatus previous = application.getStatus();
        stateMachine.validateTransition(previous, newStatus); // throws InvalidStateTransitionException if illegal

        application.setStatus(newStatus);
        application.setUpdatedAt(LocalDateTime.now());
        Application saved = applicationRepository.save(application);

        // Audit trail write is best-effort too, for the same local-dev reason as above.
        try {
            historyService.recordTransition(applicationId, previous.name(), newStatus.name(), actingUserEmail);
        } catch (Exception e) {
            log.warn("Could not record status history in DynamoDB (is AWS configured?): {}", e.getMessage());
        }

        notificationService.sendApplicationStatusChangeNotification(
                application.getStudent().getEmail(), application.getJob().getTitle(), newStatus.name());

        return saved;
    }

    public List<Application> getApplicationsForStudent(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return applicationRepository.findByStudentId(student.getId());
    }

    public List<Application> getApplicationsForJob(Long jobId, String recruiterEmail) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id " + jobId));
        User recruiter = userRepository.findByEmail(recruiterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!job.getPostedBy().getId().equals(recruiter.getId())) {
            throw new AccessDeniedException("You do not have permission to view applicants for this job");
        }
        return applicationRepository.findByJobId(jobId);
    }
}
