package com.email.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "code_files")
public class CodeFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(length = 50)
    private String language;

    @Column(name = "ast_data", columnDefinition = "JSON")
    private String astData;

    public CodeFile() {}

    public CodeFile(Integer id, Project project, String filePath, String content, String language, String astData) {
        this.id = id;
        this.project = project;
        this.filePath = filePath;
        this.content = content;
        this.language = language;
        this.astData = astData;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getAstData() { return astData; }
    public void setAstData(String astData) { this.astData = astData; }
}
