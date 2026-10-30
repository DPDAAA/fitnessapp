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
import com.example.fitnessapp.model.WeeklyVolume;
import com.example.fitnessapp.storage.WorkoutRepository;
import com.example.fitnessapp.util.SettingsManager;
import com.example.fitnessapp.util.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatsVolumeFragment extends Fragment {

    private WorkoutRepository repo;
    private SettingsManager settingsManager;
    private LinearLayout chartContainer, chartLabelsContainer;
    private TextView tvChartEmpty;
    private RecyclerView recycler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats_volume, container, false);
        repo = new WorkoutRepository(requireContext());
        settingsManager = new SettingsManager(requireContext());
        
        chartContainer = view.findViewById(R.id.chart_container);
        chartLabelsContainer = view.findViewById(R.id.chart_labels_container);
        tvChartEmpty = view.findViewById(R.id.tv_chart_empty);
        
        recycler = view.findViewById(R.id.recycler_volume_weeks);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
    
    private void loadData() {
        long[] weekStarts = TimeUtils.getLast7WeeksStartMs();
        String[] weekLabels = TimeUtils.getLast7WeeksLabels(weekStarts);
        List<WeeklyVolume> volumes = repo.getWeeklyVolumesAdvanced(weekStarts, weekLabels);
        
        drawChart(volumes);
        
        // Reverse for the list so newest is on top
        List<WeeklyVolume> reversedList = new ArrayList<>(volumes);
        Collections.reverse(reversedList);
        recycler.setAdapter(new VolumeAdapter(reversedList));
    }
    
    private void drawChart(List<WeeklyVolume> volumes) {
        chartContainer.removeAllViews();
        chartLabelsContainer.removeAllViews();
        
        double maxVolume = 0;
        for (WeeklyVolume wv : volumes) {
            if (wv.getVolume() > maxVolume) maxVolume = wv.getVolume();
        }
        
        if (maxVolume == 0) {
            chartContainer.setVisibility(View.GONE);
            chartLabelsContainer.setVisibility(View.GONE);
            tvChartEmpty.setVisibility(View.VISIBLE);
            return;
        }
        
        chartContainer.setVisibility(View.VISIBLE);
        chartLabelsContainer.setVisibility(View.VISIBLE);
        tvChartEmpty.setVisibility(View.GONE);
        
        for (WeeklyVolume wv : volumes) {
            LinearLayout.LayoutParams wrapParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
            
            LinearLayout column = new LinearLayout(getContext());
            column.setOrientation(LinearLayout.VERTICAL);
            column.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            column.setLayoutParams(wrapParams);
            
            View bar = new View(getContext());
            bar.setBackgroundColor(getResources().getColor(R.color.dark_red, null));
            int heightPercent = (int) ((wv.getVolume() / maxVolume) * 100);
            if (heightPercent < 1 && wv.getVolume() > 0) heightPercent = 1;
            
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                (int) (getResources().getDisplayMetrics().density * 24),
                0, 
                heightPercent / 100f
            );
            bar.setLayoutParams(barParams);
            
            column.setOnClickListener(v -> {
                Toast.makeText(getContext(), wv.getWeekLabel() + ": " + settingsManager.formatWeightDisplay(wv.getVolume()), Toast.LENGTH_SHORT).show();
            });
            
            column.addView(bar);
            chartContainer.addView(column);
            
            TextView label = new TextView(getContext());
            label.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            label.setText(wv.getWeekLabel());
            label.setTextSize(10f);
            label.setTextColor(getResources().getColor(R.color.text_secondary, null));
            label.setGravity(Gravity.CENTER);
            label.setMaxLines(1);
            chartLabelsContainer.addView(label);
        }
    }

    private class VolumeAdapter extends RecyclerView.Adapter<VolumeAdapter.ViewHolder> {
        private List<WeeklyVolume> items;

        VolumeAdapter(List<WeeklyVolume> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stats_weekly, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WeeklyVolume wv = items.get(position);
            holder.tvLabel.setText(wv.getWeekLabel());
            holder.tvVolume.setText(settingsManager.formatWeightDisplay(wv.getVolume()));
            holder.tvDetails.setText(wv.getWorkouts() + " Workouts • " + wv.getSets() + " Sätze");
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvLabel, tvVolume, tvDetails;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvLabel = itemView.findViewById(R.id.tv_week_label);
                tvVolume = itemView.findViewById(R.id.tv_week_volume);
                tvDetails = itemView.findViewById(R.id.tv_week_details);
            }
        }
    }
}