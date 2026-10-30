package com.example.fitnessapp.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class WorkoutDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "fitness_tracker.db";
    public static final int DATABASE_VERSION = 3; // Migration to v3

    // === Table: exercise ===
    public static final String TABLE_EXERCISE = "exercise";
    public static final String COL_EXERCISE_ID = "id";
    public static final String COL_EXERCISE_NAME = "name";
    public static final String COL_EXERCISE_MUSCLE = "muscle_group"; // New in v3

    // === Table: workout_preset ===
    public static final String TABLE_PRESET = "workout_preset";
    public static final String COL_PRESET_ID = "id";
    public static final String COL_PRESET_NAME = "name";
    public static final String COL_PRESET_CREATED = "created_at";

    // === Table: preset_exercise ===
    public static final String TABLE_PRESET_EX = "preset_exercise";
    public static final String COL_PRESET_EX_ID = "id";
    public static final String COL_PRESET_EX_PRESET_ID = "preset_id";
    public static final String COL_PRESET_EX_EXERCISE_ID = "exercise_id";
    public static final String COL_PRESET_EX_ORDER = "order_index";

    // === Table: workout_session ===
    public static final String TABLE_SESSION = "workout_session";
    public static final String COL_SESSION_ID = "id";
    public static final String COL_SESSION_PRESET_ID = "preset_id"; // Nullable
    public static final String COL_SESSION_START = "start_time";
    public static final String COL_SESSION_END = "end_time";
    public static final String COL_SESSION_STATUS = "status"; // 0=active, 1=completed

    // === Table: session_exercise ===
    public static final String TABLE_SESSION_EX = "session_exercise";
    public static final String COL_SESSION_EX_ID = "id";
    public static final String COL_SESSION_EX_SESSION_ID = "session_id";
    public static final String COL_SESSION_EX_EXERCISE_ID = "exercise_id"; // Nullable
    public static final String COL_SESSION_EX_NAME = "name";
    public static final String COL_SESSION_EX_MUSCLE = "muscle_group";
    public static final String COL_SESSION_EX_ORDER = "order_index";

    // === Table: exercise_set ===
    public static final String TABLE_SET = "exercise_set";
    public static final String COL_SET_ID = "id";
    public static final String COL_SET_SESSION_EX_ID = "session_exercise_id";
    public static final String COL_SET_NUMBER = "set_number";
    public static final String COL_SET_REPS = "reps"; // Nullable
    public static final String COL_SET_WEIGHT = "weight"; // Nullable
    public static final String COL_SET_COMPLETED = "is_completed"; // Default 0

    public WorkoutDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Build tables for fresh install
        db.execSQL("CREATE TABLE " + TABLE_EXERCISE + " ("
                + COL_EXERCISE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_EXERCISE_NAME + " TEXT NOT NULL, "
                + COL_EXERCISE_MUSCLE + " TEXT NOT NULL);");

        db.execSQL("CREATE TABLE " + TABLE_PRESET + " ("
                + COL_PRESET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PRESET_NAME + " TEXT NOT NULL, "
                + COL_PRESET_CREATED + " TEXT);");

        db.execSQL("CREATE TABLE " + TABLE_PRESET_EX + " ("
                + COL_PRESET_EX_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PRESET_EX_PRESET_ID + " INTEGER NOT NULL, "
                + COL_PRESET_EX_EXERCISE_ID + " INTEGER NOT NULL, "
                + COL_PRESET_EX_ORDER + " INTEGER NOT NULL, "
                + "FOREIGN KEY(" + COL_PRESET_EX_PRESET_ID + ") REFERENCES " + TABLE_PRESET + "(" + COL_PRESET_ID + "), "
                + "FOREIGN KEY(" + COL_PRESET_EX_EXERCISE_ID + ") REFERENCES " + TABLE_EXERCISE + "(" + COL_EXERCISE_ID + "));");

        db.execSQL("CREATE TABLE " + TABLE_SESSION + " ("
                + COL_SESSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SESSION_PRESET_ID + " INTEGER, "
                + COL_SESSION_START + " TEXT, "
                + COL_SESSION_END + " TEXT, "
                + COL_SESSION_STATUS + " INTEGER NOT NULL DEFAULT 1, "
                + "FOREIGN KEY(" + COL_SESSION_PRESET_ID + ") REFERENCES " + TABLE_PRESET + "(" + COL_PRESET_ID + ") ON DELETE SET NULL);");
                
        // Ensure only one active session exists
        db.execSQL("CREATE UNIQUE INDEX idx_active_session ON " + TABLE_SESSION + " (" + COL_SESSION_STATUS + ") WHERE " + COL_SESSION_STATUS + " = 0;");

        db.execSQL("CREATE TABLE " + TABLE_SESSION_EX + " ("
                + COL_SESSION_EX_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SESSION_EX_SESSION_ID + " INTEGER NOT NULL, "
                + COL_SESSION_EX_EXERCISE_ID + " INTEGER, "
                + COL_SESSION_EX_NAME + " TEXT NOT NULL, "
                + COL_SESSION_EX_MUSCLE + " TEXT NOT NULL, "
                + COL_SESSION_EX_ORDER + " INTEGER NOT NULL, "
                + "FOREIGN KEY(" + COL_SESSION_EX_SESSION_ID + ") REFERENCES " + TABLE_SESSION + "(" + COL_SESSION_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY(" + COL_SESSION_EX_EXERCISE_ID + ") REFERENCES " + TABLE_EXERCISE + "(" + COL_EXERCISE_ID + ") ON DELETE SET NULL);");

        db.execSQL("CREATE TABLE " + TABLE_SET + " ("
                + COL_SET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SET_SESSION_EX_ID + " INTEGER NOT NULL, "
                + COL_SET_NUMBER + " INTEGER NOT NULL, "
                + COL_SET_REPS + " INTEGER, "
                + COL_SET_WEIGHT + " REAL, "
                + COL_SET_COMPLETED + " INTEGER NOT NULL DEFAULT 0, "
                + "FOREIGN KEY(" + COL_SET_SESSION_EX_ID + ") REFERENCES " + TABLE_SESSION_EX + "(" + COL_SESSION_EX_ID + ") ON DELETE CASCADE);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.beginTransaction();
            try {
                // 1. Alter exercise table
                db.execSQL("ALTER TABLE exercise ADD COLUMN muscle_group TEXT NOT NULL DEFAULT 'Unknown';");

                // 2. Create new tables
                db.execSQL("CREATE TABLE workout_preset (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, created_at TEXT);");
                
                db.execSQL("CREATE TABLE preset_exercise (id INTEGER PRIMARY KEY AUTOINCREMENT, preset_id INTEGER NOT NULL, exercise_id INTEGER NOT NULL, order_index INTEGER NOT NULL, FOREIGN KEY(preset_id) REFERENCES workout_preset(id), FOREIGN KEY(exercise_id) REFERENCES exercise(id));");
                
                db.execSQL("CREATE TABLE workout_session (id INTEGER PRIMARY KEY AUTOINCREMENT, preset_id INTEGER, start_time TEXT, end_time TEXT, status INTEGER NOT NULL DEFAULT 1, FOREIGN KEY(preset_id) REFERENCES workout_preset(id) ON DELETE SET NULL);");
                db.execSQL("CREATE UNIQUE INDEX idx_active_session ON workout_session (status) WHERE status = 0;");
                
                db.execSQL("CREATE TABLE session_exercise (id INTEGER PRIMARY KEY AUTOINCREMENT, session_id INTEGER NOT NULL, exercise_id INTEGER, name TEXT NOT NULL, muscle_group TEXT NOT NULL, order_index INTEGER NOT NULL, FOREIGN KEY(session_id) REFERENCES workout_session(id) ON DELETE CASCADE, FOREIGN KEY(exercise_id) REFERENCES exercise(id) ON DELETE SET NULL);");
                
                db.execSQL("CREATE TABLE exercise_set (id INTEGER PRIMARY KEY AUTOINCREMENT, session_exercise_id INTEGER NOT NULL, set_number INTEGER NOT NULL, reps INTEGER, weight REAL, is_completed INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(session_exercise_id) REFERENCES session_exercise(id) ON DELETE CASCADE);");

                // 3. Migrate data
                // Migrate exercises from plan_exercise if not exists
                db.execSQL("INSERT INTO exercise (name, muscle_group) SELECT DISTINCT exercise_name, muscle_group FROM plan_exercise WHERE exercise_name NOT IN (SELECT name FROM exercise);");
                
                // Migrate training_day to workout_preset
                db.execSQL("INSERT INTO workout_preset (id, name, created_at) SELECT id, day_name, CURRENT_TIMESTAMP FROM training_day WHERE is_rest = 0;");
                
                // Migrate preset_exercise with dynamic order_index mapping
                db.execSQL("INSERT INTO preset_exercise (id, preset_id, exercise_id, order_index) SELECT p.id, p.day_id, e.id, (SELECT COUNT(*) FROM plan_exercise p2 WHERE p2.day_id = p.day_id AND p2.id <= p.id) FROM plan_exercise p JOIN exercise e ON p.exercise_name = e.name JOIN workout_preset wp ON p.day_id = wp.id;");
                
                // Migrate session to workout_session
                db.execSQL("INSERT INTO workout_session (id, preset_id, start_time, end_time, status) SELECT id, NULL, date || ' ' || startzeit, date || ' ' || endzeit, 1 FROM session;");
                
                // Migrate session_exercise
                db.execSQL("INSERT INTO session_exercise (session_id, exercise_id, name, muscle_group, order_index) SELECT DISTINCT w.session_id, w.exercise_id, e.name, e.muscle_group, 0 FROM workout_set w JOIN exercise e ON w.exercise_id = e.id;");
                
                // Migrate exercise_set with sequential set_number via correlated subquery
                db.execSQL("INSERT INTO exercise_set (id, session_exercise_id, set_number, reps, weight, is_completed) SELECT w.id, se.id, (SELECT COUNT(*) FROM workout_set w2 WHERE w2.session_id = w.session_id AND w2.exercise_id = w.exercise_id AND w2.id <= w.id), w.reps, w.weight, 1 FROM workout_set w JOIN session_exercise se ON w.session_id = se.session_id AND w.exercise_id = se.exercise_id;");

                // 4. Drop legacy tables
                db.execSQL("DROP TABLE plan_exercise;");
                db.execSQL("DROP TABLE training_day;");
                db.execSQL("DROP TABLE workout_set;");
                db.execSQL("DROP TABLE session;");
                
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.e("WorkoutDbHelper", "Migration failed", e);
                throw e; // Abort migration and let it crash so no data is lost
            } finally {
                db.endTransaction();
            }
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }
}