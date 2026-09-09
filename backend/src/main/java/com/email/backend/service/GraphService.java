package com.email.backend.service;

import com.email.backend.model.CodeFile;
import com.email.backend.model.FileNode;
import com.email.backend.repository.CodeFileRepository;
import com.email.backend.repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GraphService {

    private static final Logger log = LoggerFactory.getLogger(GraphService.class);

    private final FileRepository fileRepository;
    private final CodeParserService parserService;
    private final CodeFileRepository codeFileRepository;

    @Autowired
    public GraphService(FileRepository fileRepository, CodeParserService parserService, CodeFileRepository codeFileRepository) {
        this.fileRepository = fileRepository;
        this.parserService = parserService;
        this.codeFileRepository = codeFileRepository;
    }

    public GraphService(FileRepository fileRepository, CodeParserService parserService) {
        this(fileRepository, parserService, null);
    }

    public List<FileNode> buildGraphForProject(Integer projectId, List<CodeFile> files) {
        List<FileNode> nodeList = buildGraphInMemory(projectId, files);

        // Save nodes to Neo4j graph database if available
        try {
            if (fileRepository != null) {
                Iterable<FileNode> saved = fileRepository.saveAll(nodeList);
                List<FileNode> result = new ArrayList<>();
                saved.forEach(result::add);
                return result;
            }
        } catch (Exception e) {
            log.warn("Failed to persist graph to Neo4j for project {}: {}. Continuing with in-memory graph.", projectId, e.getMessage());
        }
        return nodeList;
    }

    public List<FileNode> buildGraphInMemory(Integer projectId, List<CodeFile> files) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

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

        return new ArrayList<>(nodeMap.values());
    }

    public List<FileNode> getDependencies(String path) {
        try {
            if (fileRepository != null) {
                return fileRepository.findDependencies(path);
            }
        } catch (Exception e) {
            log.warn("Neo4j error getting dependencies for path {}: {}", path, e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<FileNode> getDependents(String path) {
        try {
            if (fileRepository != null) {
                return fileRepository.findDependents(path);
            }
        } catch (Exception e) {
            log.warn("Neo4j error getting dependents for path {}: {}", path, e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<FileNode> getProjectGraph(Integer projectId) {
        try {
            if (fileRepository != null) {
                List<FileNode> neoNodes = fileRepository.findByProjectId(projectId);
                if (neoNodes != null && !neoNodes.isEmpty()) {
                    return neoNodes;
                }
            }
        } catch (Exception e) {
            log.warn("Neo4j is not available or failed to fetch graph for project {}: {}. Falling back to relational store.", projectId, e.getMessage());
        }

        // Fallback: load CodeFiles from MySQL repository
        if (codeFileRepository != null) {
            List<CodeFile> codeFiles = codeFileRepository.findByProjectId(projectId);
            if (codeFiles != null && !codeFiles.isEmpty()) {
                log.info("Constructed graph from {} MySQL code files for project {}", codeFiles.size(), projectId);
                return buildGraphInMemory(projectId, codeFiles);
            }
        }

        return Collections.emptyList();
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
