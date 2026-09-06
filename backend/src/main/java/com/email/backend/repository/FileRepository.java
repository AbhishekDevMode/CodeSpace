package com.email.backend.repository;

import com.email.backend.model.FileNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends Neo4jRepository<FileNode, String> {

    Optional<FileNode> findOneByPath(String path);

    // Returns all files that this file depends on (outgoing IMPORTS or CALLS)
    @Query("MATCH (f:File {path: $path})-[:IMPORTS|CALLS*1..2]->(dep:File) RETURN dep")
    List<FileNode> findDependencies(String path);

    // Returns all files that depend on this file (incoming IMPORTS or CALLS)
    @Query("MATCH (dep:File)-[:IMPORTS|CALLS*1..2]->(f:File {path: $path}) RETURN dep")
    List<FileNode> findDependents(String path);

    // Returns all files belonging to a specific project
    @Query("MATCH (f:File {projectId: $projectId}) RETURN f")
    List<FileNode> findByProjectId(Integer projectId);
}