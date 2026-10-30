package com.example.fitnessapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnessapp.R;
import java.util.List;

public class TrainedExerciseAdapter extends RecyclerView.Adapter<TrainedExerciseAdapter.ViewHolder> {

    private List<String> exercises;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String exerciseName);
    }

    public TrainedExerciseAdapter(List<String> exercises, OnItemClickListener listener) {
        this.exercises = exercises;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trained_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = exercises.get(position);
        holder.tvName.setText(name);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(name));
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }
}