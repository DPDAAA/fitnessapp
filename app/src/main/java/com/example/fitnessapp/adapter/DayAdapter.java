package com.example.fitnessapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnessapp.R;
import com.example.fitnessapp.model.TrainingDay;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.List;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {

    private List<TrainingDay> days;
    private OnDayClickListener listener;
    private long selectedDayId = -1;

    public interface OnDayClickListener {
        void onDayClick(TrainingDay day);
        void onRestToggled(TrainingDay day, boolean isRest);
    }

    public DayAdapter(List<TrainingDay> days, OnDayClickListener listener) {
        this.days = days;
        this.listener = listener;
        if (!days.isEmpty()) {
            selectedDayId = days.get(0).getId();
        }
    }

    public long getSelectedDayId() {
        return selectedDayId;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_training_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        TrainingDay day = days.get(position);
        holder.tvDayName.setText(day.getDayName());
        holder.switchRest.setOnCheckedChangeListener(null);
        holder.switchRest.setChecked(day.isRest());

        if (day.getId() == selectedDayId) {
            holder.cardView.setStrokeWidth(4);
            holder.cardView.setStrokeColor(Color.parseColor("#B71C1C")); // dark_red_light
        } else {
            holder.cardView.setStrokeWidth(0);
        }

        holder.switchRest.setOnCheckedChangeListener((buttonView, isChecked) -> {
            day.setRest(isChecked);
            listener.onRestToggled(day, isChecked);
        });

        holder.itemView.setOnClickListener(v -> {
            selectedDayId = day.getId();
            notifyDataSetChanged();
            listener.onDayClick(day);
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvDayName;
        SwitchMaterial switchRest;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_day);
            tvDayName = itemView.findViewById(R.id.tv_day_name);
            switchRest = itemView.findViewById(R.id.switch_rest);
        }
    }
}