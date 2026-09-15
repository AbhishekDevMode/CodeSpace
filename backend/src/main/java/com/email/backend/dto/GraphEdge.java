package com.email.backend.dto;

import jdk.jfr.DataAmount;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstrutor
public class GraphEdge {

    private String source;
    private String target;
    private int weight;

    public GraphEdge(String source, String target, int weight) {
        this.source = source;
        this.target = target;
        this.weight=weight;
    }
}

