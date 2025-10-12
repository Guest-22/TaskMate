package com.example.taskmate;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    // Requesting for notification permission (Unique ID per permission req).
    private static final int NOTIFICATION_PERMISSION_CODE = 1001;

    // Declaring all variables.
    TimePickerDialog timePickerDialog;
    Calendar calendar = Calendar.getInstance();
    int currentHour;
    int currentMinute;
    String amPm;

    private EditText txteTitle, txteDescription, datePicker, timePicker;
    private RadioGroup rgSchedType;
    private RadioButton rbOneTime, rbWeekly;
    private Button btnSave, btnExportDB, btnDelete;
    private Toast activeToast; // For preventing toast stacks; removes the current toast & replace with a new toast message.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_task);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize all variables.
        txteTitle = findViewById(R.id.txteTitle);
        txteDescription = findViewById(R.id.txteDescription);
        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);
        rgSchedType = findViewById(R.id.rgSchedType);
        rbOneTime = findViewById(R.id.rbOneTime);
        rbWeekly = findViewById(R.id.rbWeekly);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        rbOneTime.setChecked(true); // Automatically sets one-time as default selected option
        // -------------------------------------------------------------------------------------------------------------------------
        // DEBUGGING SQLITE: MUST BE HIDDEN BEFORE APP LAUNCH.
        btnExportDB = findViewById(R.id.btnExportDB); // Export SQLiteDB button
        // DEBUGGING SQLITE: MUST BE HIDDEN BEFORE APP LAUNCH.
        // -------------------------------------------------------------------------------------------------------------------------

        // -------------------------------------------------------------------------------------------------------------------------
        // DEBUGGING SQLITE: MUST BE HIDDEN BEFORE APP LAUNCH.
        // Export SQLiteDB button logic
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1002);
        btnExportDB.setOnClickListener(v -> exportDatabaseToDownload());
        // DEBUGGING SQLITE: MUST BE HIDDEN BEFORE APP LAUNCH.
        // -------------------------------------------------------------------------------------------------------------------------

        // Creating an instance of TaskDBHelper class to use SQLite operations (i.e., CRUD Operations).
        TaskDBHelper dbHelper = new TaskDBHelper(this);

        // -------------------------------------------------------------------------------------------------------------------------
        // EDIT MODE; OVERRIDES ADD TASK'S LAYOUT.
        // Checks if this activity was opened for EDIT MODE.
        boolean isEdit = getIntent().getBooleanExtra("isEdit", false);

        if (isEdit) {
            // Change button text and sets the title to Edit Task.
            btnSave.setText("Update");
            getSupportActionBar().setTitle("Edit Task");

            // Get data from Intent and pre-fill input fields/EditText.
            int taskId = getIntent().getIntExtra("taskId", -1);
            String title = getIntent().getStringExtra("title");
            String description = getIntent().getStringExtra("description");
            String date = getIntent().getStringExtra("date");
            String time = getIntent().getStringExtra("time");
            String scheduleType = getIntent().getStringExtra("scheduleType");

            txteTitle.setText(title);
            txteDescription.setText(description);
            datePicker.setText(date);
            timePicker.setText(time);

            // Set RadioButton selection.
            if ("Weekly".equalsIgnoreCase(scheduleType)) {
                rbWeekly.setChecked(true);
            } else {
                rbOneTime.setChecked(true);
            }

            // Override save button to update instead of insert.
            btnSave.setOnClickListener(v -> {
                String newTitle = txteTitle.getText().toString().trim();
                String newDescription = txteDescription.getText().toString().trim();
                String newDate = datePicker.getText().toString().trim();
                String newTime = timePicker.getText().toString().trim();

                int selectedId = rgSchedType.getCheckedRadioButtonId();
                String newScheduleType = (selectedId == rbOneTime.getId()) ? "One-time" : "Weekly";
                boolean isWeekly = newScheduleType.equalsIgnoreCase("Weekly"); //

                if (newTitle.isEmpty() || newDescription.isEmpty() || newDate.isEmpty() || newTime.isEmpty()) {
                    showToast("Please fill in all fields");
                } else if (isDateTimeInPast(newDate, newTime)) {
                    showToast("You cannot set a task in the past."); // Handles past dates.
                } else {
                    // Updates task in DB.
                    dbHelper.updateTask(taskId, newTitle, newDescription, newDate, newTime, newScheduleType);

                    // Cancel old alarm and schedule new one using same taskId
                    AlarmScheduler.cancelAlarm(this, taskId);
                    AlarmScheduler.scheduleAlarm(this, taskId, newTitle, newDescription, newDate, newTime, isWeekly);

                    showToast("Task updated successfully!");
                    finish(); // Close and return to list
                }
            });

            // Delete button
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this task?")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        // Removes/cancels upcoming notification.
                        // Cancel alarm, remove notification and delete DB record
                        AlarmScheduler.cancelAlarm(this, taskId);
                        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        if (manager != null) {
                            manager.cancel(taskId);
                        }
                        dbHelper.deleteTask(taskId);
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                    })
                    .setNegativeButton("No", null)
                    .show();
            });
        }
        // EDIT MODE; OVERRIDES ADD TASK'S LAYOUT.
        // -------------------------------------------------------------------------------------------------------------------------

        // -------------------------------------------------------------------------------------------------------------------------
        // DATE PICKER & TIME PICKER LOGIC.
        // Handle date picker.
        // Credits: KDTechs (How to Create a Date And Time Picker From EditText in Android Studio + Source Code).
        datePicker.setOnClickListener(v -> {
            new DatePickerDialog(AddTaskActivity.this, (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                datePicker.setText(updateDate());
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Handle time picker.
        // Credits: Coding Demos (Android Timepicker – Use EditText to Show TimePickerDialog (Explained)).
        timePicker.setOnClickListener(view -> {
            currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            currentMinute = calendar.get(Calendar.MINUTE);

            timePickerDialog = new TimePickerDialog(AddTaskActivity.this, (timePicker, hourOfDay, minutes) -> {
                int hour = hourOfDay % 12;
                if (hour == 0) hour = 12;
                amPm = (hourOfDay >= 12) ? "PM" : "AM";

                AddTaskActivity.this.timePicker.setText(
                        String.format(Locale.US, "%02d:%02d %s", hour, minutes, amPm)
                );
            }, currentHour, currentMinute, false);
            timePickerDialog.show();
        });
        // DATE PICKER & TIME PICKER LOGIC.
        // -------------------------------------------------------------------------------------------------------------------------

        // -------------------------------------------------------------------------------------------------------------------------
        // DEFAULT: ADD TASK LAYOUT.
        // When 'Save' button is pressed, do this action. (Only applies if not in edit mode).
        if (!isEdit) {
            btnSave.setOnClickListener(v -> {
                // Check notification permission before allowing task creation (One-Time).
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                NOTIFICATION_PERMISSION_CODE); // Notification permission request.

                        showToast("Please allow notifications to enable reminders.");
                        return; // Block task creation
                    }
                }

                // Get all inputs.
                String title = txteTitle.getText().toString().trim();
                String description = txteDescription.getText().toString().trim();
                String date = datePicker.getText().toString().trim();
                String time = timePicker.getText().toString().trim();

                // Validates selected radiobutton; default: one-time.
                int selectedId = rgSchedType.getCheckedRadioButtonId();
                String scheduleType = (selectedId == rbOneTime.getId()) ? "One-time" : "Weekly";
                boolean isWeekly = scheduleType.equalsIgnoreCase("Weekly");

                if (title.isEmpty() || description.isEmpty() || date.isEmpty() || time.isEmpty()) {
                    showToast("Please fill in all fields"); // Handles empty inputs.
                } else if (isDateTimeInPast(date, time)) {
                    showToast("You cannot set a task in the past."); // Handles past dates.
                } else {
                    // Insert and get the generated id; use it to schedule alarm.
                    long newIdLong = dbHelper.insertTask(title, description, date, time, scheduleType);
                    final int newTaskId = (int) newIdLong;
                    AlarmScheduler.scheduleAlarm(this, newTaskId, title, description, date, time, isWeekly);

                    showToast("Task saved successfully!");

                    // Sets the edittext's fields back to default.
                    txteTitle.setText("");
                    txteDescription.setText("");
                    datePicker.setText("");
                    timePicker.setText("");
                    rbOneTime.setChecked(true);
                }
            });
        }
        // DEFAULT: ADD TASK LAYOUT.
        // -------------------------------------------------------------------------------------------------------------------------
    }

    // Converts the current Calendar date into a formatted string like "2025-10-05" (Year, Month, Day_of_Month).
    private String updateDate() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
        return dateFormat.format(calendar.getTime());
    }

    // Validates the date; no previous dates allowed.
    private boolean isDateTimeInPast(String date, String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US);
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTime(sdf.parse(date + " " + time));

            return selectedCal.getTimeInMillis() < System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
            return true; // Block on error.
        }
    }

    // Prevents toast stacking.
    private void showToast(String message) {
        if (activeToast != null) {
            activeToast.cancel(); // Cancel any existing Toast
        }
        activeToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        activeToast.show();
    }

    // -------------------------------------------------------------------------------------------------------------------------
    // DEBUGGING SQLITE: MUST BE HIDDEN BEFORE APP LAUNCH.
    // Exports SQLite DB to local storage of android device for debugging purposes; will be removed or hidden in the future updates???
    private void exportDatabaseToDownload() {
        File dbFile = getDatabasePath("taskmate.db");
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File destFile = new File(downloadDir, "taskmate_exported.db");

        try {
            Files.copy(dbFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            showToast("Exported to: " + destFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            showToast("Export failed: " + e.getMessage());
        }
    }
    // DEBUGGING SQLITE: MUST BE HIDDEN BEFORE APP LAUNCH.
    // -------------------------------------------------------------------------------------------------------------------------
}
