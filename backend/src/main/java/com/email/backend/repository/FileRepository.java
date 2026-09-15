package com.email.backend.repository;

import com.email.backend.model.FileNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
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

    List<FileNode> findByProjectId(Long projectId);

    // Get a single file by path
    FileNode findByPath(String path);

    // DEPTH 1: Package-level aggregated dependencies
    // Groups files by their package and counts imports between packages
    @Query("""
                MATCH (f:File {projectId: $projectId})
                WITH f, 
                     CASE 
                         WHEN f.path CONTAINS '/' THEN substring(f.path, 0, size(f.path) - size(split(f.path, '/')[-1]) - 1)
                         ELSE 'root'
                     END AS package
                MATCH (f)-[:IMPORTS]->(dep:File)
                WITH package AS sourcePkg, 
                     CASE 
                         WHEN dep.path CONTAINS '/' THEN substring(dep.path, 0, size(dep.path) - size(split(dep.path, '/')[-1]) - 1)
                         ELSE 'root'
                     END AS targetPkg
                WHERE sourcePkg <> targetPkg
                RETURN sourcePkg, targetPkg, COUNT(*) AS weight
            """)
    List<Object[]> findPackageDependenciesRaw(@Param("projectId") Long projectId);

    // DEPTH 2: Class-level dependencies for a specific package
    @Query("""
                MATCH (f:File {projectId: $projectId})
                WHERE f.path STARTS WITH $package
                OPTIONAL MATCH (f)-[r:IMPORTS]->(dep:File)
                RETURN f, collect(dep) as deps
            """)
    List<FileNode> findClassDependenciesByPackage(
            @Param("projectId") Long projectId,
            @Param("package") String packageName
    );

    // SEARCH: Get subgraph around a specific file (2 degrees deep)
    @Query("""
                MATCH (f:File {projectId: $projectId})
                WHERE f.path CONTAINS $searchTerm OR f.name CONTAINS $searchTerm
                OPTIONAL MATCH (f)-[:IMPORTS*1..2]->(dep:File)
                OPTIONAL MATCH (dependent:File)-[:IMPORTS*1..2]->(f)
                RETURN DISTINCT f, collect(DISTINCT dep) as imports, collect(DISTINCT dependent) as dependents
            """)
    List<Object[]> findSubgraphAroundFile(
            @Param("projectId") Long projectId,
            @Param("searchTerm") String searchTerm
    );

    // Get all imports for a specific file
    @Query("MATCH (f:File {path: $path})-[:IMPORTS]->(dep) RETURN dep")
    List<FileNode> findImportsByPath(@Param("path") String path);

    // Get all files that import this file
    @Query("MATCH (dependent:File)-[:IMPORTS]->(f:File {path: $path}) RETURN dependent")
    List<FileNode> findDependentsByPath(@Param("path") String path);

}