package com.example.fitnessapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnessapp.R;
import com.example.fitnessapp.model.PlanExercise;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private List<PlanExercise> exercises;
    private OnExerciseDeleteListener deleteListener;

    public interface OnExerciseDeleteListener {
        void onDeleteClick(PlanExercise exercise, int position);
    }

    public ExerciseAdapter(List<PlanExercise> exercises, OnExerciseDeleteListener deleteListener) {
        this.exercises = exercises;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plan_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        PlanExercise exercise = exercises.get(position);
        holder.tvName.setText(exercise.getName());
        holder.tvMuscle.setText(exercise.getMuscleGroup());
        holder.tvSetsReps.setText(exercise.getSets() + " Sets x " + exercise.getReps() + " Reps");
        holder.tvWeight.setText(exercise.getWeight() + " kg");

        holder.btnDelete.setOnClickListener(v -> deleteListener.onDeleteClick(exercise, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public void removeExercise(int position) {
        exercises.remove(position);
        notifyItemRemoved(position);
    }
    
    public void addExercise(PlanExercise exercise) {
        exercises.add(exercise);
        notifyItemInserted(exercises.size() - 1);
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMuscle, tvSetsReps, tvWeight;
        ImageButton btnDelete;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_exercise_name);
            tvMuscle = itemView.findViewById(R.id.tv_muscle_group);
            tvSetsReps = itemView.findViewById(R.id.tv_sets_reps);
            tvWeight = itemView.findViewById(R.id.tv_weight);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}