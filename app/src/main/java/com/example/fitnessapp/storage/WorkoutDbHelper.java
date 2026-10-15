package com.example.fitnessapp.storage;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WorkoutDbHelper extends SQLiteOpenHelper {


    public static final String DATABASE_NAME = "fitness_tracker.db";
    public static final int DATABASE_VERSION = 1;

    // === Tabelle: Exercise ===
    public static final String TABLE_EXERCISE = "exercise";
    public static final String COL_EXERCISE_ID = "id";
    public static final String COL_EXERCISE_NAME = "name";

    // === Tabelle: Session ===
    public static final String TABLE_SESSION = "session";
    public static final String COL_SESSION_ID = "id";
    public static final String COL_SESSION_DATE = "date";
    public static final String COL_SESSION_STARTZEIT = "startzeit";
    public static final String COL_SESSION_ENDZEIT = "endzeit";

    // === Tabelle: WorkoutSet ===
    public static final String TABLE_SET = "workout_set";
    public static final String COL_SET_ID = "id";
    public static final String COL_SET_EXERCISE_ID = "exercise_id"; // Fremdschlüssel
    public static final String COL_SET_SESSION_ID = "session_id";   // Fremdschlüssel
    public static final String COL_SET_WEIGHT = "weight";
    public static final String COL_SET_REPS = "reps";

    public WorkoutDbHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }




    @Override
    public void onCreate(SQLiteDatabase db) {
        String createExerciseTable = "CREATE TABLE " + TABLE_EXERCISE + " ("
                + COL_EXERCISE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_EXERCISE_NAME + " TEXT NOT NULL);";

        String createSessionTable = "CREATE TABLE " + TABLE_SESSION + " ("
                + COL_SESSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SESSION_DATE + " TEXT, "
                + COL_SESSION_STARTZEIT + " TEXT, "
                + COL_SESSION_ENDZEIT + " TEXT);";

        String createSetTable = "CREATE TABLE " + TABLE_SET + " ("
                + COL_SET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SET_WEIGHT + " REAL, "
                + COL_SET_REPS + " INTEGER, "
                + COL_SET_EXERCISE_ID + " INTEGER, "
                + COL_SET_SESSION_ID + " INTEGER, "
                + "FOREIGN KEY(" + COL_SET_EXERCISE_ID + ") REFERENCES " + TABLE_EXERCISE + "(" + COL_EXERCISE_ID + "), "
                + "FOREIGN KEY(" + COL_SET_SESSION_ID + ") REFERENCES " + TABLE_SESSION + "(" + COL_SESSION_ID + "));";

        db.execSQL(createExerciseTable);
        db.execSQL(createSessionTable);
        db.execSQL(createSetTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SET);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSION);
        onCreate(db);

    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }



}
