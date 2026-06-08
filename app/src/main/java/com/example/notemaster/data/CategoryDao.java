package com.example.notemaster.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.notemaster.model.Category;
import com.example.notemaster.util.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    private DatabaseHelper dbHelper;
    private Context context;

    public CategoryDao(Context context) {
        this.context = context;
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    // 插入分类
    public long insertCategory(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CATEGORY_NAME, category.getName());
        values.put(DatabaseHelper.COLUMN_CATEGORY_CREATED_AT, category.getCreatedAt());
        
        long id = db.insert(DatabaseHelper.TABLE_CATEGORIES, null, values);
        db.close();
        return id;
    }

    // 更新分类
    public int updateCategory(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CATEGORY_NAME, category.getName());
        
        int rowsAffected = db.update(DatabaseHelper.TABLE_CATEGORIES, values,
                DatabaseHelper.COLUMN_CATEGORY_ID + " = ?",
                new String[]{String.valueOf(category.getId())});
        db.close();
        return rowsAffected;
    }

    // 删除分类
    public int deleteCategory(long categoryId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = db.delete(DatabaseHelper.TABLE_CATEGORIES,
                DatabaseHelper.COLUMN_CATEGORY_ID + " = ?",
                new String[]{String.valueOf(categoryId)});
        db.close();
        return rowsAffected;
    }

    // 获取所有分类
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_CATEGORIES +
                " ORDER BY " + DatabaseHelper.COLUMN_CATEGORY_NAME + " ASC";
        
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID)));
                category.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME)));
                category.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_CREATED_AT)));
                
                categories.add(category);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return categories;
    }

    // 根据ID获取分类
    public Category getCategoryById(long categoryId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES, null,
                DatabaseHelper.COLUMN_CATEGORY_ID + " = ?",
                new String[]{String.valueOf(categoryId)},
                null, null, null);
        
        Category category = null;
        if (cursor.moveToFirst()) {
            category = new Category();
            category.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID)));
            category.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME)));
            category.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_CREATED_AT)));
        }
        
        cursor.close();
        db.close();
        return category;
    }

    // 检查分类名称是否存在
    public boolean isCategoryNameExists(String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES, null,
                DatabaseHelper.COLUMN_CATEGORY_NAME + " = ?",
                new String[]{name},
                null, null, null);
        
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // 获取分类数量
    public int getCategoryCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CATEGORIES, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        
        cursor.close();
        db.close();
        return count;
    }
}