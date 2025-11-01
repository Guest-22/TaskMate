package com.example.taskmate;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
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
        holder.txtDueDate.setText(formatDate(task.getDate())); // Format date for display.
        holder.txtDueTime.setText(task.getTime());

        Context context = holder.itemView.getContext();

        // Assigns background color to existing task/s.
        int bgColor = TaskColorAssigner.getBackgroundColor(context, task);
        holder.leftPanel.setBackgroundColor(bgColor);

        // Inside onBindViewHolder:
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddTaskActivity.class);

            // Pass all task info & redirect to AddTaskActivity (editMode == true) (Update & Delete).
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

    // Helper method to format date from yyyy-MM-dd to MMMM dd, yyyy.
    private String formatDate(String rawDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // Input format.
            Date date = inputFormat.parse(rawDate); // Parse raw date.
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()); // Output format.
            return outputFormat.format(date); // Return formatted date.
        } catch (ParseException e) {
            return rawDate; // Fallback to raw date if parsing fails.
        }
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
            txtTitle = itemView.findViewById(R.id.txtvTitle); // Title TextView.
            txtDescription = itemView.findViewById(R.id.txtvDescription); // Description TextView.
            txtDueDate = itemView.findViewById(R.id.txtvDueDate); // Due Date TextView.
            txtDueTime = itemView.findViewById(R.id.txtvDueTime); // Due Time TextView.
            leftPanel = itemView.findViewById(R.id.leftPanel); // Left Panel Color LinearLayout (SchedType: One-Time/Weekly).
        }
    }
}