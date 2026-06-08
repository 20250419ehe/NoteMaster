package com.example.notemaster.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notemaster.db";
    private static final int DATABASE_VERSION = 1;

    // 表名
    public static final String TABLE_NOTES = "notes";
    public static final String TABLE_CATEGORIES = "categories";

    // 笔记表列名
    public static final String COLUMN_NOTE_ID = "id";
    public static final String COLUMN_NOTE_TITLE = "title";
    public static final String COLUMN_NOTE_CONTENT = "content";
    public static final String COLUMN_NOTE_CATEGORY = "category";
    public static final String COLUMN_NOTE_IS_PINNED = "is_pinned";
    public static final String COLUMN_NOTE_IS_DELETED = "is_deleted";
    public static final String COLUMN_NOTE_CREATED_AT = "created_at";
    public static final String COLUMN_NOTE_UPDATED_AT = "updated_at";
    public static final String COLUMN_NOTE_SORT_ORDER = "sort_order";

    // 分类表列名
    public static final String COLUMN_CATEGORY_ID = "id";
    public static final String COLUMN_CATEGORY_NAME = "name";
    public static final String COLUMN_CATEGORY_CREATED_AT = "created_at";

    // 创建笔记表SQL语句
    private static final String CREATE_TABLE_NOTES = "CREATE TABLE " + TABLE_NOTES + "("
            + COLUMN_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NOTE_TITLE + " TEXT NOT NULL,"
            + COLUMN_NOTE_CONTENT + " TEXT,"
            + COLUMN_NOTE_CATEGORY + " TEXT,"
            + COLUMN_NOTE_IS_PINNED + " INTEGER DEFAULT 0,"
            + COLUMN_NOTE_IS_DELETED + " INTEGER DEFAULT 0,"
            + COLUMN_NOTE_CREATED_AT + " INTEGER,"
            + COLUMN_NOTE_UPDATED_AT + " INTEGER,"
            + COLUMN_NOTE_SORT_ORDER + " INTEGER"
            + ")";

    // 创建分类表SQL语句
    private static final String CREATE_TABLE_CATEGORIES = "CREATE TABLE " + TABLE_CATEGORIES + "("
            + COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_CATEGORY_NAME + " TEXT NOT NULL UNIQUE,"
            + COLUMN_CATEGORY_CREATED_AT + " INTEGER"
            + ")";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_NOTES);
        db.execSQL(CREATE_TABLE_CATEGORIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        onCreate(db);
    }
}