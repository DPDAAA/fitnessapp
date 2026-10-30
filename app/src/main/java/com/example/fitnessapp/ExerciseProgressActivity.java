package com.example.fitnessapp;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.adapter.ExerciseProgressAdapter;
import com.example.fitnessapp.model.ExerciseProgressEntry;
import com.example.fitnessapp.storage.WorkoutRepository;
import com.example.fitnessapp.util.SettingsManager;
import com.example.fitnessapp.util.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExerciseProgressActivity extends AppCompatActivity {

    private WorkoutRepository repo;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_exercise_progress);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String exerciseName = getIntent().getStringExtra("EXERCISE_NAME");
        if (exerciseName == null) {
            finish();
            return;
        }

        TextView tvTitle = findViewById(R.id.tv_exercise_title);
        tvTitle.setText(exerciseName);

        repo = new WorkoutRepository(this);
        List<ExerciseProgressEntry> entries = repo.getExerciseProgress(exerciseName);
        
        SettingsManager settingsManager = new SettingsManager(this);

        RecyclerView recycler = findViewById(R.id.recycler_progress_entries);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(new ExerciseProgressAdapter(entries, settingsManager));
        
        setupChart(entries, settingsManager);
    }
    
    private void setupChart(List<ExerciseProgressEntry> entries, SettingsManager settingsManager) {
        LinearLayout chartContainer = findViewById(R.id.chart_container);
        LinearLayout chartLabelsContainer = findViewById(R.id.chart_labels_container);
        TextView tvChartEmpty = findViewById(R.id.tv_chart_empty);
        
        if (entries.isEmpty()) {
            chartContainer.setVisibility(View.GONE);
            chartLabelsContainer.setVisibility(View.GONE);
            tvChartEmpty.setVisibility(View.VISIBLE);
            return;
        }
        
        // Take up to 10 most recent entries for the chart, reverse to chronological ASC
        List<ExerciseProgressEntry> chartEntries = new ArrayList<>();
        int count = Math.min(entries.size(), 10);
        for (int i = 0; i < count; i++) {
            chartEntries.add(entries.get(i));
        }
        Collections.reverse(chartEntries);
        
        double maxWeight = 0;
        for (ExerciseProgressEntry e : chartEntries) {
            if (e.getAvgWeight() > maxWeight) maxWeight = e.getAvgWeight();
        }
        
        if (maxWeight == 0) {
            chartContainer.setVisibility(View.GONE);
            chartLabelsContainer.setVisibility(View.GONE);
            tvChartEmpty.setVisibility(View.VISIBLE);
            return;
        }
        
        chartContainer.setVisibility(View.VISIBLE);
        chartLabelsContainer.setVisibility(View.VISIBLE);
        tvChartEmpty.setVisibility(View.GONE);
        
        chartContainer.setWeightSum(chartEntries.size());
        chartLabelsContainer.setWeightSum(chartEntries.size());
        
        for (ExerciseProgressEntry e : chartEntries) {
            // Draw Bar
            LinearLayout.LayoutParams wrapParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
            
            LinearLayout column = new LinearLayout(this);
            column.setOrientation(LinearLayout.VERTICAL);
            column.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            column.setLayoutParams(wrapParams);
            
            View bar = new View(this);
            bar.setBackgroundColor(getResources().getColor(R.color.dark_red, null));
            int heightPercent = (int) ((e.getAvgWeight() / maxWeight) * 100);
            if (heightPercent < 1 && e.getAvgWeight() > 0) heightPercent = 1;
            
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                (int) (getResources().getDisplayMetrics().density * 24),
                0, 
                heightPercent / 100f
            );
            bar.setLayoutParams(barParams);
            
            column.setOnClickListener(v -> {
                Toast.makeText(this, TimeUtils.formatDate(e.getStartTimeMs()) + ": Ø " + settingsManager.formatWeightDisplay(e.getAvgWeight()), Toast.LENGTH_SHORT).show();
            });
            
            column.addView(bar);
            chartContainer.addView(column);
            
            // Draw Label
            TextView label = new TextView(this);
            label.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            label.setText(TimeUtils.formatDate(e.getStartTimeMs()).substring(0, 5)); // Just DD.MM
            label.setTextSize(10f);
            label.setTextColor(getResources().getColor(R.color.text_secondary, null));
            label.setGravity(Gravity.CENTER);
            label.setMaxLines(1);
            chartLabelsContainer.addView(label);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (repo != null) repo.close();
    }
}