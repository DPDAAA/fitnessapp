package com.example.fitnessapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnessapp.R;
import com.example.fitnessapp.model.CalendarDay;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    private List<CalendarDay> days;
    private OnDayClickListener listener;

    public interface OnDayClickListener {
        void onDayClick(CalendarDay day);
    }

    public CalendarAdapter(List<CalendarDay> days, OnDayClickListener listener) {
        this.days = days;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarDay day = days.get(position);
        
        if (day.getDay() == 0 || !day.isCurrentMonth()) {
            holder.tvDayNumber.setText("");
            holder.cardDay.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.transparent));
            holder.itemView.setOnClickListener(null);
            return;
        }

        holder.tvDayNumber.setText(String.valueOf(day.getDay()));
        
        if (day.getSessions() != null && !day.getSessions().isEmpty()) {
            // Training day -> green
            holder.cardDay.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.dark_red_light)); // Using red for brand consistency, or green if preferred. Given instructions asked for "Grün", let's use a green color. Wait, instructions said "Grün". Let's add a green color dynamically or use standard. Let's use a nice dark green.
            holder.cardDay.setCardBackgroundColor(android.graphics.Color.parseColor("#2E7D32")); // Material Green 800
        } else {
            // No training -> gray
            holder.cardDay.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black_surface_light));
        }

        holder.itemView.setOnClickListener(v -> listener.onDayClick(day));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }
    
    public void updateData(List<CalendarDay> newDays) {
        this.days = newDays;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardDay;
        TextView tvDayNumber;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardDay = itemView.findViewById(R.id.card_day);
            tvDayNumber = itemView.findViewById(R.id.tv_day_number);
        }
    }
}