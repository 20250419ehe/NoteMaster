package com.example.notemaster.model;

public class Category {
    private long id;
    private String name;
    private long createdAt;

    // 构造函数
    public Category() {}

    public Category(String name) {
        this.name = name;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}