package com.example.fitnessapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.adapter.PresetExerciseAdapter;
import com.example.fitnessapp.model.PresetExercise;
import com.example.fitnessapp.model.WorkoutPreset;
import com.example.fitnessapp.storage.WorkoutRepository;

import java.util.List;

public class PresetDetailActivity extends AppCompatActivity {

    private WorkoutRepository repo;
    private long presetId;
    private TextView tvName;
    private RecyclerView recycler;
    private PresetExerciseAdapter adapter;
    
    private final ActivityResultLauncher<Intent> selectExerciseLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    long exerciseId = result.getData().getLongExtra("EXERCISE_ID", -1);
                    if (exerciseId != -1) {
                        int order = adapter.getItemCount();
                        PresetExercise pe = new PresetExercise(0, presetId, exerciseId, order, "", ""); 
                        repo.insertPresetExercise(pe);
                        loadExercises(); 
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_preset_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        presetId = getIntent().getLongExtra("PRESET_ID", -1);
        if (presetId == -1) {
            finish();
            return;
        }

        repo = new WorkoutRepository(this);
        tvName = findViewById(R.id.tv_preset_name);
        recycler = findViewById(R.id.recycler_preset_exercises);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        loadPresetInfo();
        loadExercises();

        findViewById(R.id.btn_edit_name).setOnClickListener(v -> showEditNameDialog());
        
        findViewById(R.id.btn_delete_preset).setOnClickListener(v -> {
            new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                    .setTitle("Preset löschen")
                    .setMessage("Möchtest du dieses Preset wirklich löschen? Historische Trainings bleiben erhalten.")
                    .setPositiveButton("Löschen", (d, w) -> {
                        repo.deletePreset(presetId);
                        finish();
                    })
                    .setNegativeButton("Abbrechen", null)
                    .show();
        });

        findViewById(R.id.fab_add_exercise).setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectExerciseActivity.class);
            selectExerciseLauncher.launch(intent);
        });

        findViewById(R.id.btn_start_workout).setOnClickListener(v -> {
            long sessionId = repo.startSession(presetId, String.valueOf(System.currentTimeMillis()));
            if (sessionId == -1) {
                Toast.makeText(this, "Es läuft bereits ein Training!", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, ActiveWorkoutActivity.class));
                finish();
            }
        });
        
        setupDragAndDrop();
    }

    private void loadPresetInfo() {
        WorkoutPreset preset = repo.getPresetById(presetId);
        if (preset != null) {
            tvName.setText(preset.getName());
        }
    }

    private void loadExercises() {
        List<PresetExercise> exercises = repo.getExercisesForPreset(presetId);
        adapter = new PresetExerciseAdapter(exercises, (pe, position) -> {
            repo.deletePresetExercise(pe.getId());
            adapter.removeExercise(position);
        });
        recycler.setAdapter(adapter);
    }
    
    private void setupDragAndDrop() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                adapter.swap(from, to);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                List<PresetExercise> list = adapter.getExercises();
                for (int i = 0; i < list.size(); i++) {
                    PresetExercise pe = list.get(i);
                    if (pe.getOrderIndex() != i) {
                        pe.setOrderIndex(i);
                        repo.updatePresetExerciseOrder(pe.getId(), i);
                    }
                }
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(recycler);
    }

    private void showEditNameDialog() {
        EditText input = new EditText(this);
        input.setText(tvName.getText());
        input.setTextColor(getResources().getColor(R.color.text_primary, null));
        new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle("Preset umbenennen")
                .setView(input)
                .setPositiveButton("Speichern", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        repo.updatePresetName(presetId, newName);
                        loadPresetInfo();
                    }
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (repo != null) repo.close();
    }
}