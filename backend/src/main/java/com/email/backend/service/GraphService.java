package com.email.backend.service;

import com.email.backend.dto.GraphEdge;
import com.email.backend.dto.GraphNode;
import com.email.backend.dto.GraphResponse;
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

    public GraphResponse getGraph(Long projectId, int depth, String focus) {
        if (codeFileRepository == null) {
            return new GraphResponse(List.of(), List.of());
        }

        List<CodeFile> files = codeFileRepository.findByProjectId(projectId.intValue());
        if (files == null || files.isEmpty()) {
            return new GraphResponse(List.of(), List.of());
        }

        List<GraphFile> parsedFiles = files.stream()
                .map(this::toGraphFile)
                .filter(Objects::nonNull)
                .filter(file -> focus == null || focus.isBlank() || matchesFocus(file, focus))
                .toList();

        return switch (depth) {
            case 2 -> buildClassGraph(parsedFiles);
            case 3 -> buildMethodGraph(parsedFiles);
            default -> buildPackageGraph(parsedFiles);
        };
    }

    private GraphFile toGraphFile(CodeFile file) {
        try {
            ParsedFile parsed = parserService.parseFile(file.getContent(), file.getLanguage());
            String packageName = parsed.getPackageName();
            if (packageName == null || packageName.isBlank()) {
                packageName = packageFromPath(file.getFilePath());
            }
            return new GraphFile(file, parsed, packageName);
        } catch (RuntimeException e) {
            log.debug("Skipping unsupported file {} in detailed graph", file.getFilePath());
            return null;
        }
    }

    private boolean matchesFocus(GraphFile file, String focus) {
        String needle = focus.toLowerCase(Locale.ROOT);
        return file.codeFile.getFilePath().toLowerCase(Locale.ROOT).contains(needle)
                || file.packageName.toLowerCase(Locale.ROOT).contains(needle)
                || file.parsed.getClasses().stream().anyMatch(name -> name.toLowerCase(Locale.ROOT).contains(needle))
                || file.parsed.getMethods().stream().anyMatch(name -> name.toLowerCase(Locale.ROOT).contains(needle));
    }

    private GraphResponse buildPackageGraph(List<GraphFile> files) {
        Map<String, GraphNode> nodes = new LinkedHashMap<>();
        List<GraphEdge> edges = new ArrayList<>();
        Set<String> edgeKeys = new HashSet<>();
        for (GraphFile file : files) {
            String source = "package:" + file.packageName;
            nodes.putIfAbsent(source, new GraphNode(source, shortName(file.packageName), file.packageName,
                    file.codeFile.getLanguage(), "package", 0));
            for (GraphFile dependency : matchingDependencies(file, files)) {
                String target = "package:" + dependency.packageName;
                nodes.putIfAbsent(target, new GraphNode(target, shortName(dependency.packageName), dependency.packageName,
                        dependency.codeFile.getLanguage(), "package", 0));
                if (!source.equals(target)) addEdge(edges, edgeKeys, nodes, source, target);
            }
        }
        return new GraphResponse(new ArrayList<>(nodes.values()), edges);
    }

    private GraphResponse buildClassGraph(List<GraphFile> files) {
        Map<String, GraphNode> nodes = new LinkedHashMap<>();
        List<GraphEdge> edges = new ArrayList<>();
        Set<String> edgeKeys = new HashSet<>();
        for (GraphFile file : files) {
            List<String> sourceClasses = classesFor(file);
            for (String className : sourceClasses) {
                String source = classId(file, className);
                nodes.putIfAbsent(source, new GraphNode(source, className, file.codeFile.getFilePath(),
                        file.codeFile.getLanguage(), "class", 0));
                for (GraphFile dependency : matchingDependencies(file, files)) {
                    for (String dependencyClass : classesFor(dependency)) {
                        String target = classId(dependency, dependencyClass);
                        nodes.putIfAbsent(target, new GraphNode(target, dependencyClass, dependency.codeFile.getFilePath(),
                                dependency.codeFile.getLanguage(), "class", 0));
                        addEdge(edges, edgeKeys, nodes, source, target);
                    }
                }
            }
        }
        return new GraphResponse(new ArrayList<>(nodes.values()), edges);
    }

    private GraphResponse buildMethodGraph(List<GraphFile> files) {
        Map<String, GraphNode> nodes = new LinkedHashMap<>();
        List<GraphEdge> edges = new ArrayList<>();
        Set<String> edgeKeys = new HashSet<>();
        for (GraphFile file : files) {
            String owner = classesFor(file).get(0);
            String classNodeId = classId(file, owner);
            nodes.putIfAbsent(classNodeId, new GraphNode(classNodeId, owner, file.codeFile.getFilePath(),
                    file.codeFile.getLanguage(), "class", 0));
            for (String method : file.parsed.getMethods()) {
                String methodId = "method:" + file.codeFile.getFilePath() + "#" + method;
                nodes.putIfAbsent(methodId, new GraphNode(methodId, method, file.codeFile.getFilePath(),
                        file.codeFile.getLanguage(), "method", 0));
                addEdge(edges, edgeKeys, nodes, classNodeId, methodId);
            }
        }
        return new GraphResponse(new ArrayList<>(nodes.values()), edges);
    }

    private List<GraphFile> matchingDependencies(GraphFile source, List<GraphFile> candidates) {
        return candidates.stream()
                .filter(candidate -> candidate != source)
                .filter(candidate -> source.parsed.getImports().stream().anyMatch(importName ->
                        importName.endsWith("." + removeExtension(extractFileName(candidate.codeFile.getFilePath())))
                                || importName.equals(candidate.packageName)
                                || candidate.codeFile.getFilePath().replace('\\', '/').contains(importName.replace('.', '/'))))
                .toList();
    }

    private List<String> classesFor(GraphFile file) {
        return file.parsed.getClasses().isEmpty()
                ? List.of(removeExtension(extractFileName(file.codeFile.getFilePath())))
                : file.parsed.getClasses().stream().distinct().toList();
    }

    private String classId(GraphFile file, String className) { return "class:" + file.codeFile.getFilePath() + "#" + className; }
    private String packageFromPath(String path) {
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash < 0 ? "root" : path.substring(0, slash).replace('/', '.').replace('\\', '.');
    }
    private String shortName(String value) {
        int dot = value.lastIndexOf('.');
        return dot < 0 ? value : value.substring(dot + 1);
    }
    private void addEdge(List<GraphEdge> edges, Set<String> edgeKeys, Map<String, GraphNode> nodes, String source, String target) {
        String key = source + "->" + target;
        if (edgeKeys.add(key)) {
            edges.add(new GraphEdge(source, target, 1));
            nodes.get(source).setWeight(nodes.get(source).getWeight() + 1);
            nodes.get(target).setWeight(nodes.get(target).getWeight() + 1);
        }
    }

    private record GraphFile(CodeFile codeFile, ParsedFile parsed, String packageName) { }

    private GraphResponse getPackageGraph(Long projectId) {
        List<Object[]> rawData = fileRepository.findPackageDependenciesRaw(projectId);
        Map<String, GraphNode> nodeMap = new LinkedHashMap<>();
        List<GraphEdge> edges = new ArrayList<>();
        for (Object[] row : rawData) {
            String sourcePkg = (String) row[0];
            String targetPkg = (String) row[1];
            Long weight = ((Number) row[2]).longValue();

            nodeMap.computeIfAbsent(sourcePkg, k -> new GraphNode(sourcePkg, extractPackageName(sourcePkg), sourcePkg, "java", "package", 0));
            nodeMap.computeIfAbsent(targetPkg, k ->
                    new GraphNode(targetPkg, extractPackageName(targetPkg), targetPkg,
                            "java", "package", 0));

            edges.add(new GraphEdge(sourcePkg, targetPkg, weight.intValue()));

            // Update weight
            nodeMap.get(sourcePkg).setWeight(
                    nodeMap.get(sourcePkg).getWeight() + weight.intValue()
            );

            nodeMap.get(targetPkg).setWeight(
                    nodeMap.get(targetPkg).getWeight() + weight.intValue()
            );

        }
        return new GraphResponse(new ArrayList<>(nodeMap.values()), edges);
    }


    private GraphResponse getClassGraph(Long projectId) {
        List<FileNode> allFiles = fileRepository.findByProjectId(projectId);

        Map<String, GraphNode> nodeMap = new LinkedHashMap<>();
        List<GraphEdge> edges = new ArrayList<>();

        for (FileNode file : allFiles){
            // Add node
            nodeMap.computeIfAbsent(file.getPath(), k ->
                    new GraphNode(file.getPath(),
                            file.getName(),
                            file.getPath(),
                            file.getLanguage(),
                            "class",
                            0
                    ));

            // Add edges for imports
            if (file.getImports() != null) {
                for (FileNode imported : file.getImports()) {
                    nodeMap.computeIfAbsent(imported.getPath(), k ->
                            new GraphNode(
                                    imported.getPath(),
                                    imported.getName(),
                                    imported.getPath(),
                                    imported.getLanguage(),
                                    "class",
                                    0
                            ));

                    edges.add(new GraphEdge(file.getPath(), imported.getPath(), 1));

                    // Update weights
                    nodeMap.get(file.getPath()).setWeight(
                            nodeMap.get(file.getPath()).getWeight() + 1
                    );

                    nodeMap.get(imported.getPath()).setWeight(
                            nodeMap.get(imported.getPath()).getWeight() + 1
                    );

                }
            }
        }

        return new GraphResponse(new ArrayList<>(nodeMap.values()), edges);
    }

    private GraphResponse getMethodGraph(Long projectId) {
        return getClassGraph(projectId);
    }

    private GraphResponse getSubgraph(Long projectId, String focus) {
        List<Object[]> results = fileRepository.findSubgraphAroundFile(projectId, focus);

        Map<String, GraphNode> nodeMap = new LinkedHashMap<>();
        List<GraphEdge> edges = new ArrayList<>();
        Set<String> addedEdges = new HashSet<>();

        for (Object[] row : results) {
            FileNode center = (FileNode) row[0];
            List<FileNode> imports = (List<FileNode>) row[1];
            List<FileNode> dependents = (List<FileNode>) row[2];

            // Add center node
            nodeMap.computeIfAbsent(center.getPath(), k ->
                    new GraphNode(
                            center.getPath(),
                            center.getName(),
                            center.getPath(),
                            center.getLanguage(),
                            "class",
                            0
                    ));

            // Add import edges
            if (imports != null) {
                for (FileNode imported : imports) {
                    if (imported == null) continue;

                    nodeMap.computeIfAbsent(imported.getPath(), k ->
                            new GraphNode(
                                    imported.getPath(),
                                    imported.getName(),
                                    imported.getPath(),
                                    imported.getLanguage(),
                                    "class",
                                    0
                            ));

                    String edgeKey = center.getPath() + "->" + imported.getPath();
                    if (addedEdges.add(edgeKey)) {
                        edges.add(new GraphEdge(center.getPath(), imported.getPath(), 1));
                    }
                }
            }

            // Add dependent edges
            if (dependents != null) {
                for (FileNode dependent : dependents) {
                    if (dependent == null) continue;

                    nodeMap.computeIfAbsent(dependent.getPath(), k ->
                            new GraphNode(
                                    dependent.getPath(),
                                    dependent.getName(),
                                    dependent.getPath(),
                                    dependent.getLanguage(),
                                    "class",
                                    0
                            ));

                    String edgeKey = dependent.getPath() + "->" + center.getPath();
                    if (addedEdges.add(edgeKey)) {
                        edges.add(new GraphEdge(dependent.getPath(), center.getPath(), 1));
                    }
                }
            }
        }

        return new GraphResponse(new ArrayList<>(nodeMap.values()), edges);
    }

    public GraphResponse expandNode(String nodeId) {
        FileNode file = fileRepository.findByPath(nodeId);
        if (file == null) {
            return new GraphResponse(new ArrayList<>(), new ArrayList<>());
        }

        Map<String, GraphNode> nodeMap = new LinkedHashMap<>();
        List<GraphEdge> edges = new ArrayList<>();

        // Add the center node
        nodeMap.put(file.getPath(), new GraphNode(
                file.getPath(),
                file.getName(),
                file.getPath(),
                file.getLanguage(),
                "class",
                0
        ));

        // Add its imports
        if (file.getImports() != null) {
            for (FileNode imported : file.getImports()) {
                nodeMap.put(imported.getPath(), new GraphNode(
                        imported.getPath(),
                        imported.getName(),
                        imported.getPath(),
                        imported.getLanguage(),
                        "class",
                        0
                ));
                edges.add(new GraphEdge(file.getPath(), imported.getPath(), 1));
            }
        }

        return new GraphResponse(new ArrayList<>(nodeMap.values()), edges);
    }

    private String extractPackageName(String fullPath) {
        if (fullPath == null || fullPath.isEmpty()) return "root";
        String[] parts = fullPath.split("[./]");
        return parts.length > 0 ? parts[parts.length - 1] : fullPath;
    }

}
