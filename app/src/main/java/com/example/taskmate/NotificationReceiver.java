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

        Log.d("NotifReceiver", "onReceive() fired for taskId=" + taskId);

        // Create notification channel
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

        // Tapping notification opens MainActivity
        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                taskId,
                openIntent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build and show notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(description)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        if (manager != null) {
            manager.notify(taskId, builder.build());
            Log.d("NotifReceiver", "Notification shown for taskId=" + taskId);
        }

        // DB helper for getting schedule-type (i.e., one-time or weekly).
        TaskDBHelper dbHelper = new TaskDBHelper(context);
        String scheduleType = dbHelper.getScheduleType(taskId);

        if ("One-time".equalsIgnoreCase(scheduleType)) {
            AlarmScheduler.cancelAlarm(context, taskId);
            Log.d("NotifReceiver", "One-time alarm canceled for taskId=" + taskId);
        }

        else if ("Weekly".equalsIgnoreCase(scheduleType)) {
            try {
                String lastDate = dbHelper.getTaskDate(taskId);
                String lastTime = dbHelper.getTaskTime(taskId);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US);
                Date lastDateTime = sdf.parse(lastDate + " " + lastTime);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(lastDateTime);
                calendar.add(Calendar.DAY_OF_YEAR, 7); // Push 7 days from last scheduled time

                // Cancel old alarm.
                AlarmScheduler.cancelAlarm(context, taskId);

                // Delete old task.
                dbHelper.deleteTask(taskId);

                // Format new date/time.
                String newDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime());
                String newTime = new SimpleDateFormat("hh:mm a", Locale.US).format(calendar.getTime());

                // Insert new task.
                int newTaskId = (int) dbHelper.insertTask(title, description, newDate, newTime, "Weekly");

                // Schedule new alarm
                AlarmScheduler.scheduleAlarm(context, newTaskId, title, description, newDate, newTime, true);

                Log.d("NotifReceiver", "Renewed weekly task: newTaskId=" + newTaskId + " for " + newDate + " " + newTime);

            } catch (Exception e) {
                Log.d("NotifReceiver", "Failed to renew weekly task for taskId=" + taskId, e);
            }
        }
    }
}