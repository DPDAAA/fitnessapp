package com.example.fitnessapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.fitnessapp.R;
import com.example.fitnessapp.model.StatsOverview;
import com.example.fitnessapp.storage.WorkoutRepository;
import com.example.fitnessapp.util.SettingsManager;
import com.example.fitnessapp.util.TimeUtils;

public class StatsFragment extends Fragment {
    
    private WorkoutRepository repo;
    private SettingsManager settingsManager;

    private TextView tvWorkouts, tv7d, tvTotalVol, tvWeekVol, tvAvgSets, tvAvgDur;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats_overview, container, false);
        repo = new WorkoutRepository(requireContext());
        settingsManager = new SettingsManager(requireContext());
        
        tvWorkouts = view.findViewById(R.id.tv_overview_workouts);
        tv7d = view.findViewById(R.id.tv_overview_7d);
        tvTotalVol = view.findViewById(R.id.tv_overview_total_vol);
        tvWeekVol = view.findViewById(R.id.tv_overview_week_vol);
        tvAvgSets = view.findViewById(R.id.tv_overview_avg_sets);
        tvAvgDur = view.findViewById(R.id.tv_overview_avg_dur);

        view.findViewById(R.id.btn_nav_volume).setOnClickListener(v -> navigateTo(new StatsVolumeFragment()));
        view.findViewById(R.id.btn_nav_muscles).setOnClickListener(v -> navigateTo(new StatsMuscleFragment()));
        view.findViewById(R.id.btn_nav_progress).setOnClickListener(v -> navigateTo(new StatsProgressSelectionFragment()));
        view.findViewById(R.id.btn_nav_history).setOnClickListener(v -> navigateTo(new StatsHistoryFragment()));
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOverviewStats();
    }

    private void loadOverviewStats() {
        long start7d = TimeUtils.getStartOfLast7DaysMs();
        long startWeek = TimeUtils.getStartOfCurrentWeekMs();
        
        StatsOverview stats = repo.getAdvancedStatsOverview(start7d, startWeek);
        
        tvWorkouts.setText(String.valueOf(stats.getTotalWorkouts()));
        tv7d.setText(String.valueOf(stats.getWorkoutsLast7Days()));
        tvTotalVol.setText(settingsManager.formatWeightDisplay(stats.getTotalVolume()));
        tvWeekVol.setText(settingsManager.formatWeightDisplay(stats.getCurrentWeekVolume()));
        tvAvgSets.setText(String.format("%.1f", stats.getAvgSets()));
        tvAvgDur.setText(TimeUtils.formatDurationMs(stats.getAvgDurationMs()));
    }

    private void navigateTo(Fragment fragment) {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}