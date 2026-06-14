package com.example.notemaster.ui.category;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notemaster.R;
import com.example.notemaster.model.Category;
import com.example.notemaster.util.ThemeHelper;
import com.example.notemaster.viewmodel.CategoryViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CategoryListFragment extends Fragment {
    private CategoryViewModel categoryViewModel;
    private CategoryAdapter categoryAdapter;
    private RecyclerView recyclerView;
    private View emptyStateView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar(view);
        recyclerView = view.findViewById(R.id.categoryRecyclerView);
        emptyStateView = view.findViewById(R.id.emptyStateView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setupAdapter();
        setupViewModel();

        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddCategory);
        fabAdd.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void setupAdapter() {
        categoryAdapter = new CategoryAdapter();
        recyclerView.setAdapter(categoryAdapter);

        categoryAdapter.setOnCategoryClickListener(new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(Category category) {
                // 点击分类可以编辑
                showEditCategoryDialog(category);
            }

            @Override
            public void onCategoryDelete(Category category) {
                // 删除分类
                showDeleteCategoryDialog(category);
            }
        });
    }

    private void setupToolbar(View view) {
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        ThemeHelper.applyToolbarColor(requireContext(), toolbar);
        toolbar.setTitle(R.string.category_management);
        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());
    }

    private void setupViewModel() {
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        categoryViewModel.loadAllCategories();
        categoryViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryAdapter.setCategories(categories);
            emptyStateView.setVisibility(
                    categories == null || categories.isEmpty() ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(
                    categories == null || categories.isEmpty() ? View.GONE : View.VISIBLE);
        });
    }

    private void showAddCategoryDialog() {
        EditText editText = new EditText(getContext());
        editText.setHint(R.string.category_name_hint);
        editText.setPadding(64, 32, 64, 16);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_category)
                .setView(editText)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    String name = editText.getText().toString().trim();
                    if (!name.isEmpty()) {
                        if (!categoryViewModel.isCategoryNameExists(name)) {
                            Category category = new Category(name);
                            categoryViewModel.insertCategory(category);
                        } else {
                            Toast.makeText(getContext(), "分类已存在", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showEditCategoryDialog(Category category) {
        EditText editText = new EditText(getContext());
        editText.setText(category.getName());
        editText.setPadding(64, 32, 64, 16);

        new AlertDialog.Builder(requireContext())
                .setTitle("编辑分类")
                .setView(editText)
                .setPositiveButton("保存", (dialog, which) -> {
                    String newName = editText.getText().toString().trim();
                    if (!newName.isEmpty() && !newName.equals(category.getName())) {
                        if (!categoryViewModel.isCategoryNameExists(newName)) {
                            category.setName(newName);
                            categoryViewModel.updateCategory(category);
                        } else {
                            Toast.makeText(getContext(), "分类名已存在", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDeleteCategoryDialog(Category category) {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除分类")
                .setMessage("确定要删除\"" + category.getName() + "\"吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    categoryViewModel.deleteCategory(category.getId());
                    Toast.makeText(getContext(), "分类已删除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
