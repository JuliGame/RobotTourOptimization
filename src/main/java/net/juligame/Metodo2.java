package net.juligame;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Metodo2 {

    public static class BestPathInfo {
        List<Vector2> bestPath = new ArrayList<>();
        double bestDistance = Double.MAX_VALUE;
    }

    public static void recalculateDistances(List<Vector2> children, Pane root,
                                            List<Line> lines) {
        BestPathInfo bestPathInfo = new BestPathInfo();

        List<Vector2> path = new ArrayList<>(children);
        calculatePermutations(path, 0, bestPathInfo);

        for (int i = 0; i < bestPathInfo.bestPath.size() - 1; i++) {
            Vector2 v1 = bestPathInfo.bestPath.get(i);
            Vector2 v2 = bestPathInfo.bestPath.get(i + 1);

            Line line = new Line(v1.x, v1.y, v2.x, v2.y);
            root.getChildren().add(line);
            lines.add(line);
        }

        // Connect the last point to the start point
        if (!bestPathInfo.bestPath.isEmpty()) {
            Vector2 first = bestPathInfo.bestPath.get(0);
            Vector2 last = bestPathInfo.bestPath.get(bestPathInfo.bestPath.size() - 1);
            Line line = new Line(last.x, last.y, first.x, first.y);
            root.getChildren().add(line);
            lines.add(line);
        }
    }

    private static void calculatePermutations(List<Vector2> path, int currentIndex,
                                              BestPathInfo bestPathInfo) {
        if (currentIndex == path.size() - 1) {
            double totalDistance = calculateTotalDistance(path);
            if (totalDistance < bestPathInfo.bestDistance) {
                bestPathInfo.bestPath.clear();
                bestPathInfo.bestPath.addAll(path);
                bestPathInfo.bestDistance = totalDistance;
            }
            return;
        }

        for (int i = currentIndex; i < path.size(); i++) {
            Collections.swap(path, currentIndex, i);
            calculatePermutations(path, currentIndex + 1, bestPathInfo);
            Collections.swap(path, currentIndex, i); // backtrack
        }
    }

    private static double calculateTotalDistance(List<Vector2> path) {
        double distance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            distance += path.get(i).distance(path.get(i + 1));
        }
        distance += path.get(path.size() - 1).distance(path.get(0)); // The last point to the first
        return distance;
    }

}