package com.example.notemaster.model;

public class TodoItem {
    private String text;
    private boolean isCompleted;

    public TodoItem() {}

    public TodoItem(String text, boolean isCompleted) {
        this.text = text;
        this.isCompleted = isCompleted;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
