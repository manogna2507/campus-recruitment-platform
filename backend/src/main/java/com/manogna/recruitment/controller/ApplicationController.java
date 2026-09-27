package com.manogna.recruitment.controller;

import com.manogna.recruitment.dto.ApplicationResponse;
import com.manogna.recruitment.dto.StatusUpdateRequest;
import com.manogna.recruitment.dynamo.ApplicationStatusHistoryItem;
import com.manogna.recruitment.entity.Application;
import com.manogna.recruitment.entity.ApplicationStatus;
import com.manogna.recruitment.service.ApplicationHistoryService;
import com.manogna.recruitment.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationHistoryService historyService;

    public ApplicationController(ApplicationService applicationService, ApplicationHistoryService historyService) {
        this.applicationService = applicationService;
        this.historyService = historyService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApplicationResponse> apply(@RequestParam Long jobId,
                                                       @RequestParam(required = false) MultipartFile resume,
                                                       Authentication authentication) {
        Application application = applicationService.apply(jobId, authentication.getName(), resume);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(application));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('STUDENT')")
    public List<ApplicationResponse> myApplications(Authentication authentication) {
        return applicationService.getApplicationsForStudent(authentication.getName())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('RECRUITER')")
    public List<ApplicationResponse> applicationsForJob(@PathVariable Long jobId, Authentication authentication) {
        return applicationService.getApplicationsForJob(jobId, authentication.getName())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public ApplicationResponse updateStatus(@PathVariable Long id,
                                             @Valid @RequestBody StatusUpdateRequest request,
                                             Authentication authentication) {
        ApplicationStatus newStatus;
        try {
            newStatus = ApplicationStatus.valueOf(request.getNewStatus().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("newStatus must be one of "
                    + java.util.Arrays.toString(ApplicationStatus.values()));
        }
        Application updated = applicationService.updateStatus(id, newStatus, authentication.getName());
        return toResponse(updated);
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public List<ApplicationStatusHistoryItem> history(@PathVariable Long id) {
        return historyService.getHistory(id);
    }

    private ApplicationResponse toResponse(Application application) {
        return new ApplicationResponse(
                application.getId(),
                application.getJob().getId(),
                application.getJob().getTitle(),
                application.getStudent().getId(),
                application.getStudent().getFullName(),
                application.getStudent().getEmail(),
                application.getStatus().name(),
                application.getResumeS3Key(),
                application.getAppliedAt(),
                application.getUpdatedAt()
        );
    }
}
