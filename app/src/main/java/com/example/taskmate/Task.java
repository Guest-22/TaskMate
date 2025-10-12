package com.example.taskmate;

public class Task {
    // Unique ID for each task (auto-increment from the database).
    // This lets us identify the specific task when updating or deleting.
    private int id;

    // Task details.
    private String title;
    private String description;
    private String date;
    private String time;
    private String type;

    public Task(String title, String description, String date, String time, String type) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.type = type;
    }

    // Constructor for editing data/values from selected task/events.
    public Task(int id, String title, String description, String date, String time, String type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.type = type;
    }

    // Getter methods.
    public int getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getDate() {
        return date;
    }
    public String getTime() {
        return time;
    }
    public String getType() {
        return type;
    }
}