package com.email.backend.repository;

import com.email.backend.model.FileNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileRepositoryTest {

    @Mock
    private FileRepository fileRepository;

    private FileNode sampleNode;
    private FileNode depNode;

    @BeforeEach
    void setUp() {
        sampleNode = new FileNode();
        sampleNode.setPath("src/main/java/UserService.java");
        sampleNode.setName("UserService.java");
        sampleNode.setLanguage("java");
        sampleNode.setProjectId(1);

        depNode = new FileNode();
        depNode.setPath("src/main/java/BaseService.java");
        depNode.setName("BaseService.java");
        depNode.setLanguage("java");
        depNode.setProjectId(1);
    }

    @Test
    void testFindOneByPath() {
        when(fileRepository.findOneByPath("src/main/java/UserService.java"))
                .thenReturn(Optional.of(sampleNode));

        Optional<FileNode> found = fileRepository.findOneByPath("src/main/java/UserService.java");
        assertTrue(found.isPresent());
        assertEquals("UserService.java", found.get().getName());
        verify(fileRepository, times(1)).findOneByPath("src/main/java/UserService.java");
    }

    @Test
    void testFindDependencies() {
        when(fileRepository.findDependencies("src/main/java/UserService.java"))
                .thenReturn(List.of(depNode));

        List<FileNode> dependencies = fileRepository.findDependencies("src/main/java/UserService.java");
        assertNotNull(dependencies);
        assertEquals(1, dependencies.size());
        assertEquals("BaseService.java", dependencies.get(0).getName());
        verify(fileRepository, times(1)).findDependencies("src/main/java/UserService.java");
    }

    @Test
    void testFindDependents() {
        when(fileRepository.findDependents("src/main/java/BaseService.java"))
                .thenReturn(List.of(sampleNode));

        List<FileNode> dependents = fileRepository.findDependents("src/main/java/BaseService.java");
        assertNotNull(dependents);
        assertEquals(1, dependents.size());
        assertEquals("UserService.java", dependents.get(0).getName());
        verify(fileRepository, times(1)).findDependents("src/main/java/BaseService.java");
    }

    @Test
    void testFindByProjectId() {
        when(fileRepository.findByProjectId(1))
                .thenReturn(List.of(sampleNode, depNode));

        List<FileNode> projectFiles = fileRepository.findByProjectId(1);
        assertNotNull(projectFiles);
        assertEquals(2, projectFiles.size());
        verify(fileRepository, times(1)).findByProjectId(1);
    }
}
