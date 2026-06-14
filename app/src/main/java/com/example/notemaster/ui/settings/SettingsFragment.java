package com.example.notemaster.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.notemaster.R;
import com.google.android.material.appbar.MaterialToolbar;

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
    }
}
