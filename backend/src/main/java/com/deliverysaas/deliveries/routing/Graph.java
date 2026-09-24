package com.deliverysaas.deliveries.routing;

import java.util.ArrayList;
import java.util.List;

public class Graph {

    private final List<List<Edge>> adjacency = new ArrayList<>();

    public Graph(int nodeCount) {
        for (int i = 0; i < nodeCount; i++) {
            adjacency.add(new ArrayList<>());
        }
    }

    public void connect(int a, int b, double weight) {
        adjacency.get(a).add(new Edge(b, weight));
        adjacency.get(b).add(new Edge(a, weight));
    }

    public List<Edge> edgesFrom(int node) {
        return adjacency.get(node);
    }

    public int size() {
        return adjacency.size();
    }

    public record Edge(int to, double weight) {}
}
