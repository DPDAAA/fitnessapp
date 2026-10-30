package com.example.fitnessapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.model.MuscleGroupStat;
import com.example.fitnessapp.storage.WorkoutRepository;
import com.example.fitnessapp.util.SettingsManager;

import java.util.List;

public class StatsMuscleFragment extends Fragment {

    private WorkoutRepository repo;
    private SettingsManager settingsManager;
    private RecyclerView recycler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats_muscle, container, false);
        repo = new WorkoutRepository(requireContext());
        settingsManager = new SettingsManager(requireContext());
        recycler = view.findViewById(R.id.recycler_muscle_stats);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        List<MuscleGroupStat> stats = repo.getMuscleGroupStats();
        recycler.setAdapter(new MuscleAdapter(stats));
    }

    private class MuscleAdapter extends RecyclerView.Adapter<MuscleAdapter.ViewHolder> {
        private List<MuscleGroupStat> items;

        MuscleAdapter(List<MuscleGroupStat> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stats_muscle, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MuscleGroupStat stat = items.get(position);
            holder.tvName.setText(stat.getMuscleGroup());
            
            holder.tvVolume.setText(settingsManager.formatWeightDisplay(stat.getVolume()));
            
            int percent = (int) Math.round(stat.getVolumePercentage());
            holder.tvPercentage.setText(percent + "%");
            holder.progressBar.setProgress(percent);
            
            holder.tvStats.setText(stat.getWorkouts() + " Workouts • " + stat.getSets() + " Sätze");
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvVolume, tvPercentage, tvStats;
            ProgressBar progressBar;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_muscle_name);
                tvVolume = itemView.findViewById(R.id.tv_muscle_volume);
                tvPercentage = itemView.findViewById(R.id.tv_muscle_percentage);
                tvStats = itemView.findViewById(R.id.tv_muscle_stats);
                progressBar = itemView.findViewById(R.id.progress_volume);
            }
        }
    }
}