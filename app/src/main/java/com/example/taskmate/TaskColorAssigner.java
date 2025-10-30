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

    // Assigns dot icon color in Material Calendar View.
    public static int getDotIcon(Context context, List<Task> sameDayTasks) {
        boolean hasWeekly = false;
        boolean hasOneTime = false;
        long nowMillis = System.currentTimeMillis(); // Get current time.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US); // DATETIME format (e.g., 2025-10-30 12:00 PM).

        for (Task t : sameDayTasks) {
            if ("Weekly".equalsIgnoreCase(t.getType())) {
                hasWeekly = true;
            } else {
                hasOneTime = true;
            }
        }

        // Dual-dot logic: If the date contains both weekly & one-time.
        if (hasWeekly && hasOneTime) {
            boolean hasUpcoming = false; // Track if any one-time is still upcoming.
            for (Task t : sameDayTasks) {
                if (!"Weekly".equalsIgnoreCase(t.getType())) {
                    try {
                        String dateTimeString = t.getDate() + " " + t.getTime(); // Get date & time (e.g., 2025-10-30 12:00 PM).
                        Date parsedDate = sdf.parse(dateTimeString);
                        long diffMillis = parsedDate.getTime() - nowMillis; // Compare due_time to our current_time.
                        long diffDays = diffMillis / (1000L * 60L * 60L * 24L);

                        if (diffMillis >= 0) { // Found at least one upcoming.
                            hasUpcoming = true;
                            if (diffDays <= 3) return R.drawable.dot_dual_red;
                            else if (diffDays <= 7) return R.drawable.dot_dual_yellow;
                            else return R.drawable.dot_dual_green;
                        }

                    } catch (Exception e) {
                        LogHelper.e("TaskColorAssigner", "getDotIcon: Failed to parse datetime for dual-dot taskId=" + t.getId(), e);
                        return R.drawable.dot_dual_red;
                    }
                }
            }
            if (!hasUpcoming)
                return R.drawable.dot_dual_gray; // Return gray only if no upcoming one-time left.
        }

        // Single-dot logic: If the day contains one task (assign them a dot as per their criteria).
        // Weekly = blue & Onetime = gray (past due), red (3 days left), yellow (7 days left), and green (more than 7 days left).
        Task reference = sameDayTasks.get(0);
        if ("Weekly".equalsIgnoreCase(reference.getType())) {
            return R.drawable.dot_blue;
        }

        try {
            boolean hasUpcoming = false; // Tracks if any one-time is still upcoming.
            for (Task t : sameDayTasks) { // Check all one-time tasks.
                String dateTimeString = t.getDate() + " " + t.getTime(); // DATETIME format (e.g., 2025-10-30 12:00 PM).
                Date parsedDate = sdf.parse(dateTimeString);
                long diffMillis = parsedDate.getTime() - nowMillis;
                long diffDays = diffMillis / (1000L * 60L * 60L * 24L);

                if (diffMillis >= 0) {
                    hasUpcoming = true;
                    if (diffDays <= 3) return R.drawable.dot_red;
                    else if (diffDays <= 7) return R.drawable.dot_yellow;
                    else return R.drawable.dot_green;
                }
            }

            if (!hasUpcoming)
                return R.drawable.dot_gray; // Return gray only if no upcoming one-time left.
        } catch (Exception e) {
            LogHelper.e("TaskColorAssigner", "getDotIcon: Failed to parse datetime for single-dot taskId=" + reference.getId(), e);
            return R.drawable.dot_red; // Fallback to red if parsing fails.
        }
        return R.drawable.dot_red; // Last fallback, in case something went wrong.
    }
}