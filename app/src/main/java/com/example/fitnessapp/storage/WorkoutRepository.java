package com.example.fitnessapp.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.fitnessapp.model.*;
import com.example.fitnessapp.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class WorkoutRepository {

    private WorkoutDbHelper dbHelper;
    private SQLiteDatabase db;

    private static boolean timestampsNormalized = false;

    public WorkoutRepository(Context context) {
        dbHelper = new WorkoutDbHelper(context);
        db = dbHelper.getWritableDatabase();
        if (!timestampsNormalized) {
            normalizeTimestamps();
            timestampsNormalized = true;
        }
    }

    /**
     * Fixes the V2 migration issue where old string timestamps like "2023-10-25 14:30"
     * prevent CAST(start_time AS INTEGER) from working in V3 queries.
     */
    private void normalizeTimestamps() {
        Cursor cursor = db.rawQuery("SELECT id, start_time, end_time FROM " + WorkoutDbHelper.TABLE_SESSION + 
                                    " WHERE start_time LIKE '%-%' OR end_time LIKE '%-%'", null);
        if (cursor.getCount() == 0) {
            cursor.close();
            return;
        }

        db.beginTransaction();
        try {
            while(cursor.moveToNext()) {
                long id = cursor.getLong(0);
                String start = cursor.getString(1);
                String end = cursor.getString(2);

                ContentValues cv = new ContentValues();
                if (start != null && start.contains("-")) {
                    long startMs = TimeUtils.parseTimeMs(start);
                    if (startMs > 0) cv.put(WorkoutDbHelper.COL_SESSION_START, String.valueOf(startMs));
                }
                if (end != null && end.contains("-")) {
                    long endMs = TimeUtils.parseTimeMs(end);
                    if (endMs > 0) cv.put(WorkoutDbHelper.COL_SESSION_END, String.valueOf(endMs));
                }

                if (cv.size() > 0) {
                    db.update(WorkoutDbHelper.TABLE_SESSION, cv, "id = ?", new String[]{String.valueOf(id)});
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            cursor.close();
        }
    }

    // ================= EXERCISE =================

    public long insertExercise(Exercise exercise) {
        ContentValues cv = new ContentValues();
        cv.put(WorkoutDbHelper.COL_EXERCISE_NAME, exercise.getName());
        cv.put(WorkoutDbHelper.COL_EXERCISE_MUSCLE, exercise.getMuscleGroup());
        return db.insert(WorkoutDbHelper.TABLE_EXERCISE, null, cv);
    }

    public int updateExercise(Exercise exercise) {
        ContentValues cv = new ContentValues();
        cv.put(WorkoutDbHelper.COL_EXERCISE_NAME, exercise.getName());
        cv.put(WorkoutDbHelper.COL_EXERCISE_MUSCLE, exercise.getMuscleGroup());
        return db.update(WorkoutDbHelper.TABLE_EXERCISE, cv, WorkoutDbHelper.COL_EXERCISE_ID + " = ?", new String[]{String.valueOf(exercise.getId())});
    }

    public void deleteExercise(long exerciseId) {
        db.delete(WorkoutDbHelper.TABLE_PRESET_EX, WorkoutDbHelper.COL_PRESET_EX_EXERCISE_ID + " = ?", new String[]{String.valueOf(exerciseId)});
        db.delete(WorkoutDbHelper.TABLE_EXERCISE, WorkoutDbHelper.COL_EXERCISE_ID + " = ?", new String[]{String.valueOf(exerciseId)});
    }

    public List<Exercise> getAllExercises() {
        List<Exercise> list = new ArrayList<>();
        Cursor cursor = db.query(WorkoutDbHelper.TABLE_EXERCISE, null, null, null, null, null, WorkoutDbHelper.COL_EXERCISE_NAME + " ASC");
        while (cursor.moveToNext()) {
            list.add(new Exercise(
                    cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_EXERCISE_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_EXERCISE_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_EXERCISE_MUSCLE))
            ));
        }
        cursor.close();
        return list;
    }

    // ================= PRESETS =================

    public void deletePreset(long presetId) {
        db.delete(WorkoutDbHelper.TABLE_PRESET_EX, WorkoutDbHelper.COL_PRESET_EX_PRESET_ID + " = ?", new String[]{String.valueOf(presetId)});
        db.delete(WorkoutDbHelper.TABLE_PRESET, WorkoutDbHelper.COL_PRESET_ID + " = ?", new String[]{String.valueOf(presetId)});
    }

    public void updatePresetName(long presetId, String newName) {
        ContentValues cv = new ContentValues();
        cv.put(WorkoutDbHelper.COL_PRESET_NAME, newName);
        db.update(WorkoutDbHelper.TABLE_PRESET, cv, WorkoutDbHelper.COL_PRESET_ID + " = ?", new String[]{String.valueOf(presetId)});
    }

    public void deletePresetExercise(long presetExerciseId) {
        db.delete(WorkoutDbHelper.TABLE_PRESET_EX, WorkoutDbHelper.COL_PRESET_EX_ID + " = ?", new String[]{String.valueOf(presetExerciseId)});
    }

    public void updatePresetExerciseOrder(long presetExerciseId, int newOrder) {
        ContentValues cv = new ContentValues();
        cv.put(WorkoutDbHelper.COL_PRESET_EX_ORDER, newOrder);
        db.update(WorkoutDbHelper.TABLE_PRESET_EX, cv, WorkoutDbHelper.COL_PRESET_EX_ID + " = ?", new String[]{String.valueOf(presetExerciseId)});
    }

    public WorkoutPreset getPresetById(long presetId) {
        Cursor cursor = db.query(WorkoutDbHelper.TABLE_PRESET, null, WorkoutDbHelper.COL_PRESET_ID + " = ?", new String[]{String.valueOf(presetId)}, null, null, null);
        WorkoutPreset preset = null;
        if (cursor.moveToFirst()) {
            preset = new WorkoutPreset(
                    cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_PRESET_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_PRESET_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_PRESET_CREATED))
            );
        }
        cursor.close();
        return preset;
    }

    public long insertPreset(WorkoutPreset preset) {
        ContentValues cv = new ContentValues();
        cv.put(WorkoutDbHelper.COL_PRESET_NAME, preset.getName());
        cv.put(WorkoutDbHelper.COL_PRESET_CREATED, preset.getCreatedAt());
        return db.insert(WorkoutDbHelper.TABLE_PRESET, null, cv);
    }

    public List<WorkoutPreset> getAllPresets() {
        List<WorkoutPreset> list = new ArrayList<>();
        Cursor cursor = db.query(WorkoutDbHelper.TABLE_PRESET, null, null, null, null, null, WorkoutDbHelper.COL_PRESET_ID + " ASC");
        while (cursor.moveToNext()) {
            list.add(new WorkoutPreset(
                    cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_PRESET_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_PRESET_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_PRESET_CREATED))
            ));
        }
        cursor.close();
        return list;
    }

    public long insertPresetExercise(PresetExercise pe) {
        ContentValues cv = new ContentValues();
        cv.put(WorkoutDbHelper.COL_PRESET_EX_PRESET_ID, pe.getPresetId());
        cv.put(WorkoutDbHelper.COL_PRESET_EX_EXERCISE_ID, pe.getExerciseId());
        cv.put(WorkoutDbHelper.COL_PRESET_EX_ORDER, pe.getOrderIndex());
        return db.insert(WorkoutDbHelper.TABLE_PRESET_EX, null, cv);
    }
    
    public List<PresetExercise> getExercisesForPreset(long presetId) {
        List<PresetExercise> list = new ArrayList<>();
        String query = "SELECT pe.*, e.name, e.muscle_group FROM " + WorkoutDbHelper.TABLE_PRESET_EX + " pe " +
                       "JOIN " + WorkoutDbHelper.TABLE_EXERCISE + " e ON pe.exercise_id = e.id " +
                       "WHERE pe.preset_id = ? ORDER BY pe.order_index ASC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(presetId)});
        while (cursor.moveToNext()) {
            list.add(new PresetExercise(
                    cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_PRESET_EX_ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_PRESET_EX_PRESET_ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_PRESET_EX_EXERCISE_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_PRESET_EX_ORDER)),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("muscle_group"))
            ));
        }
        cursor.close();
        return list;
    }

    // ================= SESSIONS =================

    public Session getLastCompletedSession() {
        Cursor cursor = db.query(WorkoutDbHelper.TABLE_SESSION, null, WorkoutDbHelper.COL_SESSION_STATUS + " = ?", new String[]{String.valueOf(Session.STATUS_COMPLETED)}, null, null, WorkoutDbHelper.COL_SESSION_ID + " DESC", "1");
        Session session = null;
        if (cursor.moveToFirst()) {
            Long presetId = null;
            if (!cursor.isNull(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_PRESET_ID))) {
                presetId = cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_PRESET_ID));
            }
            session = new Session(
                    cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_ID)),
                    presetId,
                    cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_START)),
                    cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_END)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_STATUS))
            );
        }
        cursor.close();
        return session;
    }

    public int getExerciseCountForSession(long sessionId) {
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + WorkoutDbHelper.TABLE_SESSION_EX + " WHERE " + WorkoutDbHelper.COL_SESSION_EX_SESSION_ID + " = ?", new String[]{String.valueOf(sessionId)});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    public int getSetCountForSession(long sessionId) {
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + WorkoutDbHelper.TABLE_SET + " s JOIN " + WorkoutDbHelper.TABLE_SESSION_EX + " se ON s." + WorkoutDbHelper.COL_SET_SESSION_EX_ID + " = se." + WorkoutDbHelper.COL_SESSION_EX_ID + " WHERE se." + WorkoutDbHelper.COL_SESSION_EX_SESSION_ID + " = ?", new String[]{String.valueOf(sessionId)});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    public Session getActiveSession() {
        Cursor cursor = db.query(WorkoutDbHelper.TABLE_SESSION, null, WorkoutDbHelper.COL_SESSION_STATUS + " = ?", new String[]{String.valueOf(Session.STATUS_ACTIVE)}, null, null, null);
        Session session = null;
        if (cursor.moveToFirst()) {
            Long presetId = null;
            if (!cursor.isNull(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_PRESET_ID))) {
                presetId = cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_PRESET_ID));
            }
            session = new Session(
                    cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_ID)),
                    presetId,
                    cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_START)),
                    cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_END)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_STATUS))
            );
        }
        cursor.close();
        return session;
    }

    public Session getSessionById(long sessionId) {
        Cursor cursor = db.query(WorkoutDbHelper.TABLE_SESSION, null, WorkoutDbHelper.COL_SESSION_ID + " = ?", new String[]{String.valueOf(sessionId)}, null, null, null);
        Session session = null;
        if (cursor.moveToFirst()) {
            Long presetId = null;
            if (!cursor.isNull(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_PRESET_ID))) {
                presetId = cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_PRESET_ID));
            }
            session = new Session(
                    cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_ID)),
                    presetId,
                    cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_START)),
                    cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_END)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_STATUS))
            );
        }
        cursor.close();
        return session;
    }

    public List<SessionSummary> getCompletedSessionsSummary() {
        return getCompletedSessionsForTimeRange(0, Long.MAX_VALUE);
    }
    
    public List<SessionSummary> getCompletedSessionsForTimeRange(long startMs, long endMs) {
        List<SessionSummary> list = new ArrayList<>();
        String query = "SELECT s." + WorkoutDbHelper.COL_SESSION_ID + ", s." + WorkoutDbHelper.COL_SESSION_START + ", s." + WorkoutDbHelper.COL_SESSION_END + ", " +
                "COUNT(DISTINCT se." + WorkoutDbHelper.COL_SESSION_EX_ID + ") as exercise_count, " +
                "SUM(CASE WHEN es." + WorkoutDbHelper.COL_SET_COMPLETED + " = 1 THEN 1 ELSE 0 END) as completed_sets, " +
                "SUM(CASE WHEN es." + WorkoutDbHelper.COL_SET_COMPLETED + " = 1 AND es." + WorkoutDbHelper.COL_SET_WEIGHT + " IS NOT NULL AND es." + WorkoutDbHelper.COL_SET_REPS + " IS NOT NULL THEN (es." + WorkoutDbHelper.COL_SET_WEIGHT + " * es." + WorkoutDbHelper.COL_SET_REPS + ") ELSE 0 END) as total_volume " +
                "FROM " + WorkoutDbHelper.TABLE_SESSION + " s " +
                "LEFT JOIN " + WorkoutDbHelper.TABLE_SESSION_EX + " se ON s." + WorkoutDbHelper.COL_SESSION_ID + " = se." + WorkoutDbHelper.COL_SESSION_EX_SESSION_ID + " " +
                "LEFT JOIN " + WorkoutDbHelper.TABLE_SET + " es ON se." + WorkoutDbHelper.COL_SESSION_EX_ID + " = es." + WorkoutDbHelper.COL_SET_SESSION_EX_ID + " " +
                "WHERE s." + WorkoutDbHelper.COL_SESSION_STATUS + " = " + Session.STATUS_COMPLETED + " " +
                "AND CAST(s.start_time AS INTEGER) >= ? AND CAST(s.start_time AS INTEGER) < ? " +
                "GROUP BY s." + WorkoutDbHelper.COL_SESSION_ID;
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(startMs), String.valueOf(endMs)});
        while (cursor.moveToNext()) {
            list.add(new SessionSummary(
                    cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_START)),
                    cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_END)),
                    cursor.getInt(cursor.getColumnIndexOrThrow("exercise_count")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("completed_sets")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("total_volume"))
            ));
        }
        cursor.close();
        return list;
    }

    public SessionSummary getSessionSummaryById(long sessionId) {
        SessionSummary summary = null;
        String query = "SELECT s." + WorkoutDbHelper.COL_SESSION_ID + ", s." + WorkoutDbHelper.COL_SESSION_START + ", s." + WorkoutDbHelper.COL_SESSION_END + ", " +
                "COUNT(DISTINCT se." + WorkoutDbHelper.COL_SESSION_EX_ID + ") as exercise_count, " +
                "SUM(CASE WHEN es." + WorkoutDbHelper.COL_SET_COMPLETED + " = 1 THEN 1 ELSE 0 END) as completed_sets, " +
                "SUM(CASE WHEN es." + WorkoutDbHelper.COL_SET_COMPLETED + " = 1 AND es." + WorkoutDbHelper.COL_SET_WEIGHT + " IS NOT NULL AND es." + WorkoutDbHelper.COL_SET_REPS + " IS NOT NULL THEN (es." + WorkoutDbHelper.COL_SET_WEIGHT + " * es." + WorkoutDbHelper.COL_SET_REPS + ") ELSE 0 END) as total_volume " +
                "FROM " + WorkoutDbHelper.TABLE_SESSION + " s " +
                "LEFT JOIN " + WorkoutDbHelper.TABLE_SESSION_EX + " se ON s." + WorkoutDbHelper.COL_SESSION_ID + " = se." + WorkoutDbHelper.COL_SESSION_EX_SESSION_ID + " " +
                "LEFT JOIN " + WorkoutDbHelper.TABLE_SET + " es ON se." + WorkoutDbHelper.COL_SESSION_EX_ID + " = es." + WorkoutDbHelper.COL_SET_SESSION_EX_ID + " " +
                "WHERE s." + WorkoutDbHelper.COL_SESSION_ID + " = ? " +
                "GROUP BY s." + WorkoutDbHelper.COL_SESSION_ID;
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(sessionId)});
        if (cursor.moveToFirst()) {
            summary = new SessionSummary(
                    cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_START)),
                    cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_END)),
                    cursor.getInt(cursor.getColumnIndexOrThrow("exercise_count")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("completed_sets")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("total_volume"))
            );
        }
        cursor.close();
        return summary;
    }

    public StatsOverview getAdvancedStatsOverview(long startOf7DaysMs, long startOfCurrentWeekMs) {
        String q1 = "SELECT " +
                "COUNT(DISTINCT s.id) as total_workouts, " +
                "SUM(CASE WHEN CAST(s.start_time AS INTEGER) >= ? THEN 1 ELSE 0 END) as workouts_7d, " +
                "SUM(CASE WHEN s.end_time IS NOT NULL AND s.end_time != '' THEN CAST(s.end_time AS INTEGER) - CAST(s.start_time AS INTEGER) ELSE 0 END) as total_duration " +
                "FROM workout_session s WHERE s.status = " + Session.STATUS_COMPLETED;
                
        Cursor c1 = db.rawQuery(q1, new String[]{String.valueOf(startOf7DaysMs)});
        int totalWorkouts = 0;
        int workouts7d = 0;
        long totalDurationMs = 0;
        if (c1.moveToFirst()) {
            totalWorkouts = c1.getInt(0);
            workouts7d = c1.getInt(1);
            totalDurationMs = c1.getLong(2);
        }
        c1.close();
        
        String q2 = "SELECT " +
                "SUM(CASE WHEN es.is_completed = 1 AND es.weight IS NOT NULL AND es.reps IS NOT NULL THEN (es.weight * es.reps) ELSE 0 END) as total_volume, " +
                "SUM(CASE WHEN CAST(s.start_time AS INTEGER) >= ? AND es.is_completed = 1 AND es.weight IS NOT NULL AND es.reps IS NOT NULL THEN (es.weight * es.reps) ELSE 0 END) as current_week_volume, " +
                "SUM(CASE WHEN es.is_completed = 1 THEN 1 ELSE 0 END) as total_sets " +
                "FROM workout_session s " +
                "LEFT JOIN session_exercise se ON s.id = se.session_id " +
                "LEFT JOIN exercise_set es ON se.id = es.session_exercise_id " +
                "WHERE s.status = " + Session.STATUS_COMPLETED;
                
        Cursor c2 = db.rawQuery(q2, new String[]{String.valueOf(startOfCurrentWeekMs)});
        double totalVolume = 0;
        double currentWeekVolume = 0;
        int totalSets = 0;
        if (c2.moveToFirst()) {
            totalVolume = c2.getDouble(0);
            currentWeekVolume = c2.getDouble(1);
            totalSets = c2.getInt(2);
        }
        c2.close();
        
        double avgVolume = totalWorkouts > 0 ? totalVolume / totalWorkouts : 0;
        double avgSets = totalWorkouts > 0 ? (double) totalSets / totalWorkouts : 0;
        long avgDuration = totalWorkouts > 0 ? totalDurationMs / totalWorkouts : 0;
        
        return new StatsOverview(totalWorkouts, workouts7d, totalVolume, avgVolume, currentWeekVolume, avgSets, avgDuration);
    }

    public List<WeeklyVolume> getWeeklyVolumesAdvanced(long[] weekStartMs, String[] weekLabels) {
        List<WeeklyVolume> weeklyVolumes = new ArrayList<>();
        for (String label : weekLabels) {
            weeklyVolumes.add(new WeeklyVolume(label, 0, 0, 0));
        }

        String query = "SELECT CAST(s.start_time AS INTEGER) as start_ms, " +
                "SUM(CASE WHEN es.is_completed = 1 AND es.weight IS NOT NULL AND es.reps IS NOT NULL THEN (es.weight * es.reps) ELSE 0 END) as session_volume, " +
                "SUM(CASE WHEN es.is_completed = 1 THEN 1 ELSE 0 END) as session_sets " +
                "FROM workout_session s " +
                "LEFT JOIN session_exercise se ON s.id = se.session_id " +
                "LEFT JOIN exercise_set es ON se.id = es.session_exercise_id " +
                "WHERE s.status = " + Session.STATUS_COMPLETED + " " +
                "GROUP BY s.id";
                
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            long startMs = cursor.getLong(cursor.getColumnIndexOrThrow("start_ms"));
            double volume = cursor.getDouble(cursor.getColumnIndexOrThrow("session_volume"));
            int sets = cursor.getInt(cursor.getColumnIndexOrThrow("session_sets"));
            
            for (int i = 0; i < weekStartMs.length; i++) {
                long weekStart = weekStartMs[i];
                long weekEnd = (i == weekStartMs.length - 1) ? Long.MAX_VALUE : weekStartMs[i + 1];
                if (startMs >= weekStart && startMs < weekEnd) {
                    WeeklyVolume wv = weeklyVolumes.get(i);
                    wv.addVolume(volume);
                    wv.addWorkout();
                    wv.addSets(sets);
                    break;
                }
            }
        }
        cursor.close();
        return weeklyVolumes;
    }

    public List<MuscleGroupStat> getMuscleGroupStats() {
        List<MuscleGroupStat> list = new ArrayList<>();
        String query = "SELECT se.muscle_group, " +
                       "COUNT(DISTINCT s.id) as workouts, " +
                       "SUM(CASE WHEN es.is_completed = 1 THEN 1 ELSE 0 END) as sets, " +
                       "SUM(CASE WHEN es.is_completed = 1 AND es.weight IS NOT NULL AND es.reps IS NOT NULL THEN es.weight * es.reps ELSE 0 END) as volume " +
                       "FROM workout_session s " +
                       "JOIN session_exercise se ON s.id = se.session_id " +
                       "LEFT JOIN exercise_set es ON se.id = es.session_exercise_id " +
                       "WHERE s.status = " + Session.STATUS_COMPLETED + " " +
                       "GROUP BY se.muscle_group " +
                       "ORDER BY volume DESC";
        Cursor c = db.rawQuery(query, null);
        double totalGlobalVolume = 0;
        while(c.moveToNext()) {
            String mg = c.getString(0);
            if(mg == null || mg.isEmpty()) mg = "Unbekannt";
            MuscleGroupStat stat = new MuscleGroupStat(mg, c.getInt(1), c.getInt(2), c.getDouble(3));
            totalGlobalVolume += stat.getVolume();
            list.add(stat);
        }
        c.close();
        
        for (MuscleGroupStat stat : list) {
            if (totalGlobalVolume > 0) {
                stat.setVolumePercentage((stat.getVolume() / totalGlobalVolume) * 100);
            } else {
                stat.setVolumePercentage(0);
            }
        }
        return list;
    }

    public List<TrainedExercise> getTrainedExercisesAdvanced() {
        List<TrainedExercise> list = new ArrayList<>();
        String q = "SELECT se.exercise_id, se.name FROM session_exercise se " +
                   "JOIN workout_session s ON se.session_id = s.id " +
                   "WHERE s.status = " + Session.STATUS_COMPLETED + " " +
                   "GROUP BY se.exercise_id, se.name ORDER BY se.name COLLATE NOCASE ASC";
        Cursor c = db.rawQuery(q, null);
        while(c.moveToNext()) {
            Long id = c.isNull(0) ? null : c.getLong(0);
            list.add(new TrainedExercise(id, c.getString(1)));
        }
        c.close();
        return list;
    }

    public List<ExerciseProgressEntry> getExerciseProgressAdvanced(Long exerciseId, String exerciseName) {
        List<ExerciseProgressEntry> entries = new ArrayList<>();
        
        String where = (exerciseId != null) ? 
            "(se.exercise_id = ? OR (se.exercise_id IS NULL AND se.name = ?))" : 
            "se.name = ?";
            
        String[] args = (exerciseId != null) ? 
            new String[]{String.valueOf(exerciseId), exerciseName} : 
            new String[]{exerciseName};
        
        String query = "SELECT s.id, s.start_time, se.id as se_id " +
                       "FROM " + WorkoutDbHelper.TABLE_SESSION + " s " +
                       "JOIN " + WorkoutDbHelper.TABLE_SESSION_EX + " se ON s.id = se.session_id " +
                       "WHERE s.status = " + Session.STATUS_COMPLETED + " AND " + where + " " +
                       "ORDER BY CAST(s.start_time AS INTEGER) DESC";
                       
        Cursor c = db.rawQuery(query, args);
        while(c.moveToNext()) {
            long sessionId = c.getLong(0);
            long startTimeMs = TimeUtils.parseTimeMs(c.getString(1));
            long seId = c.getLong(2);
            
            List<WorkoutSet> sets = getSetsForSessionExercise(seId);
            List<WorkoutSet> completedSets = new ArrayList<>();
            double totalWeight = 0;
            int validWeightCount = 0;
            
            for(WorkoutSet ws : sets) {
                if(ws.isCompleted()) {
                    completedSets.add(ws);
                    if(ws.getWeight() != null) {
                        totalWeight += ws.getWeight();
                        validWeightCount++;
                    }
                }
            }
            
            if(!completedSets.isEmpty()) {
                double avgWeight = validWeightCount > 0 ? totalWeight / validWeightCount : 0;
                entries.add(new ExerciseProgressEntry(sessionId, startTimeMs, avgWeight, completedSets));
            }
        }
        c.close();
        return entries;
    }

    public long startSession(Long presetId, String startTime) {
        db.beginTransaction();
        try {
            // Abort if an active session exists
            if (getActiveSession() != null) {
                return -1;
            }

            ContentValues cv = new ContentValues();
            cv.put(WorkoutDbHelper.COL_SESSION_PRESET_ID, presetId);
            cv.put(WorkoutDbHelper.COL_SESSION_START, startTime);
            cv.put(WorkoutDbHelper.COL_SESSION_STATUS, Session.STATUS_ACTIVE);
            long sessionId = db.insert(WorkoutDbHelper.TABLE_SESSION, null, cv);

            if (presetId != null) {
                List<PresetExercise> presetExercises = getExercisesForPreset(presetId);
                for (PresetExercise pe : presetExercises) {
                    ContentValues secv = new ContentValues();
                    secv.put(WorkoutDbHelper.COL_SESSION_EX_SESSION_ID, sessionId);
                    secv.put(WorkoutDbHelper.COL_SESSION_EX_EXERCISE_ID, pe.getExerciseId());
                    secv.put(WorkoutDbHelper.COL_SESSION_EX_NAME, pe.getName());
                    secv.put(WorkoutDbHelper.COL_SESSION_EX_MUSCLE, pe.getMuscleGroup());
                    secv.put(WorkoutDbHelper.COL_SESSION_EX_ORDER, pe.getOrderIndex());
                    db.insert(WorkoutDbHelper.TABLE_SESSION_EX, null, secv);
                }
            }
            db.setTransactionSuccessful();
            return sessionId;
        } finally {
            db.endTransaction();
        }
    }

    public int completeSession(long sessionId, String endTime) {
        ContentValues cv = new ContentValues();
        cv.put(WorkoutDbHelper.COL_SESSION_END, endTime);
        cv.put(WorkoutDbHelper.COL_SESSION_STATUS, Session.STATUS_COMPLETED);
        return db.update(WorkoutDbHelper.TABLE_SESSION, cv, WorkoutDbHelper.COL_SESSION_ID + " = ?", new String[]{String.valueOf(sessionId)});
    }

    // ================= SESSION EXERCISES =================

    public void deleteSessionExercise(long sessionExerciseId) {
        db.delete(WorkoutDbHelper.TABLE_SET, WorkoutDbHelper.COL_SET_SESSION_EX_ID + " = ?", new String[]{String.valueOf(sessionExerciseId)});
        db.delete(WorkoutDbHelper.TABLE_SESSION_EX, WorkoutDbHelper.COL_SESSION_EX_ID + " = ?", new String[]{String.valueOf(sessionExerciseId)});
    }

    public void updateSessionExerciseOrder(long sessionExerciseId, int newOrder) {
        ContentValues cv = new ContentValues();
        cv.put(WorkoutDbHelper.COL_SESSION_EX_ORDER, newOrder);
        db.update(WorkoutDbHelper.TABLE_SESSION_EX, cv, WorkoutDbHelper.COL_SESSION_EX_ID + " = ?", new String[]{String.valueOf(sessionExerciseId)});
    }

    public long insertSessionExercise(SessionExercise se) {
        ContentValues cv = new ContentValues();
        cv.put(WorkoutDbHelper.COL_SESSION_EX_SESSION_ID, se.getSessionId());
        cv.put(WorkoutDbHelper.COL_SESSION_EX_EXERCISE_ID, se.getExerciseId());
        cv.put(WorkoutDbHelper.COL_SESSION_EX_NAME, se.getName());
        cv.put(WorkoutDbHelper.COL_SESSION_EX_MUSCLE, se.getMuscleGroup());
        cv.put(WorkoutDbHelper.COL_SESSION_EX_ORDER, se.getOrderIndex());
        return db.insert(WorkoutDbHelper.TABLE_SESSION_EX, null, cv);
    }
    
    public List<SessionExercise> getExercisesForSession(long sessionId) {
        List<SessionExercise> list = new ArrayList<>();
        Cursor cursor = db.query(WorkoutDbHelper.TABLE_SESSION_EX, null, WorkoutDbHelper.COL_SESSION_EX_SESSION_ID + " = ?", new String[]{String.valueOf(sessionId)}, null, null, WorkoutDbHelper.COL_SESSION_EX_ORDER + " ASC");
        while (cursor.moveToNext()) {
            Long exerciseId = null;
            if (!cursor.isNull(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_EX_EXERCISE_ID))) {
                exerciseId = cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_EX_EXERCISE_ID));
            }
            list.add(new SessionExercise(
                    cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_EX_ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_EX_SESSION_ID)),
                    exerciseId,
                    cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_EX_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_EX_MUSCLE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SESSION_EX_ORDER))
            ));
        }
        cursor.close();
        return list;
    }

    public List<WorkoutSet> getSetsForSessionExercise(long sessionExerciseId) {
        List<WorkoutSet> list = new ArrayList<>();
        Cursor cursor = db.query(WorkoutDbHelper.TABLE_SET, null, WorkoutDbHelper.COL_SET_SESSION_EX_ID + " = ?", new String[]{String.valueOf(sessionExerciseId)}, null, null, WorkoutDbHelper.COL_SET_NUMBER + " ASC");
        while (cursor.moveToNext()) {
            Integer reps = null;
            if (!cursor.isNull(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SET_REPS))) {
                reps = cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SET_REPS));
            }
            Double weight = null;
            if (!cursor.isNull(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SET_WEIGHT))) {
                weight = cursor.getDouble(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SET_WEIGHT));
            }
            list.add(new WorkoutSet(
                    cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SET_ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SET_SESSION_EX_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SET_NUMBER)),
                    reps,
                    weight,
                    cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SET_COMPLETED)) == 1
            ));
        }
        cursor.close();
        return list;
    }

    // ================= PROGRESS & STATS =================

    public List<String> getTrainedExerciseNames() {
        List<String> names = new ArrayList<>();
        String query = "SELECT DISTINCT se.name FROM " + WorkoutDbHelper.TABLE_SESSION_EX + " se " +
                       "JOIN " + WorkoutDbHelper.TABLE_SESSION + " s ON se.session_id = s.id " +
                       "WHERE s.status = " + Session.STATUS_COMPLETED + " " +
                       "ORDER BY se.name COLLATE NOCASE ASC";
        Cursor c = db.rawQuery(query, null);
        while(c.moveToNext()) {
            names.add(c.getString(0));
        }
        c.close();
        return names;
    }

    public List<ExerciseProgressEntry> getExerciseProgress(String exerciseName) {
        List<ExerciseProgressEntry> entries = new ArrayList<>();
        
        String query = "SELECT s.id, s.start_time, se.id as se_id " +
                       "FROM " + WorkoutDbHelper.TABLE_SESSION + " s " +
                       "JOIN " + WorkoutDbHelper.TABLE_SESSION_EX + " se ON s.id = se.session_id " +
                       "WHERE s.status = " + Session.STATUS_COMPLETED + " AND se.name = ? " +
                       "ORDER BY CAST(s.start_time AS INTEGER) DESC";
                       
        Cursor c = db.rawQuery(query, new String[]{exerciseName});
        while(c.moveToNext()) {
            long sessionId = c.getLong(0);
            long startTimeMs = TimeUtils.parseTimeMs(c.getString(1));
            long seId = c.getLong(2);
            
            List<WorkoutSet> sets = getSetsForSessionExercise(seId);
            List<WorkoutSet> completedSets = new ArrayList<>();
            double totalWeight = 0;
            int validWeightCount = 0;
            
            for(WorkoutSet ws : sets) {
                if(ws.isCompleted()) {
                    completedSets.add(ws);
                    if(ws.getWeight() != null) {
                        totalWeight += ws.getWeight();
                        validWeightCount++;
                    }
                }
            }
            
            if(!completedSets.isEmpty()) {
                double avgWeight = validWeightCount > 0 ? totalWeight / validWeightCount : 0;
                entries.add(new ExerciseProgressEntry(sessionId, startTimeMs, avgWeight, completedSets));
            }
        }
        c.close();
        return entries;
    }

    // ================= EXERCISE SETS =================

    public void deleteWorkoutSet(long setId) {
        db.delete(WorkoutDbHelper.TABLE_SET, WorkoutDbHelper.COL_SET_ID + " = ?", new String[]{String.valueOf(setId)});
    }

    public List<WorkoutSet> getPreviousSetsForExercise(Long exerciseId, String name) {
        List<WorkoutSet> list = new ArrayList<>();
        
        // Find the most recent completed session that contains this exercise
        String subQuery = "SELECT MAX(sess.id) FROM " + WorkoutDbHelper.TABLE_SESSION + " sess " +
                          "JOIN " + WorkoutDbHelper.TABLE_SESSION_EX + " se ON sess.id = se.session_id " +
                          "WHERE sess.status = " + Session.STATUS_COMPLETED + " AND ";
        
        String[] args;
        if (exerciseId != null) {
            subQuery += "se.exercise_id = ?";
            args = new String[]{String.valueOf(exerciseId), String.valueOf(exerciseId)};
        } else {
            subQuery += "se.name = ?";
            args = new String[]{name, name};
        }

        String query = "SELECT s.* FROM " + WorkoutDbHelper.TABLE_SET + " s " +
                       "JOIN " + WorkoutDbHelper.TABLE_SESSION_EX + " se ON s.session_exercise_id = se.id " +
                       "WHERE s.is_completed = 1 AND se.session_id = (" + subQuery + ") AND ";
        
        if (exerciseId != null) {
            query += "se.exercise_id = ? ORDER BY s.set_number ASC";
        } else {
            query += "se.name = ? ORDER BY s.set_number ASC";
        }

        Cursor cursor = db.rawQuery(query, args);
        while (cursor.moveToNext()) {
            Integer reps = null;
            if (!cursor.isNull(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SET_REPS))) {
                reps = cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SET_REPS));
            }
            Double weight = null;
            if (!cursor.isNull(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SET_WEIGHT))) {
                weight = cursor.getDouble(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SET_WEIGHT));
            }
            list.add(new WorkoutSet(
                    cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SET_ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SET_SESSION_EX_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SET_NUMBER)),
                    reps,
                    weight,
                    cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COL_SET_COMPLETED)) == 1
            ));
        }
        cursor.close();
        return list;
    }

    public long insertWorkoutSet(WorkoutSet set) {
        ContentValues cv = new ContentValues();
        cv.put(WorkoutDbHelper.COL_SET_SESSION_EX_ID, set.getSessionExerciseId());
        cv.put(WorkoutDbHelper.COL_SET_NUMBER, set.getSetNumber());
        cv.put(WorkoutDbHelper.COL_SET_REPS, set.getReps());
        cv.put(WorkoutDbHelper.COL_SET_WEIGHT, set.getWeight());
        cv.put(WorkoutDbHelper.COL_SET_COMPLETED, set.isCompleted() ? 1 : 0);
        return db.insert(WorkoutDbHelper.TABLE_SET, null, cv);
    }

    public int updateWorkoutSet(WorkoutSet set) {
        ContentValues cv = new ContentValues();
        cv.put(WorkoutDbHelper.COL_SET_NUMBER, set.getSetNumber());
        cv.put(WorkoutDbHelper.COL_SET_REPS, set.getReps());
        cv.put(WorkoutDbHelper.COL_SET_WEIGHT, set.getWeight());
        cv.put(WorkoutDbHelper.COL_SET_COMPLETED, set.isCompleted() ? 1 : 0);
        return db.update(WorkoutDbHelper.TABLE_SET, cv, WorkoutDbHelper.COL_SET_ID + " = ?", new String[]{String.valueOf(set.getId())});
    }

    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
    
    // For counting items in Backup
    public int getWorkoutCount() {
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + WorkoutDbHelper.TABLE_SESSION + " WHERE status = " + Session.STATUS_COMPLETED, null);
        int count = 0;
        if(c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }
    
    public int getExerciseCount() {
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + WorkoutDbHelper.TABLE_EXERCISE, null);
        int count = 0;
        if(c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }
    
    public int getPresetCount() {
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + WorkoutDbHelper.TABLE_PRESET, null);
        int count = 0;
        if(c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    // ================= LEGACY SHIMS (for UI compilation) =================
    
    public List<TrainingDay> getAllDays() {
        List<TrainingDay> days = new ArrayList<>();
        List<WorkoutPreset> presets = getAllPresets();
        for (WorkoutPreset p : presets) {
            days.add(new TrainingDay(p.getId(), p.getName(), false));
        }
        return days;
    }
    
    public void updateTrainingDay(TrainingDay day) {
        // No-op for now
    }
    
    public List<PlanExercise> getExercisesForDay(long dayId) {
        List<PlanExercise> list = new ArrayList<>();
        List<PresetExercise> presetExercises = getExercisesForPreset(dayId);
        for (PresetExercise pe : presetExercises) {
            list.add(new PlanExercise(pe.getId(), pe.getPresetId(), pe.getName(), pe.getMuscleGroup(), 0, 0, 0.0));
        }
        return list;
    }
    
    public long insertPlanExercise(long dayId, String name, String muscle, int sets, int reps, double weight) {
        Exercise exercise = new Exercise(name, muscle);
        long exerciseId = insertExercise(exercise);
        
        PresetExercise pe = new PresetExercise(0, dayId, exerciseId, 0, name, muscle);
        return insertPresetExercise(pe);
    }
    
    public int deletePlanExercise(long exerciseId) {
        return db.delete(WorkoutDbHelper.TABLE_PRESET_EX, WorkoutDbHelper.COL_PRESET_EX_ID + " = ?", new String[]{String.valueOf(exerciseId)});
    }
    
    public long insertSession(Session session) {
        return startSession(session.getPresetId(), session.getStartTime());
    }
}