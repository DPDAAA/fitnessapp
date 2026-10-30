package com.example.fitnessapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnessapp.R;
import com.example.fitnessapp.model.WorkoutPreset;
import java.util.List;

public class PresetAdapter extends RecyclerView.Adapter<PresetAdapter.PresetViewHolder> {

    private List<WorkoutPreset> presets;
    private OnPresetClickListener listener;

    public interface OnPresetClickListener {
        void onPresetClick(WorkoutPreset preset);
    }

    public PresetAdapter(List<WorkoutPreset> presets, OnPresetClickListener listener) {
        this.presets = presets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PresetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_preset, parent, false);
        return new PresetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PresetViewHolder holder, int position) {
        WorkoutPreset preset = presets.get(position);
        holder.tvName.setText(preset.getName());
        holder.itemView.setOnClickListener(v -> listener.onPresetClick(preset));
    }

    @Override
    public int getItemCount() {
        return presets.size();
    }

    static class PresetViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        public PresetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_preset_name);
        }
    }
}