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

    // -------------------------------------------------------------------------------------------------------------------------
    // Fetch all tasks from the database
    // -------------------------------------------------------------------------------------------------------------------------
        public List<Task> getAllTasks() {
            List<Task> taskList = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

            if (cursor.moveToFirst()) {
                do {
                    // Fetch each column value
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DUE_DATE));
                    String time = cursor.getString(cursor.getColumnIndexOrThrow(COL_DUE_TIME));
                    String scheduleType = cursor.getString(cursor.getColumnIndexOrThrow(COL_SCHEDULE_TYPE));

                    // Create Task object and add to list
                    taskList.add(new Task(title, description, date, time, scheduleType));
                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();

            return taskList;
        }
    // -------------------------------------------------------------------------------------------------------------------------

    // -------------------------------------------------------------------------------------------------------------------------
    // Delete method
    // UNDER PROGRESS.

    // -------------------------------------------------------------------------------------------------------------------------
}
