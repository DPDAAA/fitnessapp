package com.example.fitnessapp.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnessapp.R;
import com.example.fitnessapp.ActiveWorkoutActivity;
import com.example.fitnessapp.PresetDetailActivity;
import com.example.fitnessapp.adapter.PresetAdapter;
import com.example.fitnessapp.model.WorkoutPreset;
import com.example.fitnessapp.storage.WorkoutRepository;
import java.util.List;

public class TrainingFragment extends Fragment {

    private WorkoutRepository repo;
    private RecyclerView recycler;
    private PresetAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_training, container, false);
        repo = new WorkoutRepository(requireContext());

        view.findViewById(R.id.btn_free_workout).setOnClickListener(v -> {
            long sessionId = repo.startSession(null, String.valueOf(System.currentTimeMillis()));
            if (sessionId == -1) {
                Toast.makeText(getContext(), "Es läuft bereits ein Training!", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(requireContext(), ActiveWorkoutActivity.class));
            }
        });

        recycler = view.findViewById(R.id.recycler_presets);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        view.findViewById(R.id.fab_add_preset).setOnClickListener(v -> showCreatePresetDialog());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPresets();
    }

    private void loadPresets() {
        List<WorkoutPreset> presets = repo.getAllPresets();
        adapter = new PresetAdapter(presets, preset -> {
            Intent intent = new Intent(requireContext(), PresetDetailActivity.class);
            intent.putExtra("PRESET_ID", preset.getId());
            startActivity(intent);
        });
        recycler.setAdapter(adapter);
    }

    private void showCreatePresetDialog() {
        EditText input = new EditText(requireContext());
        input.setHint("Preset Name");
        input.setTextColor(getResources().getColor(R.color.text_primary, null));
        input.setHintTextColor(getResources().getColor(R.color.text_secondary, null));

        new AlertDialog.Builder(requireContext(), android.R.style.Theme_Material_Dialog_Alert)
                .setTitle("Neues Preset")
                .setView(input)
                .setPositiveButton("Erstellen", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        long id = repo.insertPreset(new WorkoutPreset(name, String.valueOf(System.currentTimeMillis())));
                        Intent intent = new Intent(requireContext(), PresetDetailActivity.class);
                        intent.putExtra("PRESET_ID", id);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }
}