package com.example.notemaster.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.notemaster.R;
import com.example.notemaster.data.CategoryDao;
import com.example.notemaster.data.NoteDao;
import com.example.notemaster.data.TagDao;
import com.example.notemaster.model.Note;
import com.example.notemaster.util.BackupHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.util.List;

public class SettingsFragment extends Fragment {

    private final ActivityResultLauncher<Intent> backupLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        exportBackup(uri);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> restoreLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        importBackup(uri);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        SharedPreferences prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);

        // 深色模式
        LinearLayout themeSetting = view.findViewById(R.id.themeSetting);
        Switch darkModeSwitch = view.findViewById(R.id.darkModeSwitch);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        darkModeSwitch.setChecked(isDark);

        if (themeSetting != null) {
            themeSetting.setOnClickListener(v -> {
                boolean newState = !darkModeSwitch.isChecked();
                darkModeSwitch.setChecked(newState);
                prefs.edit().putBoolean("dark_mode", newState).apply();
                AppCompatDelegate.setDefaultNightMode(
                    newState ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );
            });
        }

        // 回收站入口
        LinearLayout trashSetting = view.findViewById(R.id.trashSetting);
        if (trashSetting != null) {
            trashSetting.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_settings_to_trash));
        }

        // 备份数据
        LinearLayout backupSetting = view.findViewById(R.id.backupSetting);
        if (backupSetting != null) {
            backupSetting.setOnClickListener(v -> showBackupDialog());
        }

        // 恢复数据
        LinearLayout restoreSetting = view.findViewById(R.id.restoreSetting);
        if (restoreSetting != null) {
            restoreSetting.setOnClickListener(v -> showRestoreDialog());
        }

        // 加载统计信息
        loadStatistics(view);

        // 主题颜色
        LinearLayout themeColorSetting = view.findViewById(R.id.themeColorSetting);
        View themeColorPreview = view.findViewById(R.id.themeColorPreview);
        if (themeColorSetting != null) {
            int savedColor = prefs.getInt("theme_color", 0xFF6200EE);
            themeColorPreview.getBackground().setTint(savedColor);

            themeColorSetting.setOnClickListener(v -> showColorPickerDialog(savedColor, color -> {
                prefs.edit().putInt("theme_color", color).apply();
                themeColorPreview.getBackground().setTint(color);
                Toast.makeText(getContext(), "主题颜色已更新，重启应用生效", Toast.LENGTH_SHORT).show();
            }));
        }
    }

    private void showColorPickerDialog(int currentColor, OnColorSelectedListener listener) {
        String[] colorNames = {"紫色", "蓝色", "绿色", "红色", "橙色", "粉色"};
        int[] colorValues = {0xFF6200EE, 0xFF2196F3, 0xFF4CAF50, 0xFFF44336, 0xFFFF9800, 0xFFE91E63};

        new AlertDialog.Builder(requireContext())
                .setTitle("选择主题颜色")
                .setItems(colorNames, (dialog, which) -> {
                    listener.onColorSelected(colorValues[which]);
                })
                .show();
    }

    private interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    private void loadStatistics(View view) {
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
    }

    private void showBackupDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("备份数据")
                .setMessage("将所有笔记导出为 JSON 文件，可用于恢复或迁移数据。")
                .setPositiveButton("备份", (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/json");
                    intent.putExtra(Intent.EXTRA_TITLE, "NoteMaster_backup_" +
                            new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                                    .format(new java.util.Date()) + ".json");
                    backupLauncher.launch(intent);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showRestoreDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("恢复数据")
                .setMessage("从 JSON 备份文件恢复笔记数据。注意：重复的笔记不会被再次导入。")
                .setPositiveButton("恢复", (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/json");
                    restoreLauncher.launch(intent);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void exportBackup(Uri uri) {
        try {
            String json = BackupHelper.exportToJson(requireContext());
            requireContext().getContentResolver().openOutputStream(uri).write(json.getBytes());
            Toast.makeText(getContext(), "备份成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "备份失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void importBackup(Uri uri) {
        try {
            String json = BackupHelper.readFromUri(requireContext(), uri);
            int count = BackupHelper.importFromJson(requireContext(), json);
            Toast.makeText(getContext(), "恢复成功，导入 " + count + " 条笔记", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "恢复失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
