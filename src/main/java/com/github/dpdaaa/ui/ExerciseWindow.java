package com.github.dpdaaa.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ExerciseWindow {

    public void openActiveExerciseWindow(String exerciseName, ObservableList<String> completedData) {
        Stage activeStage = new Stage();
        activeStage.setTitle("Aktuell: " + exerciseName);

        Label headerLabel = new Label(exerciseName);
        headerLabel.getStyleClass().add("header-label");

        Label timerLabel = new Label("Pause: 00:00");
        timerLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #D32F2F; -fx-font-weight: bold;");

        final int[] secondsPassed = { 0 };
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
            setsBox.getChildren().add(createSetRow(i, secondsPassed, timeline, setsBox));
        }

        Button btnAddSet = new Button("+ Weiterer Satz");
        btnAddSet.getStyleClass().add("menu-button");
        btnAddSet.setStyle("-fx-pref-height: 30px; -fx-font-size: 12px;");
        btnAddSet.setOnAction(e -> {
            setsBox.getChildren().add(createSetRow(setsBox.getChildren().size() + 1, secondsPassed, timeline, setsBox));
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

    private HBox createSetRow(int setNumber, int[] secondsPassed, Timeline timeline, VBox setsBox) {
        Label lblSet = new Label("Satz " + setNumber);
        lblSet.setStyle("-fx-text-fill: white; -fx-pref-width: 50px;");

        TextField txtWeight = new TextField();
        txtWeight.setPromptText("kg");
        txtWeight.setPrefWidth(60);

        TextField txtReps = new TextField();
        txtReps.setPromptText("Wdh");
        txtReps.setPrefWidth(60);

        Button btnDone = new Button("ok");
        btnDone.getStyleClass().add("action-button");

        Button btnEdit = new Button("edit");
        btnEdit.getStyleClass().add("action-button");
        btnEdit.setStyle("-fx-background-color: #FFA000;");
        btnEdit.setVisible(false);
        Button btnDelete = new Button("delete");
        btnDelete.getStyleClass().add("action-button");
        btnDelete.setStyle("-fx-background-color: #555555;");

        HBox row = new HBox(10, lblSet, txtWeight, txtReps, btnDone, btnEdit, btnDelete);
        row.setAlignment(Pos.CENTER);
        btnDone.setOnAction(e -> {
            btnDone.setDisable(true);
            btnDone.setStyle("-fx-background-color: #4CAF50;");
            txtWeight.setDisable(true);
            txtReps.setDisable(true);
            btnEdit.setVisible(true); 

            secondsPassed[0] = 0;
            timeline.playFromStart();
        });

        btnEdit.setOnAction(e -> {
            txtWeight.setDisable(false);
            txtReps.setDisable(false);
            btnDone.setDisable(false);
            btnDone.setStyle(""); 
            btnEdit.setVisible(false); 
        });

        btnDelete.setOnAction(e -> {
            setsBox.getChildren().remove(row); 
            updateSetLabels(setsBox);
        });

        return row;
    }

    private void updateSetLabels(VBox setsBox) {
        for (int i = 0; i < setsBox.getChildren().size(); i++) {
            HBox row = (HBox) setsBox.getChildren().get(i);
            Label lblSet = (Label) row.getChildren().get(0); 
            lblSet.setText("Satz " + (i + 1));
        }
    }
}
