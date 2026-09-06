package com.email.backend.repository;

import com.email.backend.model.FileNode;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface FileRepository extends Neo4jRepository<FileNode, String> {
    // Query derivation from method name
    Optional<FileNode> findOneByPath(String path);

    @Query("MATCH (f:File {path: $path})-[:IMPORTS*1..2]->(imports) RETURN imports")
    List<FileNode> findDependencies(String path);
}