package com.email.backend.controller;

import com.email.backend.model.FileNode;
import com.email.backend.security.CustomUserDetailsService;
import com.email.backend.security.JwtAuthenticationFilter;
import com.email.backend.security.JwtTokenProvider;
import com.email.backend.service.GraphService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GraphController.class)
@AutoConfigureMockMvc(addFilters = false)
class GraphControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GraphService graphService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testGetProjectGraph() throws Exception {
        FileNode node = new FileNode();
        node.setPath("src/main/App.java");
        node.setName("App.java");
        node.setProjectId(1);

        when(graphService.getProjectGraph(eq(1))).thenReturn(List.of(node));

        mockMvc.perform(get("/api/graph/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("App.java"))
                .andExpect(jsonPath("$[0].path").value("src/main/App.java"));
    }

    @Test
    void testGetDependencies() throws Exception {
        FileNode dep = new FileNode();
        dep.setPath("src/main/Utils.java");
        dep.setName("Utils.java");

        when(graphService.getDependencies("src/main/App.java")).thenReturn(List.of(dep));

        mockMvc.perform(get("/api/graph/dependencies")
                        .param("path", "src/main/App.java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Utils.java"));
    }

    @Test
    void testGetDependents() throws Exception {
        FileNode dependent = new FileNode();
        dependent.setPath("src/main/Controller.java");
        dependent.setName("Controller.java");

        when(graphService.getDependents("src/main/Utils.java")).thenReturn(List.of(dependent));

        mockMvc.perform(get("/api/graph/dependents")
                        .param("path", "src/main/Utils.java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Controller.java"));
    }
}
