package com.example.taskmate;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AlarmScheduler {

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

        /* Debugging purposes only; used for showing all alarms/notifs.
        logScheduledTasksFromDb(context);
         */

        // Convert date and time (e.g., 2025-10-08 11:30 AM) into a Calendar
        Calendar calendar = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US);
            calendar.setTime(sdf.parse(date + " " + time));
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d("AlarmScheduler", "Invalid date/time parse for taskId " + taskId + ": " + date + " " + time);
            return; // Stop if invalid date/time.
        }

        // Prevent past alarms.
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            Log.d("AlarmScheduler", "Attempted to schedule past alarm for taskId " + taskId);
            return;
        }

        // Create intent for NotificationReceiver.
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction("TASK_ALARM_" + taskId); // important for cancel matching.
        intent.putExtra("taskId", taskId);
        intent.putExtra("title", title);
        intent.putExtra("description", description);

        // Create unique PendingIntent using task ID.
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        if (alarmManager != null) {
            if (isWeekly) {
                // Schedule repeating notifs every 7 days.
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
                Log.d("AlarmScheduler", "Scheduled weekly alarm for taskId: " + taskId + " at " + calendar.getTime());
            } else {
                // One-time schedule.
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
                Log.d("AlarmScheduler", "Scheduled one-time alarm for taskId: " + taskId + " at " + calendar.getTime());
            }
        }
    }

    // Cancels existing alarm when task is deleted or updated.
    public static void cancelAlarm(Context context, int taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Must exactly match the intent used in scheduleAlarm.
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction("TASK_ALARM_" + taskId); // Ensures identity match.

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE // No new PendingIntent is created.
        );

        // Handles cancellation of existing alarm.
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent); // Cancels scheduled alarm.
            pendingIntent.cancel(); // Extra safety: removes from system.
            Log.d("AlarmScheduler", "Alarm canceled for taskId: " + taskId);
        } else {
            Log.d("AlarmScheduler", "No alarm found to cancel for taskId: " + taskId);
        }
    }

    // Retrieve and shows a of all existing task and their infos inside logcat for debugging purposes.
    public static void logScheduledTasksFromDb(Context context) {
        TaskDBHelper db = new TaskDBHelper(context);
        for (Task t : db.getAllTasks()) {
            String schedule = t.getType();
            Log.d("AlarmScheduler", "DB Task id=" + t.getId() + " title='" + t.getTitle() + "' date=" + t.getDate() + " time=" + t.getTime() + " type=" + schedule);
        }
    }
}