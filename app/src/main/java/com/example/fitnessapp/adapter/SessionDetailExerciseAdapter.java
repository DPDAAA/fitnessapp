package com.example.fitnessapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnessapp.R;
import com.example.fitnessapp.model.SessionExercise;
import com.example.fitnessapp.model.WorkoutSet;
import com.example.fitnessapp.storage.WorkoutRepository;
import com.example.fitnessapp.util.SettingsManager;
import java.util.List;

public class SessionDetailExerciseAdapter extends RecyclerView.Adapter<SessionDetailExerciseAdapter.ViewHolder> {

    private List<SessionExercise> exercises;
    private WorkoutRepository repo;
    private SettingsManager settingsManager;

    public SessionDetailExerciseAdapter(List<SessionExercise> exercises, WorkoutRepository repo, SettingsManager settingsManager) {
        this.exercises = exercises;
        this.repo = repo;
        this.settingsManager = settingsManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session_detail_exercise, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SessionExercise exercise = exercises.get(position);
        
        holder.tvName.setText(exercise.getName());
        holder.tvMuscle.setText(exercise.getMuscleGroup());
        
        holder.setsContainer.removeAllViews();
        
        List<WorkoutSet> sets = repo.getSetsForSessionExercise(exercise.getId());
        for (WorkoutSet set : sets) {
            View setView = LayoutInflater.from(holder.setsContainer.getContext())
                    .inflate(R.layout.item_session_detail_set, holder.setsContainer, false);
            
            TextView tvNum = setView.findViewById(R.id.tv_set_number);
            TextView tvWeight = setView.findViewById(R.id.tv_weight);
            TextView tvReps = setView.findViewById(R.id.tv_reps);
            ImageView imgCheck = setView.findViewById(R.id.img_check_indicator);
            
            tvNum.setText(String.valueOf(set.getSetNumber()));
            
            String wStr = set.getWeight() != null ? settingsManager.formatWeightDisplay(set.getWeight()) : "-";
            String rStr = set.getReps() != null ? String.valueOf(set.getReps()) : "-";
            
            tvWeight.setText(wStr);
            tvReps.setText(rStr + " Reps");
            
            if (set.isCompleted()) {
                imgCheck.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.dark_red, null));
            } else {
                imgCheck.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.text_secondary, null));
                tvWeight.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_secondary, null));
                tvReps.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_secondary, null));
            }
            
            holder.setsContainer.addView(setView);
        }
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMuscle;
        LinearLayout setsContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_exercise_name);
            tvMuscle = itemView.findViewById(R.id.tv_muscle_group);
            setsContainer = itemView.findViewById(R.id.layout_sets_container);
        }
    }
}