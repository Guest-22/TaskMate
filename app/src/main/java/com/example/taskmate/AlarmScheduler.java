/*package com.example.taskmate;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AlarmScheduler {

    // Schedule an alarm for a specific task
    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleAlarm(Context context, int taskId, String title, String description, String date, String time) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Convert date and time (like 2025-10-08, 11:30 AM) to Calendar
        Calendar calendar = Calendar.getInstance();
        try {
            // Convert 12-hour format string into a proper date-time object
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US);
            calendar.setTime(sdf.parse(date + " " + time));
        } catch (ParseException e) {
            e.printStackTrace();
            return; // stop if invalid date/time
        }

        // Intent to trigger NotificationReceiver
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("taskId", taskId);
        intent.putExtra("title", title);
        intent.putExtra("description", description);

        // Each task gets its own PendingIntent using its ID as a unique code
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Schedule the alarm
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    // Cancel alarm if task is deleted or updated
    public static void cancelAlarm(Context context, int taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
*/

package com.example.taskmate;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AlarmScheduler {

    // ()NEWCODE - Add constant for weekly interval (7 days)
    private static final long WEEK_INTERVAL = 7 * 24 * 60 * 60 * 1000L; // 7 days in milliseconds

    /**
     * Schedule an alarm for a specific task.
     * @param context       App context
     * @param taskId        Unique task ID
     * @param title         Notification title
     * @param description   Notification description
     * @param date          Date in "yyyy-MM-dd"
     * @param time          Time in "hh:mm a" (e.g., "07:30 PM")
     * @param isWeekly      If true, schedule repeats every week
     */
    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleAlarm(Context context, int taskId, String title, String description, String date, String time, boolean isWeekly) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Convert date and time (e.g., 2025-10-08 11:30 AM) into a Calendar
        Calendar calendar = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US);
            calendar.setTime(sdf.parse(date + " " + time));
        } catch (ParseException e) {
            e.printStackTrace();
            return; // stop if invalid date/time
        }

        // Prevent past alarms
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            return;
        }

        // Create intent for NotificationReceiver
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("taskId", taskId);
        intent.putExtra("title", title);
        intent.putExtra("description", description);

        // Create unique PendingIntent using task ID
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            if (isWeekly) {
                // Schedule repeating alarm every 7 days
                alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        WEEK_INTERVAL,
                        pendingIntent
                );
            } else {
                // One-time schedule
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        }
    }

    // Cancel alarm when task is deleted or updated
    public static void cancelAlarm(Context context, int taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
