package com.example.notemaster.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.material.appbar.MaterialToolbar;

public class ThemeHelper {
    private static final String PREFS_NAME = "settings";
    private static final String KEY_THEME_COLOR = "theme_color";
    private static final int DEFAULT_COLOR = 0xFF6200EE;

    public static int getThemeColor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_THEME_COLOR, DEFAULT_COLOR);
    }

    public static void applyToolbarColor(Context context, MaterialToolbar toolbar) {
        if (toolbar != null) {
            int color = getThemeColor(context);
            toolbar.setBackgroundColor(color);
        }
    }
}
