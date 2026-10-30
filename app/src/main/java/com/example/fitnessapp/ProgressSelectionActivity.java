package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnessapp.adapter.TrainedExerciseAdapter;
import com.example.fitnessapp.storage.WorkoutRepository;
import java.util.List;

public class ProgressSelectionActivity extends AppCompatActivity {

    private WorkoutRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_progress_selection);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repo = new WorkoutRepository(this);
        RecyclerView recycler = findViewById(R.id.recycler_trained_exercises);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        List<String> exercises = repo.getTrainedExerciseNames();
        
        if (exercises.isEmpty()) {
            Toast.makeText(this, "Noch keine abgeschlossenen Übungen vorhanden.", Toast.LENGTH_LONG).show();
        } else {
            TrainedExerciseAdapter adapter = new TrainedExerciseAdapter(exercises, name -> {
                Intent intent = new Intent(this, ExerciseProgressActivity.class);
                intent.putExtra("EXERCISE_NAME", name);
                startActivity(intent);
            });
            recycler.setAdapter(adapter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (repo != null) repo.close();
    }
}