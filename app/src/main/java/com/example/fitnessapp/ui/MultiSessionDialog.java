package com.example.fitnessapp.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.model.SessionSummary;
import com.example.fitnessapp.util.TimeUtils;

import java.util.List;

public class MultiSessionDialog extends Dialog {

    public interface OnSessionSelectListener {
        void onSessionSelected(long sessionId);
    }

    public MultiSessionDialog(@NonNull Context context, List<SessionSummary> sessions, OnSessionSelectListener listener) {
        super(context);
        setContentView(R.layout.dialog_multi_session);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        RecyclerView recycler = findViewById(R.id.recycler_multi_sessions);
        recycler.setLayoutManager(new LinearLayoutManager(context));
        
        recycler.setAdapter(new RecyclerView.Adapter<ViewHolder>() {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
                return new ViewHolder(v);
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                SessionSummary s = sessions.get(position);
                long startMs = TimeUtils.parseTimeMs(s.getStartTime());
                holder.tv.setText("Training um " + TimeUtils.formatTime(startMs) + " Uhr (" + s.getExerciseCount() + " Übungen)");
                holder.tv.setTextColor(context.getResources().getColor(R.color.text_primary, null));
                holder.itemView.setOnClickListener(v -> {
                    listener.onSessionSelected(s.getSessionId());
                    dismiss();
                });
            }

            @Override
            public int getItemCount() {
                return sessions.size();
            }
        });

        findViewById(R.id.btn_close).setOnClickListener(v -> dismiss());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(android.R.id.text1);
        }
    }
}