package com.email.backend.controller;

import com.email.backend.model.FileNode;
import com.email.backend.service.GraphService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/graph")
public class GraphController {

    private final GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping("/project/{id}")
    public ResponseEntity<List<FileNode>> getProjectGraph(@PathVariable("id") Integer id) {
        List<FileNode> graph = graphService.getProjectGraph(id);
        return ResponseEntity.ok(graph);
    }

    @GetMapping("/dependencies")
    public ResponseEntity<List<FileNode>> getDependencies(@RequestParam("path") String path) {
        List<FileNode> dependencies = graphService.getDependencies(path);
        return ResponseEntity.ok(dependencies);
    }

    @GetMapping("/dependents")
    public ResponseEntity<List<FileNode>> getDependents(@RequestParam("path") String path) {
        List<FileNode> dependents = graphService.getDependents(path);
        return ResponseEntity.ok(dependents);
    }
}
