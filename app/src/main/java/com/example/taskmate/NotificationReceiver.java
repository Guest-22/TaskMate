package com.example.taskmate;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.net.ParseException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationReceiver extends BroadcastReceiver {
    @SuppressLint("ScheduleExactAlarm")
    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra("taskId", -1);
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");

        // Create Notification Channel (Android 8+)
        String channelId = "taskmate_channel";
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "TaskMate Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        // Intent to open MainActivity when notification is tapped
        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                taskId,
                openIntent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(description)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        // Show notification
        if (manager != null) {
            manager.notify(taskId, builder.build());
            Log.d("NotificationReceiver", "Notification shown for taskId: " + taskId);
        }

        // Auto-cancel one-time alarms after triggering
        TaskDBHelper dbHelper = new TaskDBHelper(context);
        String scheduleType = dbHelper.getScheduleType(taskId);
        if ("One-time".equalsIgnoreCase(scheduleType)) {
            // cancel the pending alarm (safety) — repeating alarms remain
            AlarmScheduler.cancelAlarm(context, taskId);
            Log.d("NotificationReceiver", "Auto-canceled one-time alarm for taskId: " + taskId);
        }
        // NEWCODE
        // ()NEWCODE - Manual weekly rescheduling logic
        else if ("Weekly".equalsIgnoreCase(scheduleType)) {
            Calendar nextWeek = Calendar.getInstance();
            nextWeek.setTimeInMillis(System.currentTimeMillis());
            nextWeek.add(Calendar.DAY_OF_YEAR, 7); // Add 7 days

            // Set time to same hour/minute as original
            String time = dbHelper.getTaskTime(taskId); // e.g., "12:00 PM"
            Log.d("AlarmDebug", "Time string from DB: " + time); // Debug log to verify time format
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                Date parsedTime = sdf.parse(time);
                Calendar timeOnly = Calendar.getInstance();
                timeOnly.setTime(parsedTime);
                nextWeek.set(Calendar.HOUR_OF_DAY, timeOnly.get(Calendar.HOUR_OF_DAY));
                nextWeek.set(Calendar.MINUTE, timeOnly.get(Calendar.MINUTE));
                nextWeek.set(Calendar.SECOND, 0);
                nextWeek.set(Calendar.MILLISECOND, 0);
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (java.text.ParseException e) {
                throw new RuntimeException(e);
            }

            // Create intent for next week's alarm
            Intent newIntent = new Intent(context, NotificationReceiver.class);
            newIntent.setAction("TASK_ALARM_" + taskId);
            newIntent.putExtra("taskId", taskId);
            newIntent.putExtra("title", title);
            newIntent.putExtra("description", description);

            PendingIntent newPendingIntent = PendingIntent.getBroadcast(
                    context,
                    taskId,
                    newIntent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextWeek.getTimeInMillis(),
                    newPendingIntent
            );

            Log.d("NotificationReceiver", "Rescheduled weekly alarm for taskId: " + taskId + " at " + nextWeek.getTime());
        }
        // ()NEWCODE
    }
}
