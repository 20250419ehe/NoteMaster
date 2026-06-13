package com.example.notemaster.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class UndoRedoManager {
    private final EditText editText;
    private final List<String> history = new ArrayList<>();
    private int currentIndex = -1;
    private boolean isUndoing = false;
    private static final int MAX_HISTORY = 50;

    public UndoRedoManager(EditText editText) {
        this.editText = editText;
        saveState();

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!isUndoing) {
                    saveState();
                }
            }
        });
    }

    private void saveState() {
        String text = editText.getText().toString();
        int cursorPos = editText.getSelectionStart();

        if (currentIndex < history.size() - 1) {
            history.subList(currentIndex + 1, history.size()).clear();
        }

        history.add(text + "|||" + cursorPos);

        if (history.size() > MAX_HISTORY) {
            history.remove(0);
        } else {
            currentIndex++;
        }
    }

    public void undo() {
        if (currentIndex > 0) {
            isUndoing = true;
            currentIndex--;
            restoreState();
            isUndoing = false;
        }
    }

    public void redo() {
        if (currentIndex < history.size() - 1) {
            isUndoing = true;
            currentIndex++;
            restoreState();
            isUndoing = false;
        }
    }

    private void restoreState() {
        String state = history.get(currentIndex);
        String[] parts = state.split("\\|\\|\\|");
        String text = parts[0];
        int cursorPos = Integer.parseInt(parts[1]);

        editText.setText(text);
        editText.setSelection(Math.min(cursorPos, text.length()));
    }

    public boolean canUndo() {
        return currentIndex > 0;
    }

    public boolean canRedo() {
        return currentIndex < history.size() - 1;
    }

    public void clear() {
        history.clear();
        currentIndex = -1;
        saveState();
    }
}
