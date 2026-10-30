package com.example.fitnessapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.model.TrainedExercise;
import com.example.fitnessapp.storage.WorkoutRepository;

import java.util.List;

public class StatsProgressSelectionFragment extends Fragment {

    private WorkoutRepository repo;
    private RecyclerView recycler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats_progress_selection, container, false);
        repo = new WorkoutRepository(requireContext());
        recycler = view.findViewById(R.id.recycler_trained_exercises);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        List<TrainedExercise> exercises = repo.getTrainedExercisesAdvanced();
        
        if (exercises.isEmpty()) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Noch keine abgeschlossenen Übungen vorhanden.", Toast.LENGTH_LONG).show();
            }
        } else {
            recycler.setAdapter(new TrainedExerciseAdapter(exercises));
        }
    }

    private class TrainedExerciseAdapter extends RecyclerView.Adapter<TrainedExerciseAdapter.ViewHolder> {
        private List<TrainedExercise> items;

        TrainedExerciseAdapter(List<TrainedExercise> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trained_exercise, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TrainedExercise ex = items.get(position);
            holder.tvName.setText(ex.getName());
            
            holder.itemView.setOnClickListener(v -> {
                if (getActivity() != null) {
                    StatsExerciseProgressFragment detailFragment = new StatsExerciseProgressFragment();
                    Bundle args = new Bundle();
                    args.putString("EXERCISE_NAME", ex.getName());
                    if (ex.getExerciseId() != null) {
                        args.putLong("EXERCISE_ID", ex.getExerciseId());
                    }
                    detailFragment.setArguments(args);
                    
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, detailFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_name);
            }
        }
    }
}