package com.email.backend.service;

import com.email.backend.model.CodeFile;
import com.email.backend.model.FileNode;
import com.email.backend.repository.FileRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GraphService {

    private final FileRepository fileRepository;
    private final CodeParserService parserService;

    public GraphService(FileRepository fileRepository, CodeParserService parserService) {
        this.fileRepository = fileRepository;
        this.parserService = parserService;
    }

    public List<FileNode> buildGraphForProject(Integer projectId, List<CodeFile> files) {
        Map<String, FileNode> nodeMap = new HashMap<>();
        Map<String, ParsedFile> parsedMap = new HashMap<>();

        // 1. Create FileNode for each CodeFile
        for (CodeFile file : files) {
            String path = file.getFilePath();
            String name = extractFileName(path);

            FileNode node = new FileNode();
            node.setPath(path);
            node.setName(name);
            node.setLanguage(file.getLanguage());
            node.setContent(file.getContent());
            node.setProjectId(projectId);

            nodeMap.put(path, node);

            try {
                ParsedFile parsed = parserService.parseFile(file.getContent(), file.getLanguage());
                parsedMap.put(path, parsed);
            } catch (Exception e) {
                // Ignore unparseable or empty files
            }
        }

        // 2. Build IMPORTS and CALLS relationships
        for (CodeFile file : files) {
            String path = file.getFilePath();
            FileNode current = nodeMap.get(path);
            ParsedFile parsed = parsedMap.get(path);

            if (current != null && parsed != null) {
                Set<String> imports = parsed.getImports();
                Set<String> dependencies = parsed.getDependencies();

                for (Map.Entry<String, FileNode> entry : nodeMap.entrySet()) {
                    String targetPath = entry.getKey();
                    FileNode targetNode = entry.getValue();

                    if (!targetPath.equals(path)) {
                        String targetName = targetNode.getName();
                        String targetNameNoExt = removeExtension(targetName);

                        // Check import matching
                        for (String imp : imports) {
                            if (imp.contains(targetNameNoExt) || targetPath.contains(imp.replace('.', '/'))) {
                                current.getImports().add(targetNode);
                            }
                        }

                        // Check dependency matching (superclasses, calls, etc.)
                        for (String dep : dependencies) {
                            if (dep.equals(targetNameNoExt) || targetPath.contains(dep)) {
                                current.getCalls().add(targetNode);
                            }
                        }
                    }
                }
            }
        }

        // 3. Save nodes to Neo4j graph database
        List<FileNode> nodeList = new ArrayList<>(nodeMap.values());
        Iterable<FileNode> saved = fileRepository.saveAll(nodeList);
        List<FileNode> result = new ArrayList<>();
        saved.forEach(result::add);
        return result;
    }

    public List<FileNode> getDependencies(String path) {
        return fileRepository.findDependencies(path);
    }

    public List<FileNode> getDependents(String path) {
        return fileRepository.findDependents(path);
    }

    public List<FileNode> getProjectGraph(Integer projectId) {
        return fileRepository.findByProjectId(projectId);
    }

    private String extractFileName(String path) {
        if (path == null) return "unknown";
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    private String removeExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(0, dot) : filename;
    }
}
