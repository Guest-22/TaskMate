package com.example.taskmate;

import android.util.Log;

public class LogHelper {
    // Toggle logcat on/off globally.
    public static final boolean ENABLE_LOGS = false;

    // Debug log.
    public static void d(String tag, String message) {
        if (ENABLE_LOGS) Log.d(tag, message);
    }

    // Error log with exception.
    public static void e(String tag, String message, Throwable throwable) {
        if (ENABLE_LOGS) Log.e(tag, message, throwable);
    }
}