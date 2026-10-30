package com.example.fitnessapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.SessionDetailActivity;
import com.example.fitnessapp.adapter.CalendarAdapter;
import com.example.fitnessapp.model.CalendarDay;
import com.example.fitnessapp.model.SessionSummary;
import com.example.fitnessapp.storage.WorkoutRepository;
import com.example.fitnessapp.util.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StatsHistoryFragment extends Fragment {
    
    private WorkoutRepository repo;
    private RecyclerView recycler;
    private CalendarAdapter adapter;
    private TextView tvMonthYear;
    private Calendar currentDisplayMonth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats_history, container, false);
        
        repo = new WorkoutRepository(requireContext());
        recycler = view.findViewById(R.id.recycler_calendar);
        tvMonthYear = view.findViewById(R.id.tv_month_year);
        
        // 7 columns for 7 days a week
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 7));
        
        currentDisplayMonth = Calendar.getInstance();
        currentDisplayMonth.set(Calendar.DAY_OF_MONTH, 1);
        currentDisplayMonth.set(Calendar.HOUR_OF_DAY, 0);
        currentDisplayMonth.set(Calendar.MINUTE, 0);
        currentDisplayMonth.set(Calendar.SECOND, 0);
        currentDisplayMonth.set(Calendar.MILLISECOND, 0);
        
        view.findViewById(R.id.btn_prev_month).setOnClickListener(v -> {
            currentDisplayMonth.add(Calendar.MONTH, -1);
            loadCalendar();
        });
        
        view.findViewById(R.id.btn_next_month).setOnClickListener(v -> {
            currentDisplayMonth.add(Calendar.MONTH, 1);
            loadCalendar();
        });
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCalendar();
    }

    private void loadCalendar() {
        updateMonthLabel();
        
        // Find start and end timestamps for the query
        long startOfMonthMs = currentDisplayMonth.getTimeInMillis();
        
        Calendar endCal = (Calendar) currentDisplayMonth.clone();
        endCal.add(Calendar.MONTH, 1);
        long endOfMonthMs = endCal.getTimeInMillis();
        
        // Fetch all completed sessions for this month from DB (using new efficient method)
        List<SessionSummary> monthSessions = repo.getCompletedSessionsForTimeRange(startOfMonthMs, endOfMonthMs);
        
        List<CalendarDay> calendarDays = buildCalendarGrid(currentDisplayMonth, monthSessions);
        
        if (adapter == null) {
            adapter = new CalendarAdapter(calendarDays, this::onDayClicked);
            recycler.setAdapter(adapter);
        } else {
            adapter.updateData(calendarDays);
        }
    }
    
    private void updateMonthLabel() {
        String[] months = {"Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"};
        int month = currentDisplayMonth.get(Calendar.MONTH);
        int year = currentDisplayMonth.get(Calendar.YEAR);
        tvMonthYear.setText(months[month] + " " + year);
    }
    
    private List<CalendarDay> buildCalendarGrid(Calendar monthStart, List<SessionSummary> sessions) {
        List<CalendarDay> days = new ArrayList<>();
        
        Calendar cal = (Calendar) monthStart.clone();
        
        // Find what day of week the 1st is (1=Sunday, 2=Monday, ..., 7=Saturday)
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        
        // Convert to German logic (1=Monday, ..., 7=Sunday)
        int offset = firstDayOfWeek - 2; 
        if (offset < 0) offset += 7;
        
        // Add empty days for previous month
        for (int i = 0; i < offset; i++) {
            days.add(new CalendarDay(0, false, new ArrayList<>()));
        }
        
        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        for (int day = 1; day <= maxDays; day++) {
            // Check if any sessions fall on this day
            List<SessionSummary> daySessions = new ArrayList<>();
            for (SessionSummary s : sessions) {
                Calendar sCal = Calendar.getInstance();
                sCal.setTimeInMillis(TimeUtils.parseTimeMs(s.getStartTime()));
                if (sCal.get(Calendar.DAY_OF_MONTH) == day) {
                    daySessions.add(s);
                }
            }
            days.add(new CalendarDay(day, true, daySessions));
        }
        
        // Fill remaining days of the last week to complete the grid (optional, but looks cleaner)
        while (days.size() % 7 != 0) {
            days.add(new CalendarDay(0, false, new ArrayList<>()));
        }
        
        return days;
    }
    
    private void onDayClicked(CalendarDay day) {
        if (!day.isCurrentMonth() || day.getSessions().isEmpty()) return;
        
        if (day.getSessions().size() == 1) {
            openSessionDetail(day.getSessions().get(0).getSessionId());
        } else {
            // Open Dialog with multiple sessions
            MultiSessionDialog dialog = new MultiSessionDialog(requireContext(), day.getSessions(), this::openSessionDetail);
            dialog.show();
        }
    }
    
    private void openSessionDetail(long sessionId) {
        Intent intent = new Intent(getContext(), SessionDetailActivity.class);
        intent.putExtra("SESSION_ID", sessionId);
        startActivity(intent);
    }
}