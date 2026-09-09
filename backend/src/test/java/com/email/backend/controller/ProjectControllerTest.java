package com.email.backend.controller;

import com.email.backend.dto.ProjectRequest;
import com.email.backend.dto.ProjectResponse;
import com.email.backend.security.CustomUserDetailsService;
import com.email.backend.security.JwtAuthenticationFilter;
import com.email.backend.security.JwtTokenProvider;
import com.email.backend.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testCreateProject() throws Exception {
        ProjectRequest request = new ProjectRequest();
        request.setRepoUrl("https://github.com/example/demo");
        request.setRepoName("demo");

        ProjectResponse response = new ProjectResponse(1, "https://github.com/example/demo", "demo", "processing", LocalDateTime.now());
        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.repoName").value("demo"))
                .andExpect(jsonPath("$.status").value("processing"));
    }

    @Test
    void testCreateProjectFromFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "project.zip", "application/zip", "zip content".getBytes());
        ProjectResponse response = new ProjectResponse(2, "uploaded://project.zip", "project", "processing", LocalDateTime.now());
        
        when(projectService.createProjectFromFile(any())).thenReturn(response);

        mockMvc.perform(multipart("/api/projects/upload").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.repoName").value("project"));
    }

    @Test
    void testGetUserProjects() throws Exception {
        ProjectResponse response = new ProjectResponse(1, "https://github.com/example/demo", "demo", "completed", LocalDateTime.now());
        when(projectService.getProjectsForCurrentUser()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].repoName").value("demo"));
    }

    @Test
    void testGetProjectById() throws Exception {
        ProjectResponse response = new ProjectResponse(1, "https://github.com/example/demo", "demo", "completed", LocalDateTime.now());
        when(projectService.getProjectById(eq(1))).thenReturn(response);

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("completed"));
    }

    @Test
    void testDeleteProject() throws Exception {
        doNothing().when(projectService).deleteProject(1);

        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Project deleted successfully"));
    }
}
