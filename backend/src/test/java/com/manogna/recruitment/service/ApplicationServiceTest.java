package com.manogna.recruitment.service;

import com.manogna.recruitment.entity.*;
import com.manogna.recruitment.exception.InvalidStateTransitionException;
import com.manogna.recruitment.repository.ApplicationRepository;
import com.manogna.recruitment.repository.JobRepository;
import com.manogna.recruitment.repository.UserRepository;
import com.manogna.recruitment.statemachine.ApplicationStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private JobRepository jobRepository;
    @Mock private UserRepository userRepository;
    @Mock private StorageService storageService;
    @Mock private NotificationService notificationService;
    @Mock private ApplicationHistoryService historyService;

    private ApplicationService applicationService;

    private User recruiter;
    private User student;
    private Job job;
    private Application application;

    @BeforeEach
    void setUp() {
        // Real state machine on purpose: we want to test the service's use of it,
        // not a mocked stand-in that would let an illegal transition through silently.
        applicationService = new ApplicationService(
                applicationRepository, jobRepository, userRepository,
                new ApplicationStateMachine(), storageService, notificationService, historyService);

        recruiter = new User("Priya Recruiter", "recruiter@acme.com", "hash", Role.RECRUITER);
        recruiter.setId(1L);

        student = new User("Asha Student", "student@example.com", "hash", Role.STUDENT);
        student.setId(2L);

        job = new Job();
        job.setId(10L);
        job.setTitle("Programmer Analyst Intern");
        job.setStatus(JobStatus.OPEN);
        job.setPostedBy(recruiter);

        application = new Application();
        application.setId(100L);
        application.setJob(job);
        application.setStudent(student);
        application.setStatus(ApplicationStatus.APPLIED);
    }

    @Test
    void updateStatus_appliesAValidTransitionAndNotifiesTheStudent() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(userRepository.findByEmail("recruiter@acme.com")).thenReturn(Optional.of(recruiter));
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Application updated = applicationService.updateStatus(100L, ApplicationStatus.UNDER_REVIEW, "recruiter@acme.com");

        assertEquals(ApplicationStatus.UNDER_REVIEW, updated.getStatus());
        verify(notificationService).sendApplicationStatusChangeNotification(
                "student@example.com", "Programmer Analyst Intern", "UNDER_REVIEW");
        verify(historyService).recordTransition(100L, "APPLIED", "UNDER_REVIEW", "recruiter@acme.com");
    }

    @Test
    void updateStatus_rejectsAnIllegalTransition() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(userRepository.findByEmail("recruiter@acme.com")).thenReturn(Optional.of(recruiter));

        // APPLIED -> SELECTED skips required steps and must be rejected by the state machine
        assertThrows(InvalidStateTransitionException.class, () ->
                applicationService.updateStatus(100L, ApplicationStatus.SELECTED, "recruiter@acme.com"));

        verify(applicationRepository, never()).save(any());
        verify(notificationService, never()).sendApplicationStatusChangeNotification(any(), any(), any());
    }

    @Test
    void updateStatus_deniesARecruiterWhoDoesNotOwnTheJob() {
        User otherRecruiter = new User("Other Recruiter", "other@rival.com", "hash", Role.RECRUITER);
        otherRecruiter.setId(99L);

        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(userRepository.findByEmail("other@rival.com")).thenReturn(Optional.of(otherRecruiter));

        assertThrows(AccessDeniedException.class, () ->
                applicationService.updateStatus(100L, ApplicationStatus.UNDER_REVIEW, "other@rival.com"));
    }

    @Test
    void apply_rejectsASecondApplicationToTheSameJob() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        when(applicationRepository.findByJobIdAndStudentId(10L, 2L)).thenReturn(Optional.of(application));

        assertThrows(IllegalStateException.class, () ->
                applicationService.apply(10L, "student@example.com", null));
    }

    @Test
    void apply_rejectsApplyingToAClosedJob() {
        job.setStatus(JobStatus.CLOSED);
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));

        assertThrows(IllegalStateException.class, () ->
                applicationService.apply(10L, "student@example.com", null));
    }

    @Test
    void apply_succeedsAndDefaultsStatusToApplied() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        when(applicationRepository.findByJobIdAndStudentId(10L, 2L)).thenReturn(Optional.empty());
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Application saved = applicationService.apply(10L, "student@example.com", null);

        assertEquals(ApplicationStatus.APPLIED, saved.getStatus());
        assertEquals(student, saved.getStudent());
        assertEquals(job, saved.getJob());
    }
}
