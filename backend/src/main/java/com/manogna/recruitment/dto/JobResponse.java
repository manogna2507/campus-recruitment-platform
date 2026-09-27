package com.manogna.recruitment.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String skills;
    private LocalDate applicationDeadline;
    private String status;
    private String companyName;
    private String postedByName;
    private LocalDateTime createdAt;

    public JobResponse(Long id, String title, String description, String location, String skills,
                        LocalDate applicationDeadline, String status, String companyName,
                        String postedByName, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.skills = skills;
        this.applicationDeadline = applicationDeadline;
        this.status = status;
        this.companyName = companyName;
        this.postedByName = postedByName;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getSkills() { return skills; }
    public LocalDate getApplicationDeadline() { return applicationDeadline; }
    public String getStatus() { return status; }
    public String getCompanyName() { return companyName; }
    public String getPostedByName() { return postedByName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
