package com.example.notemaster.data;

import android.content.Context;

import com.example.notemaster.model.Category;

import java.util.List;

public class CategoryRepository {
    private CategoryDao categoryDao;

    public CategoryRepository(Context context) {
        this.categoryDao = new CategoryDao(context);
    }

    public long insertCategory(Category category) {
        return categoryDao.insertCategory(category);
    }

    public int updateCategory(Category category) {
        return categoryDao.updateCategory(category);
    }

    public int deleteCategory(long categoryId) {
        return categoryDao.deleteCategory(categoryId);
    }

    public List<Category> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    public Category getCategoryById(long categoryId) {
        return categoryDao.getCategoryById(categoryId);
    }

    public boolean isCategoryNameExists(String name) {
        return categoryDao.isCategoryNameExists(name);
    }

    public int getCategoryCount() {
        return categoryDao.getCategoryCount();
    }
}