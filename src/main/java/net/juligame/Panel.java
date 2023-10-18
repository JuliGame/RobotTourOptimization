package net.juligame;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.lang.reflect.Method;
import java.util.*;

public class Panel {

    Pane root;
    Text nodeCountText;
    Text timeText;
    List<Vector2> children = new ArrayList<>();
    public void start(Stage primaryStage) {
        root = new Pane();

        Scene scene = new Scene(root, 400, 400);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.A) {
                    index++;
                    drawOther();
            }

            if (e.getCode() == KeyCode.R) {
                root.getChildren().clear();
                children.clear();
                mapping.clear();
                solutionLines.clear();
                index = 0;
                addTexts();
            }
        });

        scene.setOnMouseClicked( e -> {
            Long time = System.currentTimeMillis();
            addNode(new Vector2((int) e.getX(), (int) e.getY()));
            recalculateDistances();
            timeText.setText("Time: " + (System.currentTimeMillis() - time) + " ms");
        });

        addTexts();

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addTexts() {
        nodeCountText = new Text("Node count: 0");
        nodeCountText.setX(10);
        nodeCountText.setY(20);
        root.getChildren().add(nodeCountText);

        timeText = new Text("Time: 0 ms");
        timeText.setX(10);
        timeText.setY(40);
        root.getChildren().add(timeText);
    }

    HashMap<Vector2, Circle> mapping = new HashMap<>();
    public void addNode(Vector2 v2) {
        Circle circle = new Circle(v2.x, v2.y, 10);
        root.getChildren().add(circle);
        children.add(v2);

        mapping.put(v2, circle);
        nodeCountText.setText("Node count: " + children.size());
    }

    ArrayList<Line> solutionLines = new ArrayList<>();
    ArrayList<Line> blackLines = new ArrayList<>();
    int index = 0;
    ArrayList<ArrayList<Vector2>> solutions = new ArrayList();
    public void recalculateDistances() {
        solutions.clear();

        if (children.size() < 2)
            return;

        float bestTotalDistance = Integer.MAX_VALUE;

        for (Line line : blackLines) {
            root.getChildren().remove(line);
        }
        blackLines.clear();

        if (children.size() < 40) {
            for (int i = 0; i < children.size(); i++) {
                Vector2 v1 = children.get(i);
                for (int j = i + 1; j < children.size(); j++) {
                    Vector2 v2 = children.get(j);
                    Line line = new Line(v1.x, v1.y, v2.x, v2.y);
                    line.setStroke(new Color(0,0,0,.3));
                    blackLines.add(line);
                    root.getChildren().add(line);
                }
            }
        }



        // Algoritmo rapido.
        for (Vector2 start: children) {
            ArrayList<Vector2> ordered = new ArrayList();
            ordered.add(start);

            List<Vector2> toVisit = new ArrayList(children);
            toVisit.remove(start);
            float totalDistance = 0;

            Vector2 last = null;
            while (!toVisit.isEmpty()) {
                Vector2 closest = null;
                for (Vector2 next: toVisit) {
                    if (closest == null) {
                        closest = next;
                    } else {
                        if (start.distance(next) < start.distance(closest)) {
                            closest = next;
                        }
                    }
                }

                if (closest == null)
                    break;

                if (last != null)
                    totalDistance += last.distance(closest);

                toVisit.remove(closest);
                ordered.add(closest);
                last = closest;
            }

            totalDistance += start.distance(ordered.get(ordered.size() - 1));
            ordered.add(start);
            System.out.println("Total distance: " + totalDistance);

            solutions.add(ordered);
            if (totalDistance < bestTotalDistance) {
                bestTotalDistance = totalDistance;
                index = solutions.size() - 1;
            }
        }
        drawOther();


        // Algoritmo lento, pero efectivo.
//        Metodo2.recalculateDistances(children, root, blackLines);


    }



    void drawOther(){
        for (Line line : solutionLines) {
            root.getChildren().remove(line);
        }
        solutionLines.clear();

        int selected = index % solutions.size();
        System.out.println("Solution: " + selected);
        for (int i = 0; i < solutions.get(selected).size() - 1; i++) {
            Vector2 v1 = solutions.get(selected).get(i);
            Vector2 v2 = solutions.get(selected).get(i + 1);
            Line line = new Line(v1.x, v1.y, v2.x, v2.y);
            line.setStroke(javafx.scene.paint.Color.RED);
            line.setStrokeWidth(2);
            solutionLines.add(line);
            root.getChildren().add(line);
        }
    }
}
