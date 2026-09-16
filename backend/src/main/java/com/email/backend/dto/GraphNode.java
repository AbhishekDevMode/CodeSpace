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

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
}
