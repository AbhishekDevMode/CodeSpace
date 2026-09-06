package com.email.backend.repository;

import com.email.backend.model.CodeFile;
import com.email.backend.model.GeneratedDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GeneratedDocRepository extends JpaRepository<GeneratedDoc, Integer> {
    Optional<GeneratedDoc> findByCodeFile(CodeFile codeFile);
    Optional<GeneratedDoc> findByCodeFileId(Integer codeFileId);
}
