package com.example.taskmate;

public class Task {
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
