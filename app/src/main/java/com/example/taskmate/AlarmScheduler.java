package com.example.taskmate;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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

    // Schedules alarm at exact date & time.
    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleAlarm(Context context, int taskId, String title, String description, String date, String time, boolean isWeekly) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Convert date and time (e.g., 2025-10-08 11:30 AM) into a Calendar
        Calendar calendar = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US);
            calendar.setTime(sdf.parse(date + " " + time));
        } catch (ParseException e) {
            LogHelper.e("AlarmScheduler", "Invalid date/time parse for taskId " + taskId + ": " + date + " " + time, e);
            return; // Stop if invalid date/time.
        }

        // Prevent past alarms.
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            LogHelper.d("AlarmScheduler", "Attempted to schedule past alarm for taskId " + taskId);
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

        // Schedules notification alarm.
        if (alarmManager != null) {
            if (isWeekly) {
                // Schedule repeating notifs every 7 days.
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
                LogHelper.d("AlarmScheduler", "Scheduled weekly alarm for taskId: " + taskId + " at " + calendar.getTime());
            } else {
                // One-time schedule.
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
                LogHelper.d("AlarmScheduler", "Scheduled one-time alarm for taskId: " + taskId + " at " + calendar.getTime());
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
            LogHelper.d("AlarmScheduler", "Alarm canceled for taskId: " + taskId);
        } else {
            LogHelper.d("AlarmScheduler", "No alarm found to cancel for taskId: " + taskId);
        }
    }

    // Get all scheduled alarms/notifs to be rescheduled after boot/restart.
    public static void scheduleAllAlarms(Context context) {
        TaskDBHelper dbHelper = new TaskDBHelper(context);
        List<Task> allTasks = dbHelper.getAllTasks();

        if (allTasks == null || allTasks.isEmpty()) {
            LogHelper.d("AlarmScheduler", "No tasks found to reschedule.");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US);
        Date now = new Date();

        for (Task task : allTasks) {
            try {
                Date taskDateTime = sdf.parse(task.getDate() + " " + task.getTime());

                if (taskDateTime != null && taskDateTime.after(now)) {
                    boolean isWeekly = "Weekly".equalsIgnoreCase(task.getType());

                    scheduleAlarm(
                            context,
                            task.getId(),
                            task.getTitle(),
                            task.getDescription(),
                            task.getDate(),
                            task.getTime(),
                            isWeekly
                    );

                    LogHelper.d("AlarmScheduler", "Rescheduled taskId=" + task.getId() + " (" + task.getTitle() + ") for " + task.getDate() + " " + task.getTime());
                } else {
                    LogHelper.d("AlarmScheduler", "Skipped past taskId=" + task.getId());
                }

            } catch (Exception e) {
                LogHelper.e("AlarmScheduler", "Failed to reschedule taskId=" + task.getId(), e);
            }
        }
        LogHelper.d("AlarmScheduler", "All future alarms restored after reboot.");
    }

    /* Retrieve and shows a of all existing task and their infos inside logcat for debugging purposes.
    public static void logScheduledTasksFromDb(Context context) {
        TaskDBHelper db = new TaskDBHelper(context);
        for (Task t : db.getAllTasks()) {
            String schedule = t.getType();
            LogHelper.d("AlarmScheduler", "DB Task id=" + t.getId() + " title='" + t.getTitle() + "' date=" + t.getDate() + " time=" + t.getTime() + " type=" + schedule);
        }
    }*/
}