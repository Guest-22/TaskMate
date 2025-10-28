package com.example.taskmate;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

// Helps locate autostart by phone model.
public class AutoStartHelper {

    public static void openAutoStartSettings(Context context) {
        String manufacturer = android.os.Build.MANUFACTURER.toLowerCase();
        Intent intent = new Intent();

        try { // Locate autostart directory inside setting.
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
}