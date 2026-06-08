package com.example.notemaster.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.notemaster.data.CategoryRepository;
import com.example.notemaster.model.Category;

import java.util.List;

public class CategoryViewModel extends AndroidViewModel {
    private CategoryRepository categoryRepository;
    private MutableLiveData<List<Category>> allCategories;
    private MutableLiveData<Category> currentCategory;

    public CategoryViewModel(@NonNull Application application) {
        super(application);
        categoryRepository = new CategoryRepository(application);
        allCategories = new MutableLiveData<>();
        currentCategory = new MutableLiveData<>();
    }

    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    public LiveData<Category> getCurrentCategory() {
        return currentCategory;
    }

    public void loadAllCategories() {
        List<Category> categories = categoryRepository.getAllCategories();
        allCategories.setValue(categories);
    }

    public void loadCategoryById(long categoryId) {
        Category category = categoryRepository.getCategoryById(categoryId);
        currentCategory.setValue(category);
    }

    public void insertCategory(Category category) {
        categoryRepository.insertCategory(category);
        loadAllCategories();
    }

    public void updateCategory(Category category) {
        categoryRepository.updateCategory(category);
        loadAllCategories();
    }

    public void deleteCategory(long categoryId) {
        categoryRepository.deleteCategory(categoryId);
        loadAllCategories();
    }

    public boolean isCategoryNameExists(String name) {
        return categoryRepository.isCategoryNameExists(name);
    }

    public int getCategoryCount() {
        return categoryRepository.getCategoryCount();
    }
}