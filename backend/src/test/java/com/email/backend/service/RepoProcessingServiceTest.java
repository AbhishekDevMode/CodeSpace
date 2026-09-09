package com.email.backend.service;

import com.email.backend.model.CodeFile;
import com.email.backend.model.Project;
import com.email.backend.repository.CodeFileRepository;
import com.email.backend.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepoProcessingServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CodeFileRepository codeFileRepository;

    @Mock
    private GraphService graphService;

    private RepoProcessingService repoProcessingService;

    @BeforeEach
    void setUp() {
        repoProcessingService = new RepoProcessingService(projectRepository, codeFileRepository, graphService);
    }

    @Test
    void testValidateRepoUrlValid() {
        assertDoesNotThrow(() -> repoProcessingService.validateRepoUrl("https://github.com/octocat/Hello-World"));
        assertDoesNotThrow(() -> repoProcessingService.validateRepoUrl("https://github.com/torvalds/linux.git"));
    }

    @Test
    void testValidateRepoUrlInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> repoProcessingService.validateRepoUrl(""));
        assertThrows(IllegalArgumentException.class, () -> repoProcessingService.validateRepoUrl("not-a-valid-url"));
        assertThrows(IllegalArgumentException.class, () -> repoProcessingService.validateRepoUrl(null));
    }

    @Test
    void testProcessZipBytesAsyncExtractsFilesAndCompletesProject() throws IOException {
        Project project = new Project();
        project.setId(10);
        project.setStatus("processing");

        when(projectRepository.findById(10)).thenReturn(Optional.of(project));

        // Prepare sample zip in memory
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("src/main/Hello.java");
            zos.putNextEntry(entry);
            zos.write("public class Hello { public static void main(String[] args) {} }".getBytes());
            zos.closeEntry();
        }

        repoProcessingService.processZipBytesAsync(10, baos.toByteArray());

        assertEquals("completed", project.getStatus());
        assertNull(project.getErrorMessage());
        verify(codeFileRepository, atLeastOnce()).saveAll(any());
        verify(graphService, atLeastOnce()).buildGraphForProject(eq(10), any());
        verify(projectRepository, atLeastOnce()).save(project);
    }
}
