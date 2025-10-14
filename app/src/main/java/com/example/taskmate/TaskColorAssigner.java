package com.example.taskmate;

import android.content.Context;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskColorAssigner {

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
                long diffMillis = dueDate.getTime() - now;
                long diffDays = diffMillis / (1000 * 60 * 60 * 24);

                if (diffDays <= 3) {
                    taskColor = "red";
                } else if (diffDays <= 7) {
                    taskColor = "yellow";
                } else {
                    taskColor = "green";
                }
            } catch (Exception e) {
                e.printStackTrace();
                taskColor = "green";
            }
        }

        switch (taskColor) {
            case "weekly":
                return ContextCompat.getColor(context, R.color.blue);
            case "red":
                return ContextCompat.getColor(context, R.color.red);
            case "yellow":
                return ContextCompat.getColor(context, R.color.yellow);
            case "green":
            default:
                return ContextCompat.getColor(context, R.color.green);
        }
    }
}
