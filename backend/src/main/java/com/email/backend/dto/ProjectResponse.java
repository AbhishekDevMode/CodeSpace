package com.email.backend.dto;

import java.time.LocalDateTime;

public class ProjectResponse {
    private Integer id;
    private String repoUrl;
    private String repoName;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;

    public ProjectResponse() {}

    public ProjectResponse(Integer id, String repoUrl, String repoName, String status, LocalDateTime createdAt) {
        this(id, repoUrl, repoName, status, null, createdAt);
    }

    public ProjectResponse(Integer id, String repoUrl, String repoName, String status, String errorMessage, LocalDateTime createdAt) {
        this.id = id;
        this.repoUrl = repoUrl;
        this.repoName = repoName;
        this.status = status;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }

    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
