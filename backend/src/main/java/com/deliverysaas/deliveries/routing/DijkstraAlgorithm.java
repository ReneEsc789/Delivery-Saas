package com.deliverysaas.deliveries.routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

public class DijkstraAlgorithm {

    public static Optional<RouteResult> shortestPath(Graph graph, int source, int target) {
        int n = graph.size();
        double[] distance = new double[n];
        int[] previous = new int[n];
        Arrays.fill(distance, Double.POSITIVE_INFINITY);
        Arrays.fill(previous, -1);
        distance[source] = 0;

        PriorityQueue<NodeDistance> queue = new PriorityQueue<>(Comparator.comparingDouble(NodeDistance::distance));
        queue.add(new NodeDistance(source, 0));

        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();
            // Entrada vieja: ya encontramos un camino más corto a este nodo después de meterla.
            if (current.distance() > distance[current.node()]) {
                continue;
            }
            if (current.node() == target) {
                break;
            }
            for (Graph.Edge edge : graph.edgesFrom(current.node())) {
                double candidate = distance[current.node()] + edge.weight();
                if (candidate < distance[edge.to()]) {
                    distance[edge.to()] = candidate;
                    previous[edge.to()] = current.node();
                    queue.add(new NodeDistance(edge.to(), candidate));
                }
            }
        }

        if (distance[target] == Double.POSITIVE_INFINITY) {
            return Optional.empty();
        }

        List<Integer> path = new ArrayList<>();
        for (int node = target; node != -1; node = previous[node]) {
            path.add(node);
        }
        Collections.reverse(path);
        return Optional.of(new RouteResult(path, distance[target]));
    }

    private record NodeDistance(int node, double distance) {}
}
