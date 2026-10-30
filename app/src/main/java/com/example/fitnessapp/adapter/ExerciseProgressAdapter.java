package com.example.fitnessapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnessapp.R;
import com.example.fitnessapp.model.ExerciseProgressEntry;
import com.example.fitnessapp.model.WorkoutSet;
import com.example.fitnessapp.util.SettingsManager;
import com.example.fitnessapp.util.TimeUtils;
import java.util.List;

public class ExerciseProgressAdapter extends RecyclerView.Adapter<ExerciseProgressAdapter.ViewHolder> {

    private List<ExerciseProgressEntry> entries;
    private SettingsManager settingsManager;

    public ExerciseProgressAdapter(List<ExerciseProgressEntry> entries, SettingsManager settingsManager) {
        this.entries = entries;
        this.settingsManager = settingsManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_progress_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExerciseProgressEntry entry = entries.get(position);
        holder.tvDate.setText(TimeUtils.formatDate(entry.getStartTimeMs()));
        
        holder.setsContainer.removeAllViews();
        List<WorkoutSet> sets = entry.getCompletedSets();
        
        for (WorkoutSet set : sets) {
            View setView = LayoutInflater.from(holder.setsContainer.getContext())
                    .inflate(R.layout.item_session_detail_set, holder.setsContainer, false);
            
            TextView tvNum = setView.findViewById(R.id.tv_set_number);
            TextView tvWeight = setView.findViewById(R.id.tv_weight);
            TextView tvReps = setView.findViewById(R.id.tv_reps);
            setView.findViewById(R.id.img_check_indicator).setVisibility(View.GONE); // No check needed here
            
            tvNum.setText(String.valueOf(set.getSetNumber()));
            
            String wStr = set.getWeight() != null ? settingsManager.formatWeightDisplay(set.getWeight()) : "-";
            String rStr = set.getReps() != null ? String.valueOf(set.getReps()) : "-";
            
            tvWeight.setText(wStr);
            tvReps.setText(rStr + " Reps");
            
            holder.setsContainer.addView(setView);
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        LinearLayout setsContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            setsContainer = itemView.findViewById(R.id.layout_sets_container);
        }
    }
}