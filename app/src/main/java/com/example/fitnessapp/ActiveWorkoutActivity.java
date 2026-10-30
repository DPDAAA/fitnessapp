package com.example.fitnessapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
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

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowManager;

import com.example.fitnessapp.adapter.ActiveExerciseAdapter;
import com.example.fitnessapp.model.Session;
import com.example.fitnessapp.model.SessionExercise;
import com.example.fitnessapp.model.WorkoutSet;
import com.example.fitnessapp.storage.WorkoutRepository;
import com.example.fitnessapp.util.SettingsManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class ActiveWorkoutActivity extends AppCompatActivity {

    private WorkoutRepository repo;
    private Session session;
    private RecyclerView recycler;
    private ActiveExerciseAdapter adapter;
    private TextView tvDuration;

    private Handler timerHandler = new Handler(Looper.getMainLooper());

    // Duration Timer (Top)
    private Runnable durationRunnable = new Runnable() {
        @Override
        public void run() {
            if (session != null) {
                long start = Long.parseLong(session.getStartTime());
                long diff = System.currentTimeMillis() - start;
                int hours = (int) (diff / (1000 * 60 * 60));
                int mins = (int) ((diff / (1000 * 60)) % 60);
                int secs = (int) ((diff / 1000) % 60);
                tvDuration.setText(String.format("%02d:%02d:%02d", hours, mins, secs));
            }
            timerHandler.postDelayed(this, 1000);
        }
    };

    // Pause Timer (Bottom)
    private SettingsManager settingsManager;
    private TextView tvTimerDisplay;
    private SwitchMaterial switchAutoStart;
    private ImageButton btnTimerPlayPause;
    private View cardTimer;
    
    private static final String PREF_TIMER_END = "timer_end_ms";
    private static final String PREF_TIMER_PAUSED_REMAINING = "timer_paused_remaining_ms";
    private static final String PREF_TIMER_IS_RUNNING = "timer_is_running";
    
    private long timerDurationMs;

    private Runnable pauseTimerRunnable = new Runnable() {
        @Override
        public void run() {
            updatePauseTimerUI();
            timerHandler.postDelayed(this, 1000);
        }
    };
    
    private final ActivityResultLauncher<Intent> selectExerciseLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    long exerciseId = result.getData().getLongExtra("EXERCISE_ID", -1);
                    if (exerciseId != -1) {
                        // Normally we would query the Exercise to get name/muscle, 
                        // but since we only get ID here, let's fetch it.
                        com.example.fitnessapp.model.Exercise ex = null;
                        for(com.example.fitnessapp.model.Exercise e : repo.getAllExercises()) {
                            if (e.getId() == exerciseId) { ex = e; break; }
                        }
                        if (ex != null) {
                            int order = adapter != null ? adapter.getItemCount() : 0;
                            SessionExercise se = new SessionExercise(0, session.getId(), exerciseId, ex.getName(), ex.getMuscleGroup(), order);
                            long id = repo.insertSessionExercise(se);
                            loadExercises(); // reload
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_active_workout);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repo = new WorkoutRepository(this);
        session = repo.getActiveSession();

        if (session == null) {
            Toast.makeText(this, "Keine aktive Session gefunden", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        settingsManager = new SettingsManager(this);
        timerDurationMs = settingsManager.getTimerDurationMs();

        if (settingsManager.isKeepScreenOnEnabled()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        tvDuration = findViewById(R.id.tv_workout_duration);
        recycler = findViewById(R.id.recycler_active_exercises);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        
        tvTimerDisplay = findViewById(R.id.tv_timer_display);
        switchAutoStart = findViewById(R.id.switch_timer_autostart);
        btnTimerPlayPause = findViewById(R.id.btn_timer_play_pause);
        cardTimer = findViewById(R.id.card_timer);
        
        switchAutoStart.setChecked(settingsManager.isAutoTimerEnabled());
        switchAutoStart.setOnCheckedChangeListener((btn, isChecked) -> {
            settingsManager.setAutoTimerEnabled(isChecked);
        });

        setupTimerButtons();
        loadExercises();
        setupDragAndDrop();

        findViewById(R.id.btn_add_exercise).setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectExerciseActivity.class);
            selectExerciseLauncher.launch(intent);
        });

        findViewById(R.id.btn_finish_workout).setOnClickListener(v -> {
            new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle("Training beenden?")
                .setPositiveButton("Beenden", (d, w) -> {
            repo.completeSession(session.getId(), String.valueOf(System.currentTimeMillis()));
            getSharedPreferences("workout_timer", Context.MODE_PRIVATE).edit().clear().apply(); // clear timer local state
            finish();
        })
                .setNegativeButton("Abbrechen", null)
                .show();
        });
        
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish(); // Just close activity, leave session active
            }
        });
    }

    private void loadExercises() {
        List<SessionExercise> list = repo.getExercisesForSession(session.getId());
        adapter = new ActiveExerciseAdapter(list, repo, settingsManager, new ActiveExerciseAdapter.OnInteractionListener() {
            @Override
            public void onExerciseDeleted(int position) { }
            @Override
            public void onSetCompleted() {
                if (switchAutoStart.isChecked()) {
                    startPauseTimer();
                }
            }
        });
        recycler.setAdapter(adapter);
    }

    private void setupDragAndDrop() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                adapter.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }
            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                List<SessionExercise> list = adapter.getExercises();
                for (int i = 0; i < list.size(); i++) {
                    SessionExercise se = list.get(i);
                    if (se.getOrderIndex() != i) {
                        se.setOrderIndex(i);
                        repo.updateSessionExerciseOrder(se.getId(), i);
                    }
                }
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(recycler);
    }
    
    // --- Timer Logic ---
    
    private void setupTimerButtons() {
        SharedPreferences localPrefs = getSharedPreferences("workout_timer", Context.MODE_PRIVATE);
        findViewById(R.id.btn_timer_plus).setOnClickListener(v -> adjustTimer(30000));
        findViewById(R.id.btn_timer_minus).setOnClickListener(v -> adjustTimer(-30000));
        
        findViewById(R.id.btn_timer_cancel).setOnClickListener(v -> {
            localPrefs.edit().putBoolean(PREF_TIMER_IS_RUNNING, false)
                       .putLong(PREF_TIMER_PAUSED_REMAINING, timerDurationMs).apply();
            updatePauseTimerUI();
        });
        
        btnTimerPlayPause.setOnClickListener(v -> {
            boolean isRunning = localPrefs.getBoolean(PREF_TIMER_IS_RUNNING, false);
            if (isRunning) {
                long end = localPrefs.getLong(PREF_TIMER_END, 0);
                long remaining = end - System.currentTimeMillis();
                if (remaining < 0) remaining = 0;
                localPrefs.edit().putBoolean(PREF_TIMER_IS_RUNNING, false)
                           .putLong(PREF_TIMER_PAUSED_REMAINING, remaining).apply();
            } else {
                long remaining = localPrefs.getLong(PREF_TIMER_PAUSED_REMAINING, timerDurationMs);
                localPrefs.edit().putBoolean(PREF_TIMER_IS_RUNNING, true)
                           .putLong(PREF_TIMER_END, System.currentTimeMillis() + remaining).apply();
            }
            updatePauseTimerUI();
        });
    }
    
    private void startPauseTimer() {
        SharedPreferences localPrefs = getSharedPreferences("workout_timer", Context.MODE_PRIVATE);
        localPrefs.edit().putBoolean(PREF_TIMER_IS_RUNNING, true)
                   .putLong(PREF_TIMER_END, System.currentTimeMillis() + timerDurationMs).apply();
        updatePauseTimerUI();
    }
    
    private void adjustTimer(long diffMs) {
        SharedPreferences localPrefs = getSharedPreferences("workout_timer", Context.MODE_PRIVATE);
        boolean isRunning = localPrefs.getBoolean(PREF_TIMER_IS_RUNNING, false);
        if (isRunning) {
            long end = localPrefs.getLong(PREF_TIMER_END, 0);
            localPrefs.edit().putLong(PREF_TIMER_END, end + diffMs).apply();
        } else {
            long rem = localPrefs.getLong(PREF_TIMER_PAUSED_REMAINING, timerDurationMs) + diffMs;
            if (rem < 0) rem = 0;
            localPrefs.edit().putLong(PREF_TIMER_PAUSED_REMAINING, rem).apply();
        }
        updatePauseTimerUI();
    }
    
    private void updatePauseTimerUI() {
        SharedPreferences localPrefs = getSharedPreferences("workout_timer", Context.MODE_PRIVATE);
        boolean isRunning = localPrefs.getBoolean(PREF_TIMER_IS_RUNNING, false);
        long remaining = 0;
        
        if (isRunning) {
            long end = localPrefs.getLong(PREF_TIMER_END, 0);
            remaining = end - System.currentTimeMillis();
            if (remaining <= 0) {
                remaining = 0;
                localPrefs.edit().putBoolean(PREF_TIMER_IS_RUNNING, false)
                           .putLong(PREF_TIMER_PAUSED_REMAINING, timerDurationMs).apply();
                isRunning = false;
                triggerTimerVibration();
            }
        } else {
            remaining = localPrefs.getLong(PREF_TIMER_PAUSED_REMAINING, timerDurationMs);
        }
        
        int mins = (int) (remaining / 60000);
        int secs = (int) ((remaining % 60000) / 1000);
        tvTimerDisplay.setText(String.format("%02d:%02d", mins, secs));
        
        btnTimerPlayPause.setImageResource(isRunning ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
    }
    
    private void triggerTimerVibration() {
        if (settingsManager.isVibrateEnabled()) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        timerHandler.post(durationRunnable);
        timerHandler.post(pauseTimerRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(durationRunnable);
        timerHandler.removeCallbacks(pauseTimerRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (repo != null) repo.close();
    }
}