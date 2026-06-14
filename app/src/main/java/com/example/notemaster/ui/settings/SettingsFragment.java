package com.example.notemaster.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.notemaster.R;
import com.example.notemaster.data.CategoryDao;
import com.example.notemaster.data.NoteDao;
import com.example.notemaster.data.TagDao;
import com.example.notemaster.model.Note;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        SharedPreferences prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);

        // 深色模式
        LinearLayout themeSetting = view.findViewById(R.id.themeSetting);
        Switch darkModeSwitch = view.findViewById(R.id.darkModeSwitch);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        darkModeSwitch.setChecked(isDark);

        themeSetting.setOnClickListener(v -> {
            boolean newState = !darkModeSwitch.isChecked();
            darkModeSwitch.setChecked(newState);
            prefs.edit().putBoolean("dark_mode", newState).apply();
            AppCompatDelegate.setDefaultNightMode(
                newState ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        // 回收站
        LinearLayout trashSetting = view.findViewById(R.id.trashSetting);
        trashSetting.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_settings_to_trash));

        // 加载统计信息
        loadStatistics(view);
    }

    private void loadStatistics(View view) {
        try {
            NoteDao noteDao = new NoteDao(requireContext());
            CategoryDao categoryDao = new CategoryDao(requireContext());
            TagDao tagDao = new TagDao(requireContext());

            List<Note> notes = noteDao.getAllNotes();
            int totalNotes = notes.size();
            int totalWords = 0;
            for (Note note : notes) {
                if (note.getContent() != null) {
                    totalWords += note.getContent().length();
                }
            }

            int totalCategories = categoryDao.getCategoryCount();
            int totalTags = tagDao.getAllTags().size();

            TextView statsTotalNotes = view.findViewById(R.id.statsTotalNotes);
            TextView statsTotalWords = view.findViewById(R.id.statsTotalWords);
            TextView statsTotalCategories = view.findViewById(R.id.statsTotalCategories);
            TextView statsTotalTags = view.findViewById(R.id.statsTotalTags);

            if (statsTotalNotes != null) statsTotalNotes.setText("笔记总数: " + totalNotes);
            if (statsTotalWords != null) statsTotalWords.setText("总字数: " + totalWords);
            if (statsTotalCategories != null) statsTotalCategories.setText("分类数量: " + totalCategories);
            if (statsTotalTags != null) statsTotalTags.setText("标签数量: " + totalTags);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
