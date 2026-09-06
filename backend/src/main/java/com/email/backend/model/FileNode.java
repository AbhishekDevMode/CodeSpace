package com.email.backend.model;

import jakarta.persistence.Id;

import java.util.Set;

@Node("File")
public class FileNode {
    @Id
    private String path;
    private String name;
    private String language;

    @Relationship(type = "IMPORTS", direction = Direction.OUTGOING)
    private Set<FileNode> imports;

    @Relationship(type = "CALLS", direction = Direction.OUTGOING)
    private Set<FileNode> calls;
}