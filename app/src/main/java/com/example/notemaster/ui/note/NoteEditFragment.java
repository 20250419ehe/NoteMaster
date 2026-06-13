package com.example.notemaster.ui.note;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.notemaster.R;
import com.example.notemaster.model.Category;
import com.example.notemaster.model.Note;
import com.example.notemaster.viewmodel.CategoryViewModel;
import com.example.notemaster.viewmodel.NoteViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class NoteEditFragment extends Fragment {
    private NoteViewModel noteViewModel;
    private CategoryViewModel categoryViewModel;
    private EditText titleEditText;
    private EditText contentEditText;
    private Spinner categorySpinner;
    private long noteId = -1;
    private String selectedCategory = "";
    private List<String> categoryNames = new ArrayList<>();
    private boolean isLocked = false;
    private String notePassword = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            noteId = getArguments().getLong("noteId", -1);
        }

        initViews(view);
        setupToolbar(view);
        setupViewModel();
        setupCategorySpinner();
        setupListeners(view);

        if (noteId != -1) {
            loadNote();
        }
    }

    private void initViews(View view) {
        titleEditText = view.findViewById(R.id.titleEditText);
        contentEditText = view.findViewById(R.id.contentEditText);
        categorySpinner = view.findViewById(R.id.categorySpinner);
    }

    private void setupToolbar(View view) {
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(noteId == -1 ? "新建笔记" : "编辑笔记");
        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());
    }

    private void setupViewModel() {
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        categoryViewModel.loadAllCategories();
    }

    private void setupCategorySpinner() {
        categoryNames.add("无分类");
        categoryViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryNames.clear();
            categoryNames.add("无分类");
            if (categories != null) {
                for (Category category : categories) {
                    categoryNames.add(category.getName());
                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    categoryNames
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(adapter);

            // 如果有选中的分类，设置Spinner位置
            if (!selectedCategory.isEmpty()) {
                int position = categoryNames.indexOf(selectedCategory);
                if (position >= 0) {
                    categorySpinner.setSelection(position);
                }
            }
        });

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = position == 0 ? "" : categoryNames.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory = "";
            }
        });
    }

    private void setupListeners(View view) {
        FloatingActionButton fabPin = view.findViewById(R.id.fabPin);
        fabPin.setOnClickListener(v -> {
            if (noteId != -1) {
                Note currentNote = noteViewModel.getCurrentNote().getValue();
                if (currentNote != null) {
                    currentNote.setPinned(!currentNote.isPinned());
                    noteViewModel.updateNote(currentNote);
                    Toast.makeText(getContext(),
                            currentNote.isPinned() ? "已置顶" : "已取消置顶",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "请先保存笔记", Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton fabSave = view.findViewById(R.id.fabSave);
        fabSave.setOnClickListener(v -> saveNote());

        // Lock functionality
        LinearLayout lockSetting = view.findViewById(R.id.lockSetting);
        ImageView lockIcon = view.findViewById(R.id.lockIcon);
        TextView lockTextView = view.findViewById(R.id.lockTextView);
        Switch lockSwitch = view.findViewById(R.id.lockSwitch);

        lockSetting.setOnClickListener(v -> lockSwitch.toggle());

        lockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showPasswordDialog(password -> {
                    notePassword = password;
                    isLocked = true;
                    lockIcon.setImageResource(R.drawable.ic_lock);
                    lockTextView.setText("已锁定");
                }, () -> lockSwitch.setChecked(false));
            } else {
                isLocked = false;
                notePassword = "";
                lockIcon.setImageResource(R.drawable.ic_lock_open);
                lockTextView.setText("锁定笔记");
            }
        });
    }

    private void loadNote() {
        noteViewModel.loadNoteById(noteId);
        noteViewModel.getCurrentNote().observe(getViewLifecycleOwner(), note -> {
            if (note != null) {
                titleEditText.setText(note.getTitle());
                contentEditText.setText(note.getContent());
                selectedCategory = note.getCategory() != null ? note.getCategory() : "";
                
                // 设置Spinner位置
                if (!selectedCategory.isEmpty()) {
                    int position = categoryNames.indexOf(selectedCategory);
                    if (position >= 0) {
                        categorySpinner.setSelection(position);
                    }
                }

                isLocked = note.isLocked();
                notePassword = note.getPassword() != null ? note.getPassword() : "";

                Switch lockSwitch = getView().findViewById(R.id.lockSwitch);
                ImageView lockIcon = getView().findViewById(R.id.lockIcon);
                TextView lockTextView = getView().findViewById(R.id.lockTextView);
                if (isLocked) {
                    lockSwitch.setChecked(true);
                    lockIcon.setImageResource(R.drawable.ic_lock);
                    lockTextView.setText("已锁定");
                }
            }
        });
    }

    private void saveNote() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (title.isEmpty()) {
            titleEditText.setError(getString(R.string.note_edit_title_hint));
            return;
        }

        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setCategory(selectedCategory);
        note.setUpdatedAt(System.currentTimeMillis());
        note.setLocked(isLocked);
        note.setPassword(notePassword);

        if (noteId != -1) {
            note.setId(noteId);
            noteViewModel.updateNote(note);
            Toast.makeText(getContext(), "笔记已更新", Toast.LENGTH_SHORT).show();
        } else {
            note.setCreatedAt(System.currentTimeMillis());
            noteViewModel.insertNote(note);
            Toast.makeText(getContext(), "笔记已保存", Toast.LENGTH_SHORT).show();
        }

        Navigation.findNavController(requireView()).navigateUp();
    }

    private interface OnPasswordSetListener {
        void onPasswordSet(String password);
    }

    private void showPasswordDialog(OnPasswordSetListener onSet, Runnable onCancel) {
        EditText passwordInput = new EditText(getContext());
        passwordInput.setHint("输入密码");
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setPadding(64, 32, 64, 16);

        new AlertDialog.Builder(requireContext())
                .setTitle("设置密码")
                .setView(passwordInput)
                .setPositiveButton("确定", (dialog, which) -> {
                    String password = passwordInput.getText().toString().trim();
                    if (!password.isEmpty()) {
                        onSet.onPasswordSet(password);
                    } else {
                        onCancel.run();
                    }
                })
                .setNegativeButton("取消", (dialog, which) -> onCancel.run())
                .setOnCancelListener(dialog -> onCancel.run())
                .show();
    }
}
