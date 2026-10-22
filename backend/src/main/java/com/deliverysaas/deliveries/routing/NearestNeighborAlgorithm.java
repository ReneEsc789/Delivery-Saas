package com.deliverysaas.deliveries.routing;

import java.util.ArrayList;
import java.util.List;

public class NearestNeighborAlgorithm {

    public static List<Integer> buildRoute(double[][] distances) {
        int n = distances.length;
        boolean[] visited = new boolean[n];
        List<Integer> route = new ArrayList<>();

        int current = 0;
        visited[current] = true;
        route.add(current);

        for (int step = 1; step < n; step++) {
            int nearest = -1;
            for (int candidate = 0; candidate < n; candidate++) {
                if (!visited[candidate]
                        && (nearest == -1 || distances[current][candidate] < distances[current][nearest])) {
                    nearest = candidate;
                }
            }
            visited[nearest] = true;
            route.add(nearest);
            current = nearest;
        }
        return route;
    }
}
