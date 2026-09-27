package com.manogna.recruitment.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class JobRequest {

    @NotBlank(message = "title is required")
    private String title;

    private String description;

    private String location;

    private String skills;

    @NotNull(message = "applicationDeadline is required (yyyy-MM-dd)")
    @FutureOrPresent(message = "applicationDeadline cannot be in the past")
    private LocalDate applicationDeadline;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public LocalDate getApplicationDeadline() { return applicationDeadline; }
    public void setApplicationDeadline(LocalDate applicationDeadline) { this.applicationDeadline = applicationDeadline; }
}
