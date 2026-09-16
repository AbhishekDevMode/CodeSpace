package com.email.backend.controller;

import com.email.backend.dto.GraphResponse;
import com.email.backend.model.FileNode;
import com.email.backend.service.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/graph")
public class GraphController {

    @Autowired
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

    @GetMapping("/project/{projectId}/view")
    public ResponseEntity<GraphResponse> getGraph(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "1") int depth,
            @RequestParam(required = false) String focus) {

        GraphResponse response = graphService.getGraph(projectId, depth, focus);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expand/{nodeId}")
    public ResponseEntity<GraphResponse> expandNode(@PathVariable String nodeId) {
        GraphResponse response = graphService.expandNode(nodeId);
        return ResponseEntity.ok(response);
    }
}
