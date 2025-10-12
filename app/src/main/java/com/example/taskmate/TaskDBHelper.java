package com.example.taskmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class TaskDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "taskmate.db";
    private static final int DB_VERSION = 1;
    public static final String TABLE_NAME = "tasks";
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_DUE_DATE = "due_date";
    public static final String COL_DUE_TIME = "due_time";
    public static final String COL_SCHEDULE_TYPE = "schedule_type";

    public TaskDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Creates the table
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
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

    /*
    // Insert method.
    public void insertTask(String title, String description, String date, String time, String scheduleType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_DUE_DATE, date);
        values.put(COL_DUE_TIME, time);
        values.put(COL_SCHEDULE_TYPE, scheduleType);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }
     */
    // Insert method that returns the inserted task's ID
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
            return taskId;  // <-- return the generated ID
        }
    // -------------------------------------------------------------------------------------------------------------------------
    // Fetch/Retrieve all tasks info from the DB; return type (List).
    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

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
    // -------------------------------------------------------------------------------------------------------------------------

    // -------------------------------------------------------------------------------------------------------------------------
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

    // -------------------------------------------------------------------------------------------------------------------------
    // Deletes task by referencing its ID.
        public void deleteTask(int id) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_NAME, COL_ID + " = ?", new String[]{String.valueOf(id)});
            db.close();
        }
    // -------------------------------------------------------------------------------------------------------------------------
    // Optional helper: get schedule type for a specific task id (used by NotificationReceiver)
    // ()NEWCODE
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
    // ()NEWCODE

    // ()NEWCODE - Get the time string (e.g., "12:00 PM") for a specific task
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
// ()NEWCODE
}
