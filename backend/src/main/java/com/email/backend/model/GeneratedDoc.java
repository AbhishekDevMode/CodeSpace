package com.email.backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "generated_docs")
public class GeneratedDoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private CodeFile codeFile;

    @Column(columnDefinition = "LONGTEXT")
    private String documentation;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public GeneratedDoc() {}

    public GeneratedDoc(Integer id, CodeFile codeFile, String documentation, LocalDateTime createdAt) {
        this.id = id;
        this.codeFile = codeFile;
        this.documentation = documentation;
        this.createdAt = createdAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public CodeFile getCodeFile() { return codeFile; }
    public void setCodeFile(CodeFile codeFile) { this.codeFile = codeFile; }

    public String getDocumentation() { return documentation; }
    public void setDocumentation(String documentation) { this.documentation = documentation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
