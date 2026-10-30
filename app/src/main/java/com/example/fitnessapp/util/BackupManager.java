package com.example.fitnessapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.fitnessapp.storage.WorkoutDbHelper;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

public class BackupManager {

    private static final String TAG = "BackupManager";
    private static final String DB_FILE_NAME = "database.db";
    private static final String METADATA_FILE_NAME = "metadata.json";
    private static final int BACKUP_FORMAT_VERSION = 1;
    private static final String SHA_ALGORITHM = "SHA-256";

    private final Context context;

    public BackupManager(Context context) {
        this.context = context;
    }

    public static class BackupResult {
        public boolean success;
        public String message;
        public BackupResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public BackupResult createBackup(OutputStream destStream, int dbWorkoutCount, int dbExerciseCount, int dbPresetCount) {
        File dbFile = context.getDatabasePath(WorkoutDbHelper.DATABASE_NAME);
        if (!dbFile.exists()) {
            return new BackupResult(false, "Keine Datenbankdatei gefunden.");
        }

        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(destStream))) {
            
            // 1. Add database file to zip
            zos.putNextEntry(new ZipEntry(DB_FILE_NAME));
            String dbSha256 = copyStreamAndCalculateSha256(new FileInputStream(dbFile), zos);
            zos.closeEntry();

            // 2. Create and add metadata JSON
            JSONObject meta = new JSONObject();
            meta.put("format_version", BACKUP_FORMAT_VERSION);
            meta.put("db_version", WorkoutDbHelper.DATABASE_VERSION);
            meta.put("created_at_ms", System.currentTimeMillis());
            meta.put("db_sha256", dbSha256);
            
            JSONObject stats = new JSONObject();
            stats.put("workouts", dbWorkoutCount);
            stats.put("exercises", dbExerciseCount);
            stats.put("presets", dbPresetCount);
            meta.put("stats", stats);

            // Export SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences("workout_settings", Context.MODE_PRIVATE);
            JSONObject prefsJson = new JSONObject();
            for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
                prefsJson.put(entry.getKey(), entry.getValue());
            }
            meta.put("preferences", prefsJson);

            zos.putNextEntry(new ZipEntry(METADATA_FILE_NAME));
            zos.write(meta.toString(4).getBytes("UTF-8"));
            zos.closeEntry();

            return new BackupResult(true, "Backup erfolgreich erstellt.");
            
        } catch (Exception e) {
            Log.e(TAG, "Backup failed", e);
            return new BackupResult(false, "Fehler beim Erstellen des Backups: " + e.getMessage());
        }
    }

    public static class RestoreMetadata {
        public boolean valid;
        public String message;
        public int workouts;
        public int exercises;
        public int presets;
        public long createdAtMs;
        public JSONObject preferences;
        public String dbSha256;
    }

    public RestoreMetadata inspectBackup(InputStream sourceStream) {
        RestoreMetadata result = new RestoreMetadata();
        result.valid = false;

        File cacheDir = context.getCacheDir();
        File tempDbFile = new File(cacheDir, "temp_restore.db");

        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(sourceStream))) {
            ZipEntry entry;
            String jsonString = null;
            boolean dbFound = false;

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(METADATA_FILE_NAME)) {
                    StringBuilder sb = new StringBuilder();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        sb.append(new String(buffer, 0, len, "UTF-8"));
                    }
                    jsonString = sb.toString();
                } else if (entry.getName().equals(DB_FILE_NAME)) {
                    // Extract temp db
                    try (FileOutputStream fos = new FileOutputStream(tempDbFile)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    dbFound = true;
                }
                zis.closeEntry();
            }

            if (!dbFound || jsonString == null) {
                result.message = "Ungültiges Backup-Format (Dateien fehlen).";
                return result;
            }

            JSONObject meta = new JSONObject(jsonString);
            int version = meta.optInt("format_version", -1);
            if (version > BACKUP_FORMAT_VERSION || version < 1) {
                result.message = "Inkompatible Backup-Version (" + version + ").";
                return result;
            }

            result.dbSha256 = meta.optString("db_sha256");
            
            // Verify checksum of extracted DB
            String tempDbSha256 = copyStreamAndCalculateSha256(new FileInputStream(tempDbFile), null);
            if (!tempDbSha256.equals(result.dbSha256)) {
                result.message = "Datenbankdatei ist beschädigt (Prüfsumme fehlerhaft).";
                return result;
            }

            JSONObject stats = meta.optJSONObject("stats");
            if (stats != null) {
                result.workouts = stats.optInt("workouts", 0);
                result.exercises = stats.optInt("exercises", 0);
                result.presets = stats.optInt("presets", 0);
            }
            result.createdAtMs = meta.optLong("created_at_ms", 0);
            result.preferences = meta.optJSONObject("preferences");

            result.valid = true;
            return result;

        } catch (Exception e) {
            Log.e(TAG, "Inspect backup failed", e);
            result.message = "Fehler beim Lesen der Backup-Datei.";
            return result;
        }
    }

    /**
     * Executes the actual restore. Assuming inspectBackup was called right before and tempDbFile is ready.
     * WARNING: Make sure all database connections in the app are closed before calling this!
     */
    public BackupResult executeRestore(JSONObject preferencesMeta) {
        File currentDbFile = context.getDatabasePath(WorkoutDbHelper.DATABASE_NAME);
        File walFile = new File(currentDbFile.getPath() + "-wal");
        File shmFile = new File(currentDbFile.getPath() + "-shm");

        File tempDbFile = new File(context.getCacheDir(), "temp_restore.db");
        if (!tempDbFile.exists()) {
            return new BackupResult(false, "Temporäre Datenbankdatei nicht gefunden.");
        }

        // We use a staging file first, to prevent corrupting current DB on I/O error
        File stagingFile = new File(context.getCacheDir(), "staging.db");
        
        try (FileInputStream fis = new FileInputStream(tempDbFile);
             FileOutputStream fos = new FileOutputStream(stagingFile)) {
             
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy DB during restore", e);
            if (stagingFile.exists()) stagingFile.delete();
            return new BackupResult(false, "Fehler beim Ersetzen der Datenbank (I/O Fehler).");
        }
        
        // Ensure valid SQLite structure on staging file before overwriting actual DB
        SQLiteDatabase testDb = null;
        try {
            testDb = SQLiteDatabase.openDatabase(stagingFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);
            // Simple integrity check
            Cursor c = testDb.rawQuery("PRAGMA integrity_check", null);
            if (c.moveToFirst()) {
                String res = c.getString(0);
                if (!"ok".equalsIgnoreCase(res)) {
                    throw new Exception("Integrity check failed: " + res);
                }
            }
            c.close();
        } catch (Exception e) {
            Log.e(TAG, "Invalid SQLite file", e);
            if (testDb != null && testDb.isOpen()) testDb.close();
            stagingFile.delete();
            return new BackupResult(false, "Wiederherstellung abgebrochen: Die Backup-Datenbank ist beschädigt oder ungültig.");
        } finally {
            if (testDb != null && testDb.isOpen()) testDb.close();
        }

        // Now safe to overwrite actual database
        if (currentDbFile.exists()) currentDbFile.delete();
        if (walFile.exists()) walFile.delete();
        if (shmFile.exists()) shmFile.delete();
        
        boolean renamed = stagingFile.renameTo(currentDbFile);
        if (!renamed) {
            // Fallback I/O copy if renameTo fails across partitions
            try (FileInputStream fis = new FileInputStream(stagingFile);
                 FileOutputStream fos = new FileOutputStream(currentDbFile)) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            } catch (IOException e) {
                return new BackupResult(false, "Kritischer Fehler beim Austauschen der Datenbankdatei.");
            }
        }

        // Restore SharedPreferences
        if (preferencesMeta != null) {
            SharedPreferences prefs = context.getSharedPreferences("workout_settings", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            
            java.util.Iterator<String> keys = preferencesMeta.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    Object value = preferencesMeta.get(key);
                    if (value instanceof Boolean) {
                        editor.putBoolean(key, (Boolean) value);
                    } else if (value instanceof Integer) {
                        editor.putInt(key, (Integer) value);
                    } else if (value instanceof String) {
                        editor.putString(key, (String) value);
                    } else if (value instanceof Long) {
                        editor.putLong(key, (Long) value);
                    } else if (value instanceof Float) {
                        editor.putFloat(key, (Float) value);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing preference: " + key, e);
                }
            }
            editor.apply();
        }

        // Cleanup
        tempDbFile.delete();
        if (stagingFile.exists()) stagingFile.delete();

        return new BackupResult(true, "Wiederherstellung erfolgreich.");
    }

    private String copyStreamAndCalculateSha256(InputStream is, OutputStream os) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(SHA_ALGORITHM);
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
            if (os != null) {
                os.write(buffer, 0, bytesRead);
            }
        }
        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}