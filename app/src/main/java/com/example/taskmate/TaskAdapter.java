package com.example.taskmate;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Adapter class to connect Task data with RecyclerView items.
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    // List to hold all Task objects.
    private List<Task> taskList;

    // Constructor to initialize the adapter with a list of tasks.
    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    // Called when RecyclerView needs a new ViewHolder.
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each task item.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view); // Return a new ViewHolder with the inflated view.
    }

    // Called to bind data to the ViewHolder at a specific position.
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        // Get the Task object at the current position.
        Task task = taskList.get(position);

        // Set the task details into the corresponding TextViews.
        holder.txtTitle.setText(task.getTitle());
        holder.txtDescription.setText(task.getDescription());
        holder.txtDueDate.setText("Due on: " + task.getDate());
        holder.txtDueTime.setText(task.getTime());

        // -------------------------------------------------------------------------------------------------------------------------
        // FUTURE UPDATES: THE COLOR WILL VARY ON DEADLINE FOR ONE_TIME SCHEDS.
        // Set color based on schedule type: weekly = blue & one-time = green.
        Context context = holder.itemView.getContext();
        String taskColor = "GREEN"; // default

        if (task.getType().equalsIgnoreCase("Weekly")) {
            taskColor = "weekly";
        } else {
            // One-time, compute the color based on deadline.
            try {
                String dateTimeString = task.getDate() + " " + task.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US);
                Date dueDate = sdf.parse(dateTimeString);

                // Use to compare dates and time; assigns color-coded colors for urgency levels.
                // Red (3 days before deadline), Yellow (7 days before deadline), Green (More than 7 days before the deadline).
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

        // Use switch statement to apply background color for each tasks.
        switch (taskColor) {
            case "weekly": // Weekly task; default color.
                holder.leftPanel.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));
                break;
            case "red": // One-time task; 3 days before deadline.
                holder.leftPanel.setBackgroundColor(ContextCompat.getColor(context, R.color.red));
                break;
            case "yellow": // One-time task; 7 days before deadline.
                holder.leftPanel.setBackgroundColor(ContextCompat.getColor(context, R.color.yellow));
                break;
            case "green": // One-time task; more than 7 days before deadline.
            default: // One-time task; default color.
                holder.leftPanel.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
                break;
        }

        // Inside onBindViewHolder:
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddTaskActivity.class);

            // Pass full task data/info and redirects to AddTaskActivity (editMode == true).
            intent.putExtra("isEdit", true);
            intent.putExtra("taskId", task.getId());
            intent.putExtra("title", task.getTitle());
            intent.putExtra("description", task.getDescription());
            intent.putExtra("date", task.getDate());
            intent.putExtra("time", task.getTime());
            intent.putExtra("scheduleType", task.getType());

            context.startActivity(intent);
        });
    }

    // Returns the total number of items in the list.
    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // ViewHolder class to hold references to the views in each item.
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDescription, txtDueDate, txtDueTime;
        LinearLayout leftPanel;

        // Constructor that finds and stores view references.
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtvTitle);               // Title TextView.
            txtDescription = itemView.findViewById(R.id.txtvDescription);   // Description TextView.
            txtDueDate = itemView.findViewById(R.id.txtvDueDate);           // Due Date TextView.
            txtDueTime = itemView.findViewById(R.id.txtvDueTime);           // Due Time TextView.
            leftPanel = itemView.findViewById(R.id.leftPanel);              // Left Panel Color LinearLayout.
        }
    }
}