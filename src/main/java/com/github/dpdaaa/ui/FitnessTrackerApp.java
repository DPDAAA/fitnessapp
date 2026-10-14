package com.github.dpdaaa.ui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FitnessTrackerApp extends Application {

    ExerciseWindow window = new ExerciseWindow();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Fitness Tracker");

        Label titleLabel = new Label("Wähle dein heutiges Training");
        titleLabel.getStyleClass().add("title-label");

        Button btnPush = new Button("Brust/Rücken");
        Button btnPull = new Button("Schulter/Arme");
        Button btnLegs = new Button("Beine/Bauch");
        Button btnFullBody = new Button("Ganzkörper");

        btnPush.getStyleClass().add("menu-button");
        btnPull.getStyleClass().add("menu-button");
        btnLegs.getStyleClass().add("menu-button");
        btnFullBody.getStyleClass().add("menu-button");

        btnPush.setOnAction(e -> openSessionOverview("Push Training", "Bankdrücken", "Schulterdrücken", "Dips", "Flys"));
        btnPull.setOnAction(e -> openSessionOverview("Pull Training", "Klimmzüge", "Langhantelrudern", "Latzug", "Bizepscurls"));
        btnLegs.setOnAction(e -> openSessionOverview("Beintraining", "Kniebeugen", "Beinpresse", "Kreuzheben", "Wadenheben"));
        btnFullBody.setOnAction(e -> openSessionOverview("Ganzkörper", "Kniebeugen", "Bankdrücken", "Kreuzheben", "Klimmzüge"));

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getChildren().addAll(titleLabel, btnPush, btnPull, btnLegs, btnFullBody);

        Scene scene = new Scene(root, 400, 500);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openSessionOverview(String trainingName, String... defaultExercises) {

        Stage sessionStage = new Stage();
        sessionStage.setTitle(trainingName + " - Übersicht");

        Label headerLabel = new Label("Aktuelle Session: " + trainingName);
        headerLabel.getStyleClass().add("header-label");

        ComboBox<String> exerciseBox = new ComboBox<>();
        exerciseBox.getItems().addAll(defaultExercises);
        exerciseBox.setPromptText("Neue Übung wählen...");
        exerciseBox.getStyleClass().add("combo-box");

        Button btnStartExercise = new Button("Übung starten");
        btnStartExercise.getStyleClass().add("action-button");

        HBox topControls = new HBox(15, exerciseBox, btnStartExercise);
        topControls.setAlignment(Pos.CENTER);
        topControls.setPadding(new Insets(10, 0, 20, 0));

        ListView<String> completedExercisesList = new ListView<>();
        completedExercisesList.getStyleClass().add("table-view"); 
        ObservableList<String> completedData = FXCollections.observableArrayList();
        completedExercisesList.setItems(completedData);

        btnStartExercise.setOnAction(e -> {
            String selected = exerciseBox.getValue();
            if (selected != null && !selected.isEmpty()) {
                window.openActiveExerciseWindow(selected, completedData);
            }
        });

        VBox layout = new VBox(15, headerLabel, topControls, new Label("Erledigte Übungen:"), completedExercisesList);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.getStyleClass().add("secondary-window");

        Scene sessionScene = new Scene(layout, 450, 500);
        sessionScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        sessionStage.setScene(sessionScene);
        sessionStage.show();
    }



    public static void main(String[] args) {
        launch(args);
    }
}