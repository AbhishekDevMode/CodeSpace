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

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
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

        String name = request.getRepoName();
        if (name == null || name.trim().isEmpty()) {
            if (request.getRepoUrl() != null && !request.getRepoUrl().trim().isEmpty()) {
                String[] parts = request.getRepoUrl().split("/");
                name = parts[parts.length - 1].replace(".git", "");
            } else {
                name = "Untitled Project";
            }
        }

        Project project = new Project();
        project.setUser(currentUser);
        project.setRepoUrl(request.getRepoUrl());
        project.setRepoName(name);
        project.setStatus("processing");

        Project saved = projectRepository.save(project);
        return mapToResponse(saved);
    }

    public ProjectResponse createProjectFromFile(MultipartFile file) {
        User currentUser = getCurrentUser();
        String originalFilename = file.getOriginalFilename();
        String name = (originalFilename != null && !originalFilename.isEmpty()) 
                ? originalFilename.replace(".zip", "") 
                : "Uploaded Project";

        Project project = new Project();
        project.setUser(currentUser);
        project.setRepoUrl("uploaded://" + originalFilename);
        project.setRepoName(name);
        project.setStatus("processing");

        Project saved = projectRepository.save(project);
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
                project.getCreatedAt()
        );
    }
}
