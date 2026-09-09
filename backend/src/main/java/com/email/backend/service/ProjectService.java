package com.email.backend.service;

import com.email.backend.dto.ProjectRequest;
import com.email.backend.dto.ProjectResponse;
import com.email.backend.model.Project;
import com.email.backend.model.User;
import com.email.backend.repository.ProjectRepository;
import com.email.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final RepoProcessingService repoProcessingService;

    public ProjectService(
            ProjectRepository projectRepository,
            UserRepository userRepository,
            RepoProcessingService repoProcessingService
    ) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.repoProcessingService = repoProcessingService;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthorized user access");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    public ProjectResponse createProject(ProjectRequest request) {
        User currentUser = getCurrentUser();

        // Validate repository URL and verify accessibility upfront
        repoProcessingService.validateRepoUrl(request.getRepoUrl());

        String name = request.getRepoName();
        if (name == null || name.trim().isEmpty()) {
            if (request.getRepoUrl() != null && !request.getRepoUrl().trim().isEmpty()) {
                String cleanUrl = request.getRepoUrl().trim().replaceAll("/+$", "");
                String[] parts = cleanUrl.split("/");
                name = parts[parts.length - 1].replace(".git", "");
            } else {
                name = "Untitled Project";
            }
        }

        Project project = new Project();
        project.setUser(currentUser);
        project.setRepoUrl(request.getRepoUrl().trim());
        project.setRepoName(name);
        project.setStatus("processing");

        Project saved = projectRepository.save(project);

        // Trigger background shallow clone / zipball download and AST parsing
        repoProcessingService.processGitRepositoryAsync(saved.getId(), saved.getRepoUrl());

        return mapToResponse(saved);
    }

    public ProjectResponse createProjectFromFile(MultipartFile file) {
        User currentUser = getCurrentUser();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file cannot be empty.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("Only .zip files are supported for project upload.");
        }

        String name = originalFilename.replace(".zip", "");

        Project project = new Project();
        project.setUser(currentUser);
        project.setRepoUrl("uploaded://" + originalFilename);
        project.setRepoName(name);
        project.setStatus("processing");

        Project saved = projectRepository.save(project);

        try {
            byte[] bytes = file.getBytes();
            repoProcessingService.processZipBytesAsync(saved.getId(), bytes);
        } catch (Exception e) {
            saved.setStatus("failed");
            saved.setErrorMessage("Failed to read uploaded file: " + e.getMessage());
            projectRepository.save(saved);
        }

        return mapToResponse(saved);
    }

    public List<ProjectResponse> getProjectsForCurrentUser() {
        User currentUser = getCurrentUser();
        return projectRepository.findByUser(currentUser)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(Integer id) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        if (!project.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: You do not own this project");
        }

        return mapToResponse(project);
    }

    public void deleteProject(Integer id) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        if (!project.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: You do not own this project");
        }

        projectRepository.delete(project);
    }

    private ProjectResponse mapToResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getRepoUrl(),
                project.getRepoName(),
                project.getStatus(),
                project.getErrorMessage(),
                project.getCreatedAt()
        );
    }
}
