package com.example.fitnessapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.fitnessapp.R;
import com.example.fitnessapp.ActiveWorkoutActivity;
import com.example.fitnessapp.storage.WorkoutRepository;
import com.example.fitnessapp.model.Session;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment {
    
    private WorkoutRepository repo;
    private View cardActive;
    private Button btnStartNew;
    private TextView tvLastDate, tvLastStats;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        repo = new WorkoutRepository(requireContext());
        
        cardActive = view.findViewById(R.id.card_active_session);
        btnStartNew = view.findViewById(R.id.btn_start_new);
        tvLastDate = view.findViewById(R.id.tv_last_workout_date);
        tvLastStats = view.findViewById(R.id.tv_last_workout_stats);

        btnStartNew.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
                if(nav != null) nav.setSelectedItemId(R.id.nav_training);
            }
        });

        view.findViewById(R.id.btn_resume_workout).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ActiveWorkoutActivity.class));
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        Session active = repo.getActiveSession();
        if (active != null) {
            cardActive.setVisibility(View.VISIBLE);
            btnStartNew.setVisibility(View.GONE);
        } else {
            cardActive.setVisibility(View.GONE);
            btnStartNew.setVisibility(View.VISIBLE);
        }

        Session last = repo.getLastCompletedSession();
        if (last != null) {
            tvLastDate.setText("Ende: " + last.getEndTime());
            int exercises = repo.getExerciseCountForSession(last.getId());
            int sets = repo.getSetCountForSession(last.getId());
            tvLastStats.setText(exercises + " Übungen • " + sets + " Sätze");
        } else {
            tvLastDate.setText("Noch kein Training abgeschlossen.");
            tvLastStats.setText("");
        }
    }
}