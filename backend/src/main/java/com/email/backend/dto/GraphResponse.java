package com.email.backend.dto;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphResponse {

    private List<GraphNode> nodes;
    private List<GraphEdge> edges;

    public GraphResponse(List<GraphNode> graphNodes, List<GraphEdge> edges) {
        this.edges = edges;
        this.nodes = graphNodes;
    }

}
