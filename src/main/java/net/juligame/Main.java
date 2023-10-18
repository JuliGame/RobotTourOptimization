package net.juligame;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public static Panel panel = new Panel();
    @Override
    public void start(Stage primaryStage) {
        panel.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}