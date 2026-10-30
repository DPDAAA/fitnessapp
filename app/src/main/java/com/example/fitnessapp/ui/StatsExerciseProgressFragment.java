package com.example.fitnessapp.ui;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.ExerciseProgressAdapter;
import com.example.fitnessapp.model.ExerciseProgressEntry;
import com.example.fitnessapp.storage.WorkoutRepository;
import com.example.fitnessapp.util.SettingsManager;
import com.example.fitnessapp.util.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatsExerciseProgressFragment extends Fragment {

    private WorkoutRepository repo;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats_exercise_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        repo = new WorkoutRepository(requireContext());
        
        Bundle args = getArguments();
        if (args == null) return;
        
        String exName = args.getString("EXERCISE_NAME");
        Long exId = args.containsKey("EXERCISE_ID") ? args.getLong("EXERCISE_ID") : null;
        
        TextView tvTitle = view.findViewById(R.id.tv_exercise_title);
        tvTitle.setText(exName);
        
        List<ExerciseProgressEntry> entries = repo.getExerciseProgressAdvanced(exId, exName);
        SettingsManager settingsManager = new SettingsManager(requireContext());
        
        RecyclerView recycler = view.findViewById(R.id.recycler_progress_entries);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(new ExerciseProgressAdapter(entries, settingsManager));
        
        setupChart(view, entries, settingsManager);
    }
    
    private void setupChart(View view, List<ExerciseProgressEntry> entries, SettingsManager settingsManager) {
        LinearLayout chartContainer = view.findViewById(R.id.chart_container);
        LinearLayout chartLabelsContainer = view.findViewById(R.id.chart_labels_container);
        TextView tvChartEmpty = view.findViewById(R.id.tv_chart_empty);
        
        if (entries.isEmpty()) {
            chartContainer.setVisibility(View.GONE);
            chartLabelsContainer.setVisibility(View.GONE);
            tvChartEmpty.setVisibility(View.VISIBLE);
            return;
        }
        
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
            LinearLayout.LayoutParams wrapParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
            
            LinearLayout column = new LinearLayout(requireContext());
            column.setOrientation(LinearLayout.VERTICAL);
            column.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            column.setLayoutParams(wrapParams);
            
            View bar = new View(requireContext());
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
                Toast.makeText(requireContext(), TimeUtils.formatDate(e.getStartTimeMs()) + ": Ø " + settingsManager.formatWeightDisplay(e.getAvgWeight()), Toast.LENGTH_SHORT).show();
            });
            
            column.addView(bar);
            chartContainer.addView(column);
            
            TextView label = new TextView(requireContext());
            label.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            label.setText(TimeUtils.formatDate(e.getStartTimeMs()).substring(0, 5));
            label.setTextSize(10f);
            label.setTextColor(getResources().getColor(R.color.text_secondary, null));
            label.setGravity(Gravity.CENTER);
            label.setMaxLines(1);
            chartLabelsContainer.addView(label);
        }
    }
}