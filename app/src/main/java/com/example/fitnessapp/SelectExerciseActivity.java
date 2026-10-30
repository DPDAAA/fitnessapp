package com.example.fitnessapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnessapp.adapter.ExerciseListAdapter;
import com.example.fitnessapp.model.Exercise;
import com.example.fitnessapp.storage.WorkoutRepository;
import java.util.List;

public class SelectExerciseActivity extends AppCompatActivity {

    private WorkoutRepository repo;
    private RecyclerView recycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_exercise);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repo = new WorkoutRepository(this);
        recycler = findViewById(R.id.recycler_exercises);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        
        loadExercises();

        findViewById(R.id.fab_create_exercise).setOnClickListener(v -> showCreateExerciseDialog());
    }

    private void loadExercises() {
        List<Exercise> exercises = repo.getAllExercises();
        ExerciseListAdapter adapter = new ExerciseListAdapter(exercises, exercise -> {
            Intent result = new Intent();
            result.putExtra("EXERCISE_ID", exercise.getId());
            setResult(RESULT_OK, result);
            finish();
        });
        recycler.setAdapter(adapter);
    }

    private void showCreateExerciseDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_create_exercise);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText editName = dialog.findViewById(R.id.edit_name);
        Spinner spinnerMuscle = dialog.findViewById(R.id.spinner_muscle);
        
        String[] muscles = {"Brust", "Rücken", "Schulter", "Bizeps", "Trizeps", "Beine", "Waden", "Bauch", "Sonstige"};
        // Use custom layouts for high contrast dark theme compatibility
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_muscle, muscles);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_muscle);
        spinnerMuscle.setAdapter(adapter);

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Name erforderlich", Toast.LENGTH_SHORT).show();
                return;
            }
            String muscle = spinnerMuscle.getSelectedItem().toString();
            repo.insertExercise(new Exercise(name, muscle));
            dialog.dismiss();
            loadExercises();
        });

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (repo != null) repo.close();
    }
}