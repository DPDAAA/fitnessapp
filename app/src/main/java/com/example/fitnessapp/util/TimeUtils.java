package com.example.fitnessapp.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

    public static long parseTimeMs(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return 0;
        try {
            return Long.parseLong(timeStr);
        } catch (NumberFormatException e) {
            try {
                String clean = timeStr.replace("T", " ");
                if (clean.length() > 19) clean = clean.substring(0, 19);
                SimpleDateFormat sdf;
                if (clean.length() == 16) {
                    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                } else if (clean.length() == 19) {
                    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                } else {
                    return 0;
                }
                Date d = sdf.parse(clean);
                return d != null ? d.getTime() : 0;
            } catch (Exception ex) {
                return 0;
            }
        }
    }

    public static String formatDate(long timeMs) {
        if (timeMs == 0) return "-";
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        return sdf.format(new Date(timeMs));
    }

    public static String formatTime(long timeMs) {
        if (timeMs == 0) return "-";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timeMs));
    }

    public static String formatDuration(long startMs, long endMs) {
        if (startMs == 0 || endMs == 0 || endMs < startMs) return "-";
        long diff = endMs - startMs;
        int hours = (int) (diff / (1000 * 60 * 60));
        int mins = (int) ((diff / (1000 * 60)) % 60);
        if (hours > 0) return hours + "h " + mins + "m";
        return mins + "m";
    }
    
    public static String formatDurationMs(long diff) {
        if (diff <= 0) return "-";
        int hours = (int) (diff / (1000 * 60 * 60));
        int mins = (int) ((diff / (1000 * 60)) % 60);
        if (hours > 0) return hours + "h " + mins + "m";
        return mins + "m";
    }

    public static long[] getLast7WeeksStartMs() {
        long[] starts = new long[7];
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setFirstDayOfWeek(java.util.Calendar.MONDAY);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        
        while (cal.get(java.util.Calendar.DAY_OF_WEEK) != java.util.Calendar.MONDAY) {
            cal.add(java.util.Calendar.DAY_OF_MONTH, -1);
        }
        
        cal.add(java.util.Calendar.WEEK_OF_YEAR, -6);
        
        for (int i = 0; i < 7; i++) {
            starts[i] = cal.getTimeInMillis();
            cal.add(java.util.Calendar.WEEK_OF_YEAR, 1);
        }
        return starts;
    }

    public static String[] getLast7WeeksLabels(long[] startMsArray) {
        String[] labels = new String[7];
        java.util.Calendar cal = java.util.Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            cal.setTimeInMillis(startMsArray[i]);
            labels[i] = "KW" + cal.get(java.util.Calendar.WEEK_OF_YEAR);
        }
        return labels;
    }

    public static long getStartOfLast7DaysMs() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, -7);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long getStartOfCurrentMonthMs() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    
    public static long getStartOfCurrentWeekMs() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setFirstDayOfWeek(java.util.Calendar.MONDAY);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        while (cal.get(java.util.Calendar.DAY_OF_WEEK) != java.util.Calendar.MONDAY) {
            cal.add(java.util.Calendar.DAY_OF_MONTH, -1);
        }
        return cal.getTimeInMillis();
    }
}