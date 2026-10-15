package com.example.fitnessapp.storage;
import com.example.fitnessapp.model.*;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.fitnessapp.model.WorkoutSet;

public class WorkoutRepository {

    private WorkoutDbHelper dbHelper;
    private SQLiteDatabase db;

    public WorkoutRepository(Context context) {
        dbHelper = new WorkoutDbHelper(context);
        db = dbHelper.getWritableDatabase();
    }


    public long insertExercise(com.github.dpdaaa.model.Exercise exercise) {
        ContentValues cv = new ContentValues();
        cv.put(WorkoutDbHelper.COL_EXERCISE_NAME, exercise.getName());

        return db.insert(WorkoutDbHelper.TABLE_EXERCISE, null, cv);
    }

    public int updateExercise(com.github.dpdaaa.model.Exercise exercise) {
        ContentValues cv = new ContentValues();
        cv.put(WorkoutDbHelper.COL_EXERCISE_NAME, exercise.getName());

        String whereClause = WorkoutDbHelper.COL_EXERCISE_ID + " = ?";
        return db.update(WorkoutDbHelper.TABLE_EXERCISE, cv, whereClause, new String[]{String.valueOf(exercise.getId())});
    }

    public int deleteExercise(long id) {
        String whereClause = WorkoutDbHelper.COL_EXERCISE_ID + " = ?";
        return db.delete(WorkoutDbHelper.TABLE_EXERCISE, whereClause, new String[]{String.valueOf(id)});
    }



    public long insertSession(Session session) {
        ContentValues cv = new ContentValues();
        cv.put(WorkoutDbHelper.COL_SESSION_DATE, session.getDate().toString());
        cv.put(WorkoutDbHelper.COL_SESSION_STARTZEIT, session.getStartZeit().toString());
        cv.put(WorkoutDbHelper.COL_SESSION_ENDZEIT, session.getEndZeit().toString());

        return db.insert(WorkoutDbHelper.TABLE_SESSION, null, cv);
    }

    public int updateSession(Session session) {
        ContentValues cv = new ContentValues();
        cv.put(WorkoutDbHelper.COL_SESSION_DATE, session.getDate().toString());
        cv.put(WorkoutDbHelper.COL_SESSION_STARTZEIT, session.getStartZeit().toString());
        cv.put(WorkoutDbHelper.COL_SESSION_ENDZEIT, session.getEndZeit().toString());

        String whereClause = WorkoutDbHelper.COL_SESSION_ID + " = ?";
        return db.update(WorkoutDbHelper.TABLE_SESSION, cv, whereClause, new String[]{String.valueOf(session.getId())});
    }

    public int deleteSession(long id) {
        String whereClause = WorkoutDbHelper.COL_SESSION_ID + " = ?";
        return db.delete(WorkoutDbHelper.TABLE_SESSION, whereClause, new String[]{String.valueOf(id)});
    }



    public long insertWorkoutSet(WorkoutSet set) {
        ContentValues cv = new ContentValues();
        cv.put(WorkoutDbHelper.COL_SET_WEIGHT, set.getWeight());
        cv.put(WorkoutDbHelper.COL_SET_REPS, set.getReps());
        cv.put(WorkoutDbHelper.COL_SET_EXERCISE_ID, set.getExerciseId());
        cv.put(WorkoutDbHelper.COL_SET_SESSION_ID, set.getSessionId());

        return db.insert(WorkoutDbHelper.TABLE_SET, null, cv);
    }

    public int updateWorkoutSet(WorkoutSet set) {
        ContentValues cv = new ContentValues();
        cv.put(WorkoutDbHelper.COL_SET_WEIGHT, set.getWeight());
        cv.put(WorkoutDbHelper.COL_SET_REPS, set.getReps());
        cv.put(WorkoutDbHelper.COL_SET_EXERCISE_ID, set.getExerciseId());
        cv.put(WorkoutDbHelper.COL_SET_SESSION_ID, set.getSessionId());

        String whereClause = WorkoutDbHelper.COL_SET_ID + " = ?";
        return db.update(WorkoutDbHelper.TABLE_SET, cv, whereClause, new String[]{String.valueOf(set.getId())});
    }

    public int deleteWorkoutSet(long id) {
        String whereClause = WorkoutDbHelper.COL_SET_ID + " = ?";
        return db.delete(WorkoutDbHelper.TABLE_SET, whereClause, new String[]{String.valueOf(id)});
    }

    public void close() {
        dbHelper.close();
    }
}