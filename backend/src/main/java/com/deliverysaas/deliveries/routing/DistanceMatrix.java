package com.deliverysaas.deliveries.routing;

import java.util.List;

import com.deliverysaas.shared.geo.GeoPoint;

public class DistanceMatrix {

    public static double[][] of(List<GeoPoint> points) {
        int n = points.size();
        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double km = points.get(i).distanceKmTo(points.get(j));
                matrix[i][j] = km;
                matrix[j][i] = km;
            }
        }
        return matrix;
    }

    public static double pathLength(List<Integer> path, double[][] distances) {
        double total = 0;
        for (int i = 0; i + 1 < path.size(); i++) {
            total += distances[path.get(i)][path.get(i + 1)];
        }
        return total;
    }
}
