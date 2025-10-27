package com.example.taskmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import java.util.ArrayList;
import java.util.List;

public class TaskDBHelper extends SQLiteOpenHelper {

    // INITIALIZING FINAL SQLITE STRUCTURE.
    private static final String DB_NAME = "taskmate.db";
    private static final int DB_VERSION = 1;
    public static final String TABLE_NAME = "tasks";
    public static final String COL_ID = "id"; // Primary key.
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_DUE_DATE = "due_date";
    public static final String COL_DUE_TIME = "due_time";
    public static final String COL_SCHEDULE_TYPE = "schedule_type";

    public TaskDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Creates the table.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable =
                "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_DUE_DATE + " TEXT, " +
                COL_DUE_TIME + " TEXT, " +
                COL_SCHEDULE_TYPE + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Insert method that returns the inserted task's ID.
        public long insertTask(String title, String description, String date, String time, String scheduleType) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_TITLE, title);
            values.put(COL_DESCRIPTION, description);
            values.put(COL_DUE_DATE, date);
            values.put(COL_DUE_TIME, time);
            values.put(COL_SCHEDULE_TYPE, scheduleType);

            long taskId = db.insert(TABLE_NAME, null, values);
            db.close();
            return taskId;  // Return the generated ID.
        }

    // Fetch/Retrieve all tasks info from the DB; return type (List).
    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        // Automatic sort by due date & time.
        // Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_DUE_DATE + " ASC, " + COL_DUE_TIME + " ASC", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DUE_DATE));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(COL_DUE_TIME));
                String scheduleType = cursor.getString(cursor.getColumnIndexOrThrow(COL_SCHEDULE_TYPE));

                taskList.add(new Task(id, title, description, date, time, scheduleType));
            } while (cursor.moveToNext()); // While there is rows with data, store all the task object inside our Task model.
        }
        cursor.close();
        db.close();
        return taskList;
    }

    // Sort by creation order (default)
    public List<Task> getTasksSortedByCreation() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_ID + " ASC", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DUE_DATE));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(COL_DUE_TIME));
                String scheduleType = cursor.getString(cursor.getColumnIndexOrThrow(COL_SCHEDULE_TYPE));

                taskList.add(new Task(id, title, description, date, time, scheduleType));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }

    // Sort by due date and time.
    public List<Task> getTasksSortedByDueDate() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Fetch all tasks without sorting by type
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DUE_DATE)); // e.g., "2025-10-27"
                String time = cursor.getString(cursor.getColumnIndexOrThrow(COL_DUE_TIME)); // e.g., "12:57 PM"
                String scheduleType = cursor.getString(cursor.getColumnIndexOrThrow(COL_SCHEDULE_TYPE));

                taskList.add(new Task(id, title, description, date, time, scheduleType));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        // Sort by full datetime (date + time), ignoring type
        Collections.sort(taskList, (t1, t2) -> {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.US);
                Date dt1 = format.parse(t1.getDate().trim() + " " + t1.getTime().trim());
                Date dt2 = format.parse(t2.getDate().trim() + " " + t2.getTime().trim());
                return dt1.compareTo(dt2); // earliest first
            } catch (ParseException e) {
                return 0;
            }
        });
        return taskList;
    }

    // Sort by schedule type, then due date and time.
    public List<Task> getTasksSortedByScheduleType() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_SCHEDULE_TYPE +
                " ASC, " + COL_DUE_DATE + " ASC", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DUE_DATE));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(COL_DUE_TIME));
                String scheduleType = cursor.getString(cursor.getColumnIndexOrThrow(COL_SCHEDULE_TYPE));

                taskList.add(new Task(id, title, description, date, time, scheduleType));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        // Compare due date & time.
        Collections.sort(taskList, (t1, t2) -> {
            int typeCompare = t1.getType().compareTo(t2.getType());
            if (typeCompare != 0) return typeCompare;

            int dateCompare = t1.getDate().compareTo(t2.getDate());
            if (dateCompare != 0) return dateCompare;

            try {
                SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.US);
                Date time1 = format.parse(t1.getTime());
                Date time2 = format.parse(t2.getTime());
                return time1.compareTo(time2);
            } catch (ParseException e) {
                return t1.getTime().compareTo(t2.getTime()); // fallback
            }
        });
        return taskList;
    }

    // Update an existing task using its ID.
        public void updateTask(int id, String title, String description, String date, String time, String scheduleType) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_TITLE, title);
            values.put(COL_DESCRIPTION, description);
            values.put(COL_DUE_DATE, date);
            values.put(COL_DUE_TIME, time);
            values.put(COL_SCHEDULE_TYPE, scheduleType);

            // UPDATE tasks SET title=?, description=?, ... WHERE id = ?
            db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
            db.close();
        }

    // Deletes existing task by referencing its ID.
        public void deleteTask(int id) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_NAME, COL_ID + " = ?", new String[]{String.valueOf(id)});
            db.close();
        }

    // Gets the scheduled type for a specific task id (used by NotificationReceiver).
    public String getScheduleType(int id) {
        String result = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_SCHEDULE_TYPE + " FROM " + TABLE_NAME + " WHERE " + COL_ID + " = ?", new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            result = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return result;
    }

    // Gets the time string (e.g., "12:00 PM") for a specific task.
    public String getTaskTime(int taskId) {
        String time = null;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + COL_DUE_TIME + " FROM " + TABLE_NAME + " WHERE " + COL_ID + " = ?", new String[]{String.valueOf(taskId)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                time = cursor.getString(cursor.getColumnIndexOrThrow(COL_DUE_TIME));
            }
            cursor.close();
        }
        db.close();
        return time;
    }

    // Gets the date string (e.g., 2025-10-27) for a specific task.
    public String getTaskDate(int taskId) {
        String date = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_DUE_DATE +" FROM " + TABLE_NAME +" WHERE " + COL_ID +" = ?", new String[]{String.valueOf(taskId)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                date = cursor.getString(0);
            }
            cursor.close();
        }
        db.close();
        return date;
    }

    // Updates task date for weekly; change existing task date & time to next week (7 days after notif fires).
    public void updateTaskDate(int taskId, String newDate, String newTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DUE_DATE, newDate);
        values.put(COL_DUE_TIME, newTime);
        db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
    }
}