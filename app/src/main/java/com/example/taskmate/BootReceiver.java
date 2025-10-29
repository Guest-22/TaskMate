package com.example.taskmate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
            Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(intent.getAction())) {
            
            LogHelper.d("TaskMateBootReceiver", "BOOT_COMPLETED received — restoring alarms...");
            AlarmScheduler.scheduleAllAlarms(context); // Reschedule all task alarms/notifs.
        }
    }
}