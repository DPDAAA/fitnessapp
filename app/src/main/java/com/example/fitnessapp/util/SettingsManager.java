package com.example.fitnessapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Locale;

public class SettingsManager {
    private static final String PREF_NAME = "workout_settings";
    
    // Keys
    private static final String KEY_TIMER_DURATION_SEC = "timer_duration_sec";
    private static final String KEY_AUTO_TIMER = "timer_auto_start";
    private static final String KEY_WEIGHT_UNIT = "weight_unit"; // "kg" or "lb"
    private static final String KEY_VIBRATE = "vibrate_enabled";
    private static final String KEY_KEEP_SCREEN_ON = "keep_screen_on";

    // Defaults
    private static final int DEFAULT_TIMER_SEC = 90;
    private static final boolean DEFAULT_AUTO_TIMER = true;
    private static final String DEFAULT_WEIGHT_UNIT = "kg";
    private static final boolean DEFAULT_VIBRATE = true;
    private static final boolean DEFAULT_KEEP_SCREEN_ON = false;

    private static final double KG_TO_LB = 2.20462262;

    private SharedPreferences prefs;

    public SettingsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public int getTimerDurationSec() {
        return prefs.getInt(KEY_TIMER_DURATION_SEC, DEFAULT_TIMER_SEC);
    }

    public void setTimerDurationSec(int seconds) {
        prefs.edit().putInt(KEY_TIMER_DURATION_SEC, seconds).apply();
    }
    
    public long getTimerDurationMs() {
        return getTimerDurationSec() * 1000L;
    }

    public boolean isAutoTimerEnabled() {
        return prefs.getBoolean(KEY_AUTO_TIMER, DEFAULT_AUTO_TIMER);
    }

    public void setAutoTimerEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_TIMER, enabled).apply();
    }

    public String getWeightUnit() {
        return prefs.getString(KEY_WEIGHT_UNIT, DEFAULT_WEIGHT_UNIT);
    }

    public void setWeightUnit(String unit) {
        if ("kg".equalsIgnoreCase(unit) || "lb".equalsIgnoreCase(unit)) {
            prefs.edit().putString(KEY_WEIGHT_UNIT, unit.toLowerCase(Locale.US)).apply();
        }
    }
    
    public boolean isUnitLb() {
        return "lb".equals(getWeightUnit());
    }

    public boolean isVibrateEnabled() {
        return prefs.getBoolean(KEY_VIBRATE, DEFAULT_VIBRATE);
    }

    public void setVibrateEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_VIBRATE, enabled).apply();
    }

    public boolean isKeepScreenOnEnabled() {
        return prefs.getBoolean(KEY_KEEP_SCREEN_ON, DEFAULT_KEEP_SCREEN_ON);
    }

    public void setKeepScreenOnEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, enabled).apply();
    }

    // Weight Conversion Utilities
    
    /**
     * Converts a database kg weight to the currently selected display unit.
     */
    public double convertToDisplay(double kg) {
        if (isUnitLb()) {
            return kg * KG_TO_LB;
        }
        return kg;
    }

    /**
     * Converts an inputted display weight back to kg for database storage.
     */
    public double convertToKg(double displayWeight) {
        if (isUnitLb()) {
            return displayWeight / KG_TO_LB;
        }
        return displayWeight;
    }

    /**
     * Formats a kg weight for display with the current unit label.
     */
    public String formatWeightDisplay(double kg) {
        double display = convertToDisplay(kg);
        String label = isUnitLb() ? " lb" : " kg";
        // Round to 1 decimal place max
        if (display == Math.floor(display)) {
            return String.format(Locale.US, "%.0f%s", display, label);
        } else {
            return String.format(Locale.US, "%.1f%s", display, label);
        }
    }
    
    /**
     * Formats a kg weight for EditText inputs (no unit label).
     */
    public String formatWeightInput(double kg) {
        double display = convertToDisplay(kg);
        if (display == Math.floor(display)) {
            return String.format(Locale.US, "%.0f", display);
        } else {
            return String.format(Locale.US, "%.1f", display);
        }
    }
}