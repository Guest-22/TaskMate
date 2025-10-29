package com.example.taskmate;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

// Helps locate autostart by phone model.
public class AutoStartHelper {

    // Opens autostart settings directly.
    public static void openAutoStartSettings(Context context) {
        String manufacturer = android.os.Build.MANUFACTURER.toLowerCase();
        Intent intent = new Intent();

        try { // Helps user locate auto-start directory directly.
            switch (manufacturer) {
                case "vivo":
                    intent.setComponent(new ComponentName(
                            "com.vivo.permissionmanager",
                            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                    ));
                    break;
                case "tecno":
                    intent.setComponent(new ComponentName(
                            "com.transsion.phonemaster",
                            "com.cyin.himgr.autostart.AutoStartActivity"
                    ));
                    break;
                default: // Fallback: open general settings.
                    intent.setAction(Settings.ACTION_SETTINGS);
                    break;
            }
            context.startActivity(intent);

        } catch (Exception e) {
            // If everything fails, fallback to general settings.
            Intent fallback = new Intent(Settings.ACTION_SETTINGS);
            context.startActivity(fallback);
            Toast.makeText(context, "Please enable auto-start manually in settings.", Toast.LENGTH_LONG).show();
        }
    }

    // Ask the user manually if auto-start is enabled.
    public static void showAutoStartPrompt(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("taskmate_prefs", Context.MODE_PRIVATE);
        boolean autoStartEnabled = prefs.getBoolean("autostart_enabled", false);

        if (!autoStartEnabled) {
            new AlertDialog.Builder(context)
                    .setTitle("Enable Auto‑Start")
                    .setMessage("To make sure your reminders and alarms work after reboot, please allow TaskMate to auto‑start.")
                    .setPositiveButton("Go to Settings", (dialog, which) ->
                            openAutoStartSettings(context))
                    .setNegativeButton("Later", null)
                    .setNeutralButton("I’ve enabled it", (dialog, which) -> {
                        prefs.edit().putBoolean("autostart_enabled", true).apply();
                        Toast.makeText(context, "Thanks! Auto‑Start marked as enabled.", Toast.LENGTH_SHORT).show();
                    })
                    .show();
        }
    }
}