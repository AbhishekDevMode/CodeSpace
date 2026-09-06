package com.email.backend.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Set;

@Node("File")
public class FileNode {

    @Id
    private String path;
    private String name;
    private String language;

    @Relationship(type = "IMPORTS", direction = Relationship.Direction.OUTGOING)
    private Set<FileNode> imports;

    @Relationship(type = "CALLS", direction = Relationship.Direction.OUTGOING)
    private Set<FileNode> calls;

    public FileNode() {}

    public FileNode(String path, String name, String language, Set<FileNode> imports, Set<FileNode> calls) {
        this.path = path;
        this.name = name;
        this.language = language;
        this.imports = imports;
        this.calls = calls;
    }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Set<FileNode> getImports() { return imports; }
    public void setImports(Set<FileNode> imports) { this.imports = imports; }

    public Set<FileNode> getCalls() { return calls; }
    public void setCalls(Set<FileNode> calls) { this.calls = calls; }
}