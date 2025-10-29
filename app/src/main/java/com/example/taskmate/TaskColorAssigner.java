package com.example.taskmate;

import android.content.Context;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskColorAssigner {

    // Assigns bg color in task list view.
    public static int getBackgroundColor(Context context, Task task) {
        String taskColor = "green"; // default

        if (task.getType().equalsIgnoreCase("Weekly")) {
            taskColor = "weekly";
        } else {
            try {
                String dateTimeString = task.getDate() + " " + task.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US);
                Date dueDate = sdf.parse(dateTimeString);

                long now = System.currentTimeMillis();
                if (dueDate.getTime() < now) {
                    taskColor = "gray"; // Assings gray for past one-time task/s.
                } else {
                    long diffMillis = dueDate.getTime() - now;
                    long diffDays = diffMillis / (1000 * 60 * 60 * 24);

                    if (diffDays <= 3) {
                        taskColor = "red";
                    } else if (diffDays <= 7) {
                        taskColor = "yellow";
                    } else {
                        taskColor = "green";
                    }
                }
            } catch (Exception e) {
                LogHelper.e("TaskColorAssigner", "getBackgroundColor: Failed to parse date/time for taskId=" + task.getId(), e);
                taskColor = "green";
            }
        }

        // Assigns color based on sched type and datetime.
        switch (taskColor) {
            case "weekly":
                return ContextCompat.getColor(context, R.color.blue);
            case "red":
                return ContextCompat.getColor(context, R.color.red);
            case "yellow":
                return ContextCompat.getColor(context, R.color.yellow);
            case "gray":
                return ContextCompat.getColor(context, R.color.gray);
            case "green":
            default:
                return ContextCompat.getColor(context, R.color.green);
        }
    }

    public static int getDotIcon(Context context, List<Task> sameDayTasks) {
        boolean hasWeekly = false;
        boolean hasOneTime = false;
        long todayMillis = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        for (Task t : sameDayTasks) {
            if ("Weekly".equalsIgnoreCase(t.getType())) {
                hasWeekly = true;
            } else {
                hasOneTime = true;
            }
        }

        // Dual-dot logic
        if (hasWeekly && hasOneTime) {
            for (Task t : sameDayTasks) {
                if (!"Weekly".equalsIgnoreCase(t.getType())) {
                    try {
                        Date parsedDate = sdf.parse(t.getDate());
                        long diffMillis = parsedDate.getTime() - todayMillis;
                        long diffDays = diffMillis / (1000L * 60L * 60L * 24L);

                        if (diffDays <= 3) return R.drawable.dot_dual_red;
                        else if (diffDays <= 7) return R.drawable.dot_dual_yellow;
                        else return R.drawable.dot_dual_green;

                    } catch (Exception e) {
                        LogHelper.e("TaskColorAssigner", "getDotIcon: Failed to parse date for dual-dot taskId=" + t.getId(), e);
                        return R.drawable.dot_dual_red;
                    }
                }
            }
        }

        // Single-dot logic
        Task reference = sameDayTasks.get(0);
        if ("Weekly".equalsIgnoreCase(reference.getType())) {
            return R.drawable.dot_blue;
        }

        try {
            Date parsedDate = sdf.parse(reference.getDate());
            long diffMillis = parsedDate.getTime() - todayMillis;
            long diffDays = diffMillis / (1000L * 60L * 60L * 24L);

            if (diffDays <= 3) return R.drawable.dot_red;
            else if (diffDays <= 7) return R.drawable.dot_yellow;
            else return R.drawable.dot_green;

        } catch (Exception e) {
            LogHelper.e("TaskColorAssigner", "getDotIcon: Failed to parse date for single-dot taskId=" + reference.getId(), e);
            return R.drawable.dot_green;
        }
    }
}