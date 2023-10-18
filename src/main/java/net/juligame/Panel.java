package net.juligame;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

            if (e.getCode() == KeyCode.D) {
                use2Opt = !use2Opt;
                recalculateDistances();
            }
        });
        AtomicInteger delay = new AtomicInteger(150);
        AtomicBoolean isPressed = new AtomicBoolean(false);

        scene.setOnMousePressed(e -> {
            isPressed.set(true);
        });
        scene.setOnMouseReleased(e -> {
            addNode(new Vector2((int) e.getX(), (int) e.getY()));
            isPressed.set(false);
            recalculateDistances();
            delay.set(150);
        });

        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            long lastTime = System.currentTimeMillis();

            @Override
            public void handle(MouseEvent event) {
                if (System.currentTimeMillis() - lastTime < delay.get())
                    return;

                addNode(new Vector2((int) event.getX(), (int) event.getY()));
                delay.set(delay.get() - 5);
                if (delay.get() < 30)
                    delay.set(30);
                lastTime = System.currentTimeMillis();
            }
        });


        addTexts();

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addTexts() {
        nodeCountText = new Text("Node count: 0");
        nodeCountText.setX(10);
        nodeCountText.setY(20);
        nodeCountText.setFill(Color.RED);
        root.getChildren().add(nodeCountText);

        timeText = new Text("Time: 0 ms");
        timeText.setX(10);
        timeText.setY(40);
        timeText.setFill(Color.RED);
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

    boolean use2Opt = true;
    public void recalculateDistances() {
        Long time = System.currentTimeMillis();
        solutions.clear();

        if (children.size() < 2)
            return;

        float bestTotalDistance = Integer.MAX_VALUE;

        for (Line line : blackLines) {
            root.getChildren().remove(line);
        }
        blackLines.clear();

        if (children.size() < 50) {
            for (int i = 0; i < children.size(); i++) {
                Vector2 v1 = children.get(i);
                for (int j = i + 1; j < children.size(); j++) {
                    Vector2 v2 = children.get(j);
                    Line line = new Line(v1.x, v1.y, v2.x, v2.y);
                    line.setStroke(new Color(0,0,0,.2));
                    blackLines.add(line);
                    root.getChildren().add(line);
                }
            }
        }


        // Algoritmo rapido. (Greedy)
        for (Vector2 start: children) {
            ArrayList<Vector2> ordered = new ArrayList();
            ordered.add(start);

            List<Vector2> toVisit = new ArrayList(children);
            toVisit.remove(start);

            Vector2 last = null;
            while (!toVisit.isEmpty()) {
                Vector2 closest = getClosest(last == null ? start : last, toVisit);

                if (closest == null)
                    break;

                toVisit.remove(closest);
                ordered.add(closest);
                last = closest;
            }

            ordered.add(start);
            float totalDistance = getTotalDistance(ordered);
            System.out.println("Total distance: " + totalDistance);

            solutions.add(ordered);
            if (totalDistance < bestTotalDistance) {
                bestTotalDistance = totalDistance;
                index = solutions.size() - 1;
            }
        }

        // 2da parte. 2-opt swap.
        if (use2Opt){
            boolean improved;
            List<Vector2> ordered = solutions.get(index % solutions.size());
            do {
                improved = false;
                for(int i = 0; i < ordered.size(); i++){
                    for(int j = i + 2; j < ordered.size(); j++){
                        int nextI = (i+1) % ordered.size();
                        int nextJ = (j+1) % ordered.size();
                        Vector2 a = ordered.get(i);
                        Vector2 b = ordered.get(nextI);
                        Vector2 c = ordered.get(j);
                        Vector2 d = ordered.get(nextJ);

                        if(a.distance(b) + c.distance(d) > a.distance(c) + b.distance(d)){
                            Collections.reverse(ordered.subList(i+1, j+1));
                            improved = true;
                        }
                    }
                }
            } while(improved);
        }

        drawOther();


        // Algoritmo lento, pero 100% efectivo.
//        Metodo2.recalculateDistances(children, root, blackLines);


        timeText.setText("Time: " + (System.currentTimeMillis() - time) + " ms");
    }


    private Vector2 getClosest(Vector2 curr, List<Vector2> points){
        Vector2 closest = null;
        for(Vector2 point : points){
            if(closest == null || curr.distance(point) < curr.distance(closest)){
                closest = point;
            }
        }
        return closest;
    }

    private float getTotalDistance(ArrayList<Vector2> points){
        float totalDistance = 0;
        for(int i = 0; i < points.size(); ++i){
            totalDistance += points.get(i).distance(points.get((i+1)%points.size()));
        }
        return totalDistance;
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
