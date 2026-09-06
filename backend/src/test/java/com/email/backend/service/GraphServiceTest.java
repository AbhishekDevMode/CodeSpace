package com.email.backend.service;

import com.email.backend.model.CodeFile;
import com.email.backend.model.FileNode;
import com.email.backend.repository.FileRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class GraphServiceTest {

    @Test
    void testBuildGraphForProject() {
        FileRepository mockRepository = Mockito.mock(FileRepository.class);
        CodeParserService parserService = new CodeParserService();
        GraphService graphService = new GraphService(mockRepository, parserService);

        when(mockRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CodeFile fileA = new CodeFile();
        fileA.setFilePath("src/com/example/UserService.java");
        fileA.setLanguage("java");
        fileA.setContent("package com.example;\nimport com.example.UserRepository;\npublic class UserService extends BaseService {}");

        CodeFile fileB = new CodeFile();
        fileB.setFilePath("src/com/example/UserRepository.java");
        fileB.setLanguage("java");
        fileB.setContent("package com.example;\npublic class UserRepository {}");

        List<FileNode> nodes = graphService.buildGraphForProject(1, Arrays.asList(fileA, fileB));

        assertEquals(2, nodes.size());
        FileNode userNode = nodes.stream().filter(n -> n.getName().equals("UserService.java")).findFirst().orElse(null);
        assertNotNull(userNode);
        assertFalse(userNode.getImports().isEmpty());
    }
}
