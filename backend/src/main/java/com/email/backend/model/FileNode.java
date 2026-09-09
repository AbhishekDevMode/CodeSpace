package com.email.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Node("File")
public class FileNode {

    @Id
    private String path;
    private String name;
    private String language;
    private String content;
    private Integer projectId;

    @Relationship(type = "IMPORTS", direction = Relationship.Direction.OUTGOING)
    @JsonIgnoreProperties({"imports", "calls", "content"})
    private Set<FileNode> imports = new HashSet<>();

    @Relationship(type = "CALLS", direction = Relationship.Direction.OUTGOING)
    @JsonIgnoreProperties({"imports", "calls", "content"})
    private Set<FileNode> calls = new HashSet<>();

    public FileNode() {}

    public FileNode(String path, String name, String language, String content, Integer projectId) {
        this.path = path;
        this.name = name;
        this.language = language;
        this.content = content;
        this.projectId = projectId;
    }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    public Set<FileNode> getImports() { return imports; }
    public void setImports(Set<FileNode> imports) { this.imports = imports; }

    public Set<FileNode> getCalls() { return calls; }
    public void setCalls(Set<FileNode> calls) { this.calls = calls; }
}