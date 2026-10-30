package com.example.fitnessapp;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.adapter.ExerciseAdapter;
import com.example.fitnessapp.model.PlanExercise;
import com.example.fitnessapp.model.Session;
import com.example.fitnessapp.storage.WorkoutRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class WorkoutOverviewActivity extends AppCompatActivity {

    private WorkoutRepository repository;
    private long dayId;
    private RecyclerView recyclerExercises;
    private ExerciseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workout_overview);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dayId = getIntent().getLongExtra("DAY_ID", -1);
        if (dayId == -1) {
            finish();
            return;
        }

        repository = new WorkoutRepository(this);
        recyclerExercises = findViewById(R.id.recycler_exercises);
        recyclerExercises.setLayoutManager(new LinearLayoutManager(this));

        loadExercises();

        FloatingActionButton fabAdd = findViewById(R.id.fab_add_exercise);
        fabAdd.setOnClickListener(v -> showAddExerciseDialog());

        Button btnStartWorkout = findViewById(R.id.btn_start_workout_session);
        btnStartWorkout.setOnClickListener(v -> {
            Session session = new Session(dayId, String.valueOf(System.currentTimeMillis()), null, Session.STATUS_ACTIVE);
            long sessionId = repository.insertSession(session);
            if (sessionId == -1) {
                Toast.makeText(this, "Workout already in progress!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Workout Session Started!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadExercises() {
        List<PlanExercise> exercises = repository.getExercisesForDay(dayId);
        adapter = new ExerciseAdapter(exercises, (exercise, position) -> {
            repository.deletePlanExercise(exercise.getId());
            adapter.removeExercise(position);
        });
        recyclerExercises.setAdapter(adapter);
    }

    private void showAddExerciseDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_exercise);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText editName = dialog.findViewById(R.id.edit_name);
        EditText editMuscle = dialog.findViewById(R.id.edit_muscle);
        EditText editSets = dialog.findViewById(R.id.edit_sets);
        EditText editReps = dialog.findViewById(R.id.edit_reps);
        EditText editWeight = dialog.findViewById(R.id.edit_weight);
        
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnSave = dialog.findViewById(R.id.btn_save);
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnSave.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String muscle = editMuscle.getText().toString().trim();
            int sets = editSets.getText().toString().isEmpty() ? 0 : Integer.parseInt(editSets.getText().toString());
            int reps = editReps.getText().toString().isEmpty() ? 0 : Integer.parseInt(editReps.getText().toString());
            double weight = editWeight.getText().toString().isEmpty() ? 0.0 : Double.parseDouble(editWeight.getText().toString());
            
            long id = repository.insertPlanExercise(dayId, name, muscle, sets, reps, weight);
            adapter.addExercise(new PlanExercise(id, dayId, name, muscle, sets, reps, weight));
            
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (repository != null) {
            repository.close();
        }
    }
}