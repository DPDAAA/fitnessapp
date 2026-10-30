package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.adapter.DayAdapter;
import com.example.fitnessapp.model.TrainingDay;
import com.example.fitnessapp.storage.WorkoutRepository;

import java.util.List;

public class SelectDayActivity extends AppCompatActivity {

    private WorkoutRepository repository;
    private List<TrainingDay> days;
    private DayAdapter adapter;
    private RecyclerView recyclerDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_day);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = new WorkoutRepository(this);
        recyclerDays = findViewById(R.id.recycler_days);
        recyclerDays.setLayoutManager(new LinearLayoutManager(this));

        loadDays();

        Button btnViewRoutine = findViewById(R.id.btn_view_routine);
        btnViewRoutine.setOnClickListener(v -> {
            if (adapter != null && adapter.getSelectedDayId() != -1) {
                Intent intent = new Intent(SelectDayActivity.this, WorkoutOverviewActivity.class);
                intent.putExtra("DAY_ID", adapter.getSelectedDayId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please select a day", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDays() {
        days = repository.getAllDays();
        adapter = new DayAdapter(days, new DayAdapter.OnDayClickListener() {
            @Override
            public void onDayClick(TrainingDay day) {
                // visual selection handled in adapter
            }

            @Override
            public void onRestToggled(TrainingDay day, boolean isRest) {
                repository.updateTrainingDay(day);
            }
        });
        recyclerDays.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        repository.close();
    }
}