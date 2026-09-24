package com.deliverysaas.deliveries.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TwoOptAlgorithm {

    private static final double minImprovementKm = 1e-9;

    public static List<Integer> improve(List<Integer> initialRoute, double[][] distances) {
        List<Integer> route = new ArrayList<>(initialRoute);
        int n = route.size();
        boolean improved = true;

        while (improved) {
            improved = false;
            for (int i = 1; i < n - 1; i++) {
                for (int k = i + 1; k < n; k++) {
                    int before = route.get(i - 1);
                    int first = route.get(i);
                    int last = route.get(k);

                    double removed = distances[before][first];
                    double added = distances[before][last];
                    if (k + 1 < n) {
                        int after = route.get(k + 1);
                        removed += distances[last][after];
                        added += distances[first][after];
                    }

                    if (added < removed - minImprovementKm) {
                        Collections.reverse(route.subList(i, k + 1));
                        improved = true;
                    }
                }
            }
        }
        return route;
    }
}
