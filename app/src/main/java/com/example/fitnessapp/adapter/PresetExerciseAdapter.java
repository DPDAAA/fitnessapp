package com.example.fitnessapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnessapp.R;
import com.example.fitnessapp.model.PresetExercise;
import java.util.List;

public class PresetExerciseAdapter extends RecyclerView.Adapter<PresetExerciseAdapter.ViewHolder> {

    private List<PresetExercise> exercises;
    private OnItemInteractionListener listener;

    public interface OnItemInteractionListener {
        void onDeleteClick(PresetExercise exercise, int position);
    }

    public PresetExerciseAdapter(List<PresetExercise> exercises, OnItemInteractionListener listener) {
        this.exercises = exercises;
        this.listener = listener;
    }

    public List<PresetExercise> getExercises() {
        return exercises;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_preset_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PresetExercise pe = exercises.get(position);
        holder.tvName.setText(pe.getName());
        holder.tvMuscle.setText(pe.getMuscleGroup());
        holder.btnDelete.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                listener.onDeleteClick(pe, currentPos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public void removeExercise(int position) {
        exercises.remove(position);
        notifyItemRemoved(position);
    }
    
    public void swap(int fromPosition, int toPosition) {
        PresetExercise temp = exercises.remove(fromPosition);
        exercises.add(toPosition, temp);
        notifyItemMoved(fromPosition, toPosition);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMuscle;
        ImageButton btnDelete;
        ImageView imgDrag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_exercise_name);
            tvMuscle = itemView.findViewById(R.id.tv_muscle_group);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            imgDrag = itemView.findViewById(R.id.img_drag);
        }
    }
}