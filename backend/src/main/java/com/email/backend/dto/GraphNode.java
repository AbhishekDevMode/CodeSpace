package com.email.backend.dto;

public class GraphNode {

    private String id;
    private String label;
    private String path;
    private String language;
    private String type;
    private int weight;

    public GraphNode(String id, String label, String path, String language, String type, int weight) {
        this.id = id;
        this.label = label;
        this.path = path;
        this.language = language;
        this.type = type;
        this.weight = weight;
    }
}
