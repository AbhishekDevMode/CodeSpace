package com.email.backend.dto;

public class ProjectRequest {
    private String repoUrl;
    private String repoName;

    public ProjectRequest() {}

    public ProjectRequest(String repoUrl, String repoName) {
        this.repoUrl = repoUrl;
        this.repoName = repoName;
    }

    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }

    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }
}
