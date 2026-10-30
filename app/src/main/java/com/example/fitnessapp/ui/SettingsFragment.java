package com.example.fitnessapp.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.fitnessapp.R;
import com.example.fitnessapp.storage.WorkoutRepository;
import com.example.fitnessapp.util.BackupManager;
import com.example.fitnessapp.util.SettingsManager;
import com.example.fitnessapp.util.TimeUtils;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.io.InputStream;
import java.io.OutputStream;

public class SettingsFragment extends Fragment {
    
    private SettingsManager settingsManager;
    private WorkoutRepository repo;
    private TextView tvDurationDesc;
    
    private final ActivityResultLauncher<String> createBackupLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/zip"),
            uri -> {
                if (uri != null) performBackup(uri);
            }
    );

    private final ActivityResultLauncher<String[]> restoreBackupLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) inspectAndRestoreBackup(uri);
            }
    );
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        settingsManager = new SettingsManager(requireContext());
        repo = new WorkoutRepository(requireContext());
        
        SwitchMaterial switchAutoTimer = view.findViewById(R.id.switch_auto_timer);
        tvDurationDesc = view.findViewById(R.id.tv_timer_duration_desc);
        MaterialButtonToggleGroup toggleUnit = view.findViewById(R.id.toggle_weight_unit);
        SwitchMaterial switchVibrate = view.findViewById(R.id.switch_vibrate);
        SwitchMaterial switchScreenOn = view.findViewById(R.id.switch_screen_on);
        
        // Initialize UI with current settings
        switchAutoTimer.setChecked(settingsManager.isAutoTimerEnabled());
        tvDurationDesc.setText("Aktuell: " + settingsManager.getTimerDurationSec() + " Sekunden");
        
        if (settingsManager.isUnitLb()) {
            toggleUnit.check(R.id.btn_unit_lb);
        } else {
            toggleUnit.check(R.id.btn_unit_kg);
        }
        
        switchVibrate.setChecked(settingsManager.isVibrateEnabled());
        switchScreenOn.setChecked(settingsManager.isKeepScreenOnEnabled());
        
        // Listeners
        switchAutoTimer.setOnCheckedChangeListener((btn, isChecked) -> {
            settingsManager.setAutoTimerEnabled(isChecked);
        });
        
        view.findViewById(R.id.btn_change_duration).setOnClickListener(v -> showDurationDialog());
        
        toggleUnit.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_unit_lb) {
                    settingsManager.setWeightUnit("lb");
                } else {
                    settingsManager.setWeightUnit("kg");
                }
            }
        });
        
        switchVibrate.setOnCheckedChangeListener((btn, isChecked) -> {
            settingsManager.setVibrateEnabled(isChecked);
        });
        
        switchScreenOn.setOnCheckedChangeListener((btn, isChecked) -> {
            settingsManager.setKeepScreenOnEnabled(isChecked);
        });
        
        view.findViewById(R.id.btn_backup_create).setOnClickListener(v -> {
            if (repo.getActiveSession() != null) {
                Toast.makeText(getContext(), "Bitte beende zuerst das aktive Training.", Toast.LENGTH_LONG).show();
                return;
            }
            createBackupLauncher.launch("fitnessapp_backup.zip");
        });

        view.findViewById(R.id.btn_backup_restore).setOnClickListener(v -> {
            if (repo.getActiveSession() != null) {
                Toast.makeText(getContext(), "Bitte beende zuerst das aktive Training.", Toast.LENGTH_LONG).show();
                return;
            }
            restoreBackupLauncher.launch(new String[]{"application/zip", "application/octet-stream"});
        });
        
        return view;
    }
    
    private void performBackup(Uri uri) {
        try {
            int wCount = repo.getWorkoutCount();
            int eCount = repo.getExerciseCount();
            int pCount = repo.getPresetCount();
            
            // It's important to close repo temporarily to flush WAL or ensure DB is fully flushed
            // However, SQLiteOpenHelper manages this decently if no active transaction.
            
            OutputStream os = requireContext().getContentResolver().openOutputStream(uri);
            if (os != null) {
                BackupManager bm = new BackupManager(requireContext());
                BackupManager.BackupResult res = bm.createBackup(os, wCount, eCount, pCount);
                if (res.success) {
                    Toast.makeText(getContext(), "Backup erfolgreich erstellt!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), res.message, Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void inspectAndRestoreBackup(Uri uri) {
        try {
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            if (is == null) return;
            
            BackupManager bm = new BackupManager(requireContext());
            BackupManager.RestoreMetadata meta = bm.inspectBackup(is);
            
            if (!meta.valid) {
                new AlertDialog.Builder(requireContext(), android.R.style.Theme_Material_Dialog_Alert)
                    .setTitle("Fehlerhaftes Backup")
                    .setMessage(meta.message)
                    .setPositiveButton("OK", null)
                    .show();
                return;
            }
            
            String msg = "Backup vom " + TimeUtils.formatDate(meta.createdAtMs) + " " + TimeUtils.formatTime(meta.createdAtMs) + " Uhr\n\n" +
                         "• Workouts: " + meta.workouts + "\n" +
                         "• Übungen: " + meta.exercises + "\n" +
                         "• Presets: " + meta.presets + "\n\n" +
                         "Achtung: Dein lokaler Datenbestand wird unwiderruflich überschrieben!";
                         
            new AlertDialog.Builder(requireContext(), android.R.style.Theme_Material_Dialog_Alert)
                .setTitle("Backup wiederherstellen?")
                .setMessage(msg)
                .setPositiveButton("Wiederherstellen", (d, w) -> executeRestore(bm, meta))
                .setNegativeButton("Abbrechen", null)
                .show();
                
        } catch (Exception e) {
            Toast.makeText(getContext(), "Fehler beim Lesen der Datei.", Toast.LENGTH_LONG).show();
        }
    }
    
    private void executeRestore(BackupManager bm, BackupManager.RestoreMetadata meta) {
        // CLOSE repository connections before overwriting!
        if (repo != null) {
            repo.close();
        }
        
        BackupManager.BackupResult res = bm.executeRestore(meta.preferences);
        
        // REOPEN repository
        repo = new WorkoutRepository(requireContext());
        
        if (res.success) {
            Toast.makeText(getContext(), "Wiederherstellung erfolgreich! App wird neu gestartet...", Toast.LENGTH_LONG).show();
            // Reload activity to apply prefs and new DB instantly
            requireActivity().finish();
            startActivity(requireActivity().getIntent());
        } else {
            new AlertDialog.Builder(requireContext(), android.R.style.Theme_Material_Dialog_Alert)
                .setTitle("Fehler")
                .setMessage(res.message)
                .setPositiveButton("OK", null)
                .show();
        }
    }
    
    private void showDurationDialog() {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(settingsManager.getTimerDurationSec()));
        input.setTextColor(getResources().getColor(R.color.text_primary, null));
        
        new AlertDialog.Builder(requireContext(), android.R.style.Theme_Material_Dialog_Alert)
            .setTitle("Pausendauer (in Sek)")
            .setView(input)
            .setPositiveButton("Speichern", (dialog, which) -> {
                String valStr = input.getText().toString().trim();
                try {
                    int val = Integer.parseInt(valStr);
                    if (val >= 15 && val <= 600) {
                        settingsManager.setTimerDurationSec(val);
                        tvDurationDesc.setText("Aktuell: " + val + " Sekunden");
                    } else {
                        Toast.makeText(getContext(), "Bitte Wert zwischen 15 und 600 wählen.", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Ungültige Eingabe.", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Abbrechen", null)
            .show();
    }
}