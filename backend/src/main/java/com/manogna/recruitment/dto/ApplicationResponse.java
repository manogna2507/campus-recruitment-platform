package com.manogna.recruitment.dto;

import java.time.LocalDateTime;

public class ApplicationResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String status;
    private String resumeS3Key;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    public ApplicationResponse(Long id, Long jobId, String jobTitle, Long studentId, String studentName,
                                String studentEmail, String status, String resumeS3Key,
                                LocalDateTime appliedAt, LocalDateTime updatedAt) {
        this.id = id;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.status = status;
        this.resumeS3Key = resumeS3Key;
        this.appliedAt = appliedAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getJobId() { return jobId; }
    public String getJobTitle() { return jobTitle; }
    public Long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getStudentEmail() { return studentEmail; }
    public String getStatus() { return status; }
    public String getResumeS3Key() { return resumeS3Key; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
