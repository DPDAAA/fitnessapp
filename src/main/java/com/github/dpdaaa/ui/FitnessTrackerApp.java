package com.github.dpdaaa.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class FitnessTrackerApp extends Application {

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
                openActiveExerciseWindow(selected, completedData);
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

    private void openActiveExerciseWindow(String exerciseName, ObservableList<String> completedData) {
        Stage activeStage = new Stage();
        activeStage.setTitle("Aktuell: " + exerciseName);

        Label headerLabel = new Label(exerciseName);
        headerLabel.getStyleClass().add("header-label");

        Label timerLabel = new Label("Pause: 00:00");
        timerLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #D32F2F; -fx-font-weight: bold;");
        
        final int[] secondsPassed = {0}; 
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsPassed[0]++;
            int minutes = secondsPassed[0] / 60;
            int seconds = secondsPassed[0] % 60;
            timerLabel.setText(String.format("Pause: %02d:%02d", minutes, seconds));
        }));
        timeline.setCycleCount(Animation.INDEFINITE);

        VBox setsBox = new VBox(10);
        setsBox.setAlignment(Pos.CENTER);
        
        for (int i = 1; i <= 3; i++) {
            setsBox.getChildren().add(createSetRow(i, secondsPassed, timeline));
        }

        Button btnAddSet = new Button("+ Weiterer Satz");
        btnAddSet.getStyleClass().add("menu-button");
        btnAddSet.setStyle("-fx-pref-height: 30px; -fx-font-size: 12px;");
        btnAddSet.setOnAction(e -> {
            setsBox.getChildren().add(createSetRow(setsBox.getChildren().size() + 1, secondsPassed, timeline));
        });

        

        Button btnFinishExercise = new Button("Übung abschließen");
        btnFinishExercise.getStyleClass().add("action-button");
        btnFinishExercise.setOnAction(e -> {
            timeline.stop();
            completedData.add(exerciseName + " (" + setsBox.getChildren().size() + " Sätze)");
            activeStage.close();
        });

        VBox layout = new VBox(20, headerLabel, timerLabel, setsBox, btnAddSet, btnFinishExercise);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.getStyleClass().add("secondary-window");

        Scene activeScene = new Scene(layout, 400, 500);
        activeScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        activeStage.setScene(activeScene);
        activeStage.show();
    }

    private HBox createSetRow(int setNumber, int[] secondsPassed, Timeline timeline) {
        Label lblSet = new Label("Satz " + setNumber);
        lblSet.setStyle("-fx-text-fill: white; -fx-pref-width: 50px;");

        TextField txtWeight = new TextField();
        txtWeight.setPromptText("kg");
        txtWeight.setPrefWidth(60);

        TextField txtReps = new TextField();
        txtReps.setPromptText("Wdh");
        txtReps.setPrefWidth(60);

        Button btnDone = new Button("Fertig");
        btnDone.getStyleClass().add("action-button");
        
        btnDone.setOnAction(e -> {
            btnDone.setDisable(true);
            btnDone.setStyle("-fx-background-color: #4CAF50;");
            txtWeight.setDisable(true);
            txtReps.setDisable(true);

            secondsPassed[0] = 0;
            timeline.playFromStart();
        });

        HBox row = new HBox(15, lblSet, txtWeight, txtReps, btnDone);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    public static void main(String[] args) {
        launch(args);
    }
}