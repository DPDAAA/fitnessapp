package com.example.fitnessapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.adapter.SessionDetailExerciseAdapter;
import com.example.fitnessapp.model.SessionExercise;
import com.example.fitnessapp.model.SessionSummary;
import com.example.fitnessapp.storage.WorkoutRepository;
import com.example.fitnessapp.util.SettingsManager;
import com.example.fitnessapp.util.TimeUtils;

import java.util.List;

public class SessionDetailActivity extends AppCompatActivity {

    private WorkoutRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_session_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        long sessionId = getIntent().getLongExtra("SESSION_ID", -1);
        if (sessionId == -1) {
            finish();
            return;
        }

        repo = new WorkoutRepository(this);
        
        SessionSummary summary = repo.getSessionSummaryById(sessionId);
        if (summary == null) {
            finish();
            return;
        }

        TextView tvDate = findViewById(R.id.tv_detail_date);
        TextView tvTime = findViewById(R.id.tv_detail_time);
        TextView tvDuration = findViewById(R.id.tv_detail_duration);
        TextView tvStats = findViewById(R.id.tv_detail_stats);
        
        long startMs = TimeUtils.parseTimeMs(summary.getStartTime());
        long endMs = TimeUtils.parseTimeMs(summary.getEndTime());
        
        tvDate.setText(TimeUtils.formatDate(startMs));
        
        String timeStr = TimeUtils.formatTime(startMs);
        if (endMs > 0) {
            timeStr += " - " + TimeUtils.formatTime(endMs);
        }
        tvTime.setText(timeStr);
        tvDuration.setText(TimeUtils.formatDuration(startMs, endMs));
        
        SettingsManager settingsManager = new SettingsManager(this);
            
        tvStats.setText(
            summary.getExerciseCount() + " Übungen • " + 
            summary.getCompletedSets() + " Sätze • " + 
            settingsManager.formatWeightDisplay(summary.getTotalVolume())
        );

        RecyclerView recycler = findViewById(R.id.recycler_detail_exercises);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        
        List<SessionExercise> exercises = repo.getExercisesForSession(sessionId);
        SessionDetailExerciseAdapter adapter = new SessionDetailExerciseAdapter(exercises, repo, settingsManager);
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (repo != null) repo.close();
    }
}