package com.email.backend.repository;

import com.email.backend.model.CodeFile;
import com.email.backend.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeFileRepository extends JpaRepository<CodeFile, Integer> {
    List<CodeFile> findByProject(Project project);
    List<CodeFile> findByProjectId(Integer projectId);
}
