package com.email.backend.service;

import com.email.backend.model.CodeFile;
import com.email.backend.model.Project;
import com.email.backend.repository.CodeFileRepository;
import com.email.backend.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class RepoProcessingService {

    private static final Logger log = LoggerFactory.getLogger(RepoProcessingService.class);
    private static final Pattern GITHUB_PATTERN = Pattern.compile("https?://github\\.com/([^/]+)/([^/]+?)(?:\\.git)?(?:/.*)?$");
    private static final int MAX_FILES_TO_PROCESS = 150;
    private static final long MAX_FILE_SIZE_BYTES = 500 * 1024; // 500 KB

    private static final Set<String> IGNORED_DIRECTORIES = Set.of(
            ".git", "node_modules", "target", "build", "dist", ".idea", ".vscode",
            "vendor", "__pycache__", ".gradle", "bin", "out", ".next", ".nuxt",
            ".turbo", "coverage", ".settings", ".mvn"
    );

    private static final Map<String, String> EXTENSION_LANGUAGE_MAP = Map.ofEntries(
            Map.entry("java", "java"),
            Map.entry("py", "python"),
            Map.entry("js", "javascript"),
            Map.entry("jsx", "javascript"),
            Map.entry("ts", "typescript"),
            Map.entry("tsx", "typescript"),
            Map.entry("go", "go"),
            Map.entry("rs", "rust"),
            Map.entry("c", "c"),
            Map.entry("cpp", "cpp"),
            Map.entry("h", "c")
    );

    private final ProjectRepository projectRepository;
    private final CodeFileRepository codeFileRepository;
    private final GraphService graphService;

    public RepoProcessingService(
            ProjectRepository projectRepository,
            CodeFileRepository codeFileRepository,
            GraphService graphService
    ) {
        this.projectRepository = projectRepository;
        this.codeFileRepository = codeFileRepository;
        this.graphService = graphService;
    }

    /**
     * Validates repository URL format and verifies reachability.
     */
    public void validateRepoUrl(String repoUrl) {
        if (repoUrl == null || repoUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository URL cannot be empty.");
        }

        String trimmed = repoUrl.trim();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://") && !trimmed.startsWith("git@")) {
            throw new IllegalArgumentException("Invalid repository URL format. Please provide a valid HTTP(S) git URL.");
        }

        Matcher matcher = GITHUB_PATTERN.matcher(trimmed);
        if (matcher.find()) {
            String owner = matcher.group(1);
            String repo = matcher.group(2).replace(".git", "");
            checkGitHubRepoAccessible(owner, repo);
        }
    }

    private void checkGitHubRepoAccessible(String owner, String repo) {
        try {
            URI uri = URI.create(String.format("https://api.github.com/repos/%s/%s", owner, repo));
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "DevDocs-Analyzer");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            int code = conn.getResponseCode();

            if (code == 404) {
                throw new IllegalArgumentException("GitHub repository '" + owner + "/" + repo + "' not found or is private.");
            } else if (code >= 400 && code != 403) {
                throw new IllegalArgumentException("Unable to access GitHub repository (HTTP " + code + ").");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Pre-flight check for GitHub repository failed with warning: {}", e.getMessage());
        }
    }

    /**
     * Asynchronously downloads, extracts, parses ASTs, and builds dependency graph.
     */
    @Async
    public void processGitRepositoryAsync(Integer projectId, String repoUrl) {
        log.info("Starting asynchronous processing for project ID: {} from {}", projectId, repoUrl);
        Path tempDir = null;

        try {
            tempDir = Files.createTempDirectory("devdocs-repo-" + projectId + "-");

            // Strategy 1: Ultra-fast GitHub Zipball download
            boolean downloaded = false;
            Matcher matcher = GITHUB_PATTERN.matcher(repoUrl.trim());
            if (matcher.find()) {
                String owner = matcher.group(1);
                String repo = matcher.group(2).replace(".git", "");
                downloaded = tryDownloadGitHubZipball(owner, repo, tempDir);
            }

            // Strategy 2: Shallow Git clone fallback
            if (!downloaded) {
                log.info("Falling back to shallow git clone for {}", repoUrl);
                executeShallowGitClone(repoUrl.trim(), tempDir);
            }

            // Process extracted code files
            processCodeFilesInDirectory(projectId, tempDir);

        } catch (Exception e) {
            log.error("Failed to process repository for project {}: {}", projectId, e.getMessage(), e);
            markProjectFailed(projectId, "Repository processing error: " + e.getMessage());
        } finally {
            cleanupDirectory(tempDir);
        }
    }

    /**
     * Asynchronously processes an uploaded ZIP archive.
     */
    @Async
    public void processZipBytesAsync(Integer projectId, byte[] zipBytes) {
        log.info("Starting asynchronous ZIP archive processing for project ID: {}", projectId);
        Path tempDir = null;

        try {
            tempDir = Files.createTempDirectory("devdocs-zip-" + projectId + "-");
            unzip(new ByteArrayInputStream(zipBytes), tempDir);
            processCodeFilesInDirectory(projectId, tempDir);
        } catch (Exception e) {
            log.error("Failed to process uploaded ZIP for project {}: {}", projectId, e.getMessage(), e);
            markProjectFailed(projectId, "ZIP extraction error: " + e.getMessage());
        } finally {
            cleanupDirectory(tempDir);
        }
    }

    private boolean tryDownloadGitHubZipball(String owner, String repo, Path targetDir) {
        List<String> zipUrls = List.of(
                String.format("https://codeload.github.com/%s/%s/zip/refs/heads/main", owner, repo),
                String.format("https://codeload.github.com/%s/%s/zip/refs/heads/master", owner, repo),
                String.format("https://api.github.com/repos/%s/%s/zipball/HEAD", owner, repo)
        );

        for (String urlStr : zipUrls) {
            try {
                log.info("Attempting GitHub zip download from: {}", urlStr);
                URI uri = URI.create(urlStr);
                HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "DevDocs-Analyzer");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(20000);
                conn.setInstanceFollowRedirects(true);

                int code = conn.getResponseCode();
                if (code == 200) {
                    try (InputStream in = conn.getInputStream()) {
                        unzip(in, targetDir);
                    }
                    log.info("Successfully downloaded and extracted zipball from {}", urlStr);
                    return true;
                }
            } catch (Exception e) {
                log.warn("Failed to download zipball from {}: {}", urlStr, e.getMessage());
            }
        }
        return false;
    }

    private void executeShallowGitClone(String repoUrl, Path targetDir) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "git", "clone", "--depth", "1", "--single-branch", repoUrl, targetDir.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        boolean completed = process.waitFor(45, TimeUnit.SECONDS);
        if (!completed) {
            process.destroyForcibly();
            throw new RuntimeException("Git clone operation timed out after 45 seconds.");
        }

        if (process.exitValue() != 0) {
            throw new RuntimeException("Git clone failed with code " + process.exitValue() + ": " + output);
        }
    }

    private void unzip(InputStream inputStream, Path targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newPath = targetDir.resolve(entry.getName()).normalize();
                if (!newPath.startsWith(targetDir)) {
                    throw new IOException("Bad zip entry: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    if (newPath.getParent() != null) {
                        Files.createDirectories(newPath.getParent());
                    }
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }

    private void processCodeFilesInDirectory(Integer projectId, Path baseDir) throws IOException {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            log.warn("Project with ID {} no longer exists. Aborting processing.", projectId);
            return;
        }
        Project project = projectOpt.get();

        Path effectiveDir = baseDir;
        try (var stream = Files.list(baseDir)) {
            List<Path> entries = stream.toList();
            if (entries.size() == 1 && Files.isDirectory(entries.get(0))) {
                effectiveDir = entries.get(0);
            }
        }

        List<CodeFile> codeFiles = new ArrayList<>();
        final Path finalEffectiveDir = effectiveDir;

        Files.walkFileTree(effectiveDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String dirName = dir.getFileName() != null ? dir.getFileName().toString() : "";
                if (IGNORED_DIRECTORIES.contains(dirName) || dirName.startsWith(".")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (codeFiles.size() >= MAX_FILES_TO_PROCESS) {
                    return FileVisitResult.TERMINATE;
                }

                if (attrs.size() > MAX_FILE_SIZE_BYTES || attrs.size() == 0) {
                    return FileVisitResult.CONTINUE;
                }

                String filename = file.getFileName().toString();
                String ext = getFileExtension(filename);
                String language = EXTENSION_LANGUAGE_MAP.get(ext.toLowerCase());

                if (language != null) {
                    try {
                        String content = Files.readString(file);
                        String relativePath = finalEffectiveDir.relativize(file).toString().replace('\\', '/');

                        CodeFile codeFile = new CodeFile();
                        codeFile.setProject(project);
                        codeFile.setFilePath(relativePath);
                        codeFile.setContent(content);
                        codeFile.setLanguage(language);
                        codeFile.setAstData("{}");

                        codeFiles.add(codeFile);
                    } catch (Exception e) {
                        log.debug("Skipping unreadable file: {}", file);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });

        log.info("Found {} source code files for project ID {}", codeFiles.size(), projectId);

        if (!codeFiles.isEmpty()) {
            codeFileRepository.saveAll(codeFiles);

            try {
                graphService.buildGraphForProject(projectId, codeFiles);
                log.info("Graph built and saved to Neo4j successfully for project {}", projectId);
            } catch (Exception e) {
                log.warn("Neo4j graph generation encountered an issue for project {}: {}", projectId, e.getMessage());
            }
        }

        project.setStatus("completed");
        project.setErrorMessage(null);
        projectRepository.save(project);
        log.info("Project {} processing completed successfully.", projectId);
    }

    private void markProjectFailed(Integer projectId, String errorMessage) {
        try {
            projectRepository.findById(projectId).ifPresent(p -> {
                p.setStatus("failed");
                p.setErrorMessage(errorMessage != null && errorMessage.length() > 500
                        ? errorMessage.substring(0, 497) + "..."
                        : errorMessage);
                projectRepository.save(p);
            });
        } catch (Exception ex) {
            log.error("Failed to update project status to failed for ID {}: {}", projectId, ex.getMessage());
        }
    }

    private String getFileExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1) : "";
    }

    private void cleanupDirectory(Path dir) {
        if (dir != null && Files.exists(dir)) {
            try {
                Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.deleteIfExists(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path d, IOException exc) throws IOException {
                        Files.deleteIfExists(d);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (Exception e) {
                log.debug("Temporary directory cleanup warning for {}: {}", dir, e.getMessage());
            }
        }
    }
}
