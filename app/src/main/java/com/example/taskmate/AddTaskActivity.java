package com.example.taskmate;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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

    TimePickerDialog timePickerDialog;
    Calendar calendar = Calendar.getInstance();
    int currentHour;
    int currentMinute;
    String amPm;

    private EditText txteTitle, txteDescription, datePicker, timePicker;
    private RadioGroup rgSchedType;
    private RadioButton rbOneTime, rbDaily;
    private Button btnSave, btnExportDB, btnDelete;

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
        rbDaily = findViewById(R.id.rbDaily);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        rbOneTime.setChecked(true); // Automatically sets one-time as default selected option
        // -------------------------------------------------------------------------------------------------------------------------
        // MUST BE REMOVE IN FUTURE UPDATES.
        btnExportDB = findViewById(R.id.btnExportDB); // Export SQLiteDB button
        // MUST BE REMOVE IN FUTURE UPDATES.
        // -------------------------------------------------------------------------------------------------------------------------

        // -------------------------------------------------------------------------------------------------------------------------
        // MUST BE REMOVE IN FUTURE UPDATES.
        // Export SQLiteDB button logic
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                100);
        btnExportDB.setOnClickListener(v -> exportDatabaseToDownload());
        // MUST BE REMOVE IN FUTURE UPDATES.
        // -------------------------------------------------------------------------------------------------------------------------

        // Initialize an instance of TaskDBHelper class to use its methods of SQLite data manipulation (e.g., insert & delete).
        TaskDBHelper dbHelper = new TaskDBHelper(this);

        // ------------------------------------------------------------
        // Check if this activity was opened for EDIT MODE
        boolean isEdit = getIntent().getBooleanExtra("isEdit", false);

        if (isEdit) {
            // Change button text
            btnSave.setText("Update Task");

            // Get data from Intent and pre-fill UI
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

            // Set RadioButton selection
            if ("Daily".equalsIgnoreCase(scheduleType)) {
                rbDaily.setChecked(true);
            } else {
                rbOneTime.setChecked(true);
            }

            // ------------------------------------------------------------
            // Override save button to update instead of insert
            btnSave.setOnClickListener(v -> {
                String newTitle = txteTitle.getText().toString().trim();
                String newDescription = txteDescription.getText().toString().trim();
                String newDate = datePicker.getText().toString().trim();
                String newTime = timePicker.getText().toString().trim();

                int selectedId = rgSchedType.getCheckedRadioButtonId();
                String newScheduleType = (selectedId == rbOneTime.getId()) ? "One-time" : "Daily";

                if (newTitle.isEmpty() || newDescription.isEmpty() || newDate.isEmpty() || newTime.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.updateTask(taskId, newTitle, newDescription, newDate, newTime, newScheduleType);
                    Toast.makeText(this, "Task updated successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close and return to list
                }
            });

            // Delete button
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(this)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this task?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dbHelper.deleteTask(taskId);
                        Toast.makeText(this, "Task deleted successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
            });
        }
        // -------------------------------------------------------------------------------------------------------------------------

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

        // When 'Save' button is pressed, do this action. (Only applies if not in edit mode)
        if (!isEdit) {
            btnSave.setOnClickListener(v -> {
                String title = txteTitle.getText().toString().trim();
                String description = txteDescription.getText().toString().trim();
                String date = datePicker.getText().toString().trim();
                String time = timePicker.getText().toString().trim();

                int selectedId = rgSchedType.getCheckedRadioButtonId();
                String scheduleType = (selectedId == rbOneTime.getId()) ? "One-time" : "Daily";

                if (title.isEmpty() || description.isEmpty() || date.isEmpty() || time.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.insertTask(title, description, date, time, scheduleType);
                    Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show();

                    txteTitle.setText("");
                    txteDescription.setText("");
                    datePicker.setText("");
                    timePicker.setText("");
                    rbOneTime.setChecked(true);
                }
            });
        }
    }

    // Converts the current Calendar date into a formatted string like "2025-10-05" (Year, Month, Day_of_Month).
    private String updateDate() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
        return dateFormat.format(calendar.getTime());
    }

    // -------------------------------------------------------------------------------------------------------------------------
    // Exports SQLite DB to local storage for viewing purposes; will be removed in the future updates.
    // MUST BE REMOVED IN FUTURE UPDATES.
    private void exportDatabaseToDownload() {
        File dbFile = getDatabasePath("taskmate.db");
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File destFile = new File(downloadDir, "taskmate_exported.db");

        try {
            Files.copy(dbFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Toast.makeText(this, "Exported to: " + destFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    // MUST BE REMOVED IN FUTURE UPDATES.
    // -------------------------------------------------------------------------------------------------------------------------
}
