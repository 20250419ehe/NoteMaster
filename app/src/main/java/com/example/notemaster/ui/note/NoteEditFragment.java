package com.example.notemaster.ui.note;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import com.example.notemaster.util.ReminderHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
import com.example.notemaster.model.Note;
import com.example.notemaster.model.Tag;
import com.example.notemaster.model.TodoItem;
import com.example.notemaster.viewmodel.CategoryViewModel;
import com.example.notemaster.viewmodel.NoteViewModel;
import com.example.notemaster.viewmodel.TagViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class NoteEditFragment extends Fragment {
    private NoteViewModel noteViewModel;
    private CategoryViewModel categoryViewModel;
    private TagViewModel tagViewModel;
    private EditText titleEditText;
    private EditText contentEditText;
    private Spinner categorySpinner;
    private long noteId = -1;
    private String selectedCategory = "";
    private List<String> categoryNames = new ArrayList<>();
    private boolean isLocked = false;
    private String notePassword = "";
    private long reminderTime = 0;
    private List<String> selectedTags = new ArrayList<>();
    private TodoAdapter todoAdapter;
    private List<TodoItem> todoItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        ReminderHelper.createNotificationChannel(requireContext());

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
        toolbar.inflateMenu(R.menu.edit_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_share) {
                shareNote();
                return true;
            }
            return false;
        });
    }

    private void shareNote() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(getContext(), "笔记内容为空", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = {"分享文本", "导出为文件"};
        new AlertDialog.Builder(requireContext())
                .setTitle("分享笔记")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        shareAsText(title, content);
                    } else {
                        exportToFile(title, content);
                    }
                })
                .show();
    }

    private void shareAsText(String title, String content) {
        String shareText = title;
        if (!content.isEmpty()) {
            shareText += "\n\n" + content;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "分享笔记"));
    }

    private void exportToFile(String title, String content) {
        try {
            String fileName = title.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5_-]", "_");
            if (fileName.length() > 50) {
                fileName = fileName.substring(0, 50);
            }
            fileName += ".txt";

            java.io.File exportDir = new java.io.File(requireContext().getFilesDir(), "exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            java.io.File file = new java.io.File(exportDir, fileName);
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write("标题: " + title + "\n\n");
            writer.write(content);
            writer.close();

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, title);

            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    file
            );
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "导出笔记"));

            Toast.makeText(getContext(), "已导出: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupViewModel() {
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        tagViewModel = new ViewModelProvider(this).get(TagViewModel.class);
        categoryViewModel.loadAllCategories();
        tagViewModel.loadAllTags();
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

        // Reminder functionality
        LinearLayout reminderSetting = view.findViewById(R.id.reminderSetting);
        TextView reminderTextView = view.findViewById(R.id.reminderTextView);

        reminderSetting.setOnClickListener(v -> {
            if (noteId == -1) {
                Toast.makeText(getContext(), "请先保存笔记", Toast.LENGTH_SHORT).show();
                return;
            }
            showDateTimePicker((year, month, day, hour, minute) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day, hour, minute, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                reminderTime = calendar.getTimeInMillis();

                Note currentNote = noteViewModel.getCurrentNote().getValue();
                if (currentNote != null) {
                    currentNote.setReminderTime(reminderTime);
                    noteViewModel.updateNote(currentNote);
                    ReminderHelper.setReminder(requireContext(), currentNote.getId(),
                            currentNote.getTitle(), reminderTime);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    reminderTextView.setText("提醒: " + sdf.format(new Date(reminderTime)));
                    Toast.makeText(getContext(), "提醒已设置", Toast.LENGTH_SHORT).show();
                }
            });
        });

        if (noteId != -1) {
            noteViewModel.getCurrentNote().observe(getViewLifecycleOwner(), note -> {
                if (note != null && note.getReminderTime() > 0) {
                    reminderTime = note.getReminderTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    reminderTextView.setText("提醒: " + sdf.format(new Date(reminderTime)));
                }
            });
        }

        // Tag functionality
        ChipGroup tagChipGroup = view.findViewById(R.id.tagChipGroup);
        android.widget.ImageButton addTagButton = view.findViewById(R.id.addTagButton);

        addTagButton.setOnClickListener(v -> showAddTagDialog(tagChipGroup));

        if (noteId != -1) {
            noteViewModel.getCurrentNote().observe(getViewLifecycleOwner(), note -> {
                if (note != null) {
                    selectedTags = tagViewModel.getTagsForNote(note.getId());
                    updateTagChips(tagChipGroup);
                }
            });
        }

        // Todo functionality
        RecyclerView todoRecyclerView = view.findViewById(R.id.todoRecyclerView);
        android.widget.ImageButton addTodoButton = view.findViewById(R.id.addTodoButton);

        todoAdapter = new TodoAdapter();
        todoRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        todoRecyclerView.setAdapter(todoAdapter);

        todoAdapter.setOnTodoActionListener(new TodoAdapter.OnTodoActionListener() {
            @Override
            public void onToggle(TodoItem item, int position) {
                // Toggle handled in adapter
            }

            @Override
            public void onDelete(TodoItem item, int position) {
                todoItems.remove(position);
                todoAdapter.removeItem(position);
            }

            @Override
            public void onTextChanged(TodoItem item, String text) {
                item.setText(text);
            }
        });

        addTodoButton.setOnClickListener(v -> {
            TodoItem newItem = new TodoItem("", false);
            todoItems.add(newItem);
            todoAdapter.addItem(newItem);
        });

        if (noteId != -1) {
            noteViewModel.getCurrentNote().observe(getViewLifecycleOwner(), note -> {
                if (note != null) {
                    todoItems = parseTodosFromContent(note.getContent());
                    todoAdapter.setTodoItems(todoItems);
                }
            });
        }
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

        String fullContent = buildContentWithTodos(content);

        Note note = new Note();
        note.setTitle(title);
        note.setContent(fullContent);
        note.setCategory(selectedCategory);
        note.setUpdatedAt(System.currentTimeMillis());
        note.setLocked(isLocked);
        note.setPassword(notePassword);
        note.setReminderTime(reminderTime);

        if (noteId != -1) {
            note.setId(noteId);
            noteViewModel.updateNote(note);
            tagViewModel.setTagsForNote(noteId, selectedTags);
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

    private interface OnDateTimeSelectedListener {
        void onDateTimeSelected(int year, int month, int day, int hour, int minute);
    }

    private void showDateTimePicker(OnDateTimeSelectedListener listener) {
        Calendar now = Calendar.getInstance();
        new android.app.DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            new android.app.TimePickerDialog(requireContext(), (view1, hourOfDay, minute) -> {
                listener.onDateTimeSelected(year, month, dayOfMonth, hourOfDay, minute);
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showAddTagDialog(ChipGroup chipGroup) {
        List<Tag> allTags = tagViewModel.getAllTags().getValue();
        if (allTags == null) allTags = new ArrayList<>();

        String[] tagNames = new String[allTags.size()];
        boolean[] checkedItems = new boolean[allTags.size()];

        for (int i = 0; i < allTags.size(); i++) {
            tagNames[i] = allTags.get(i).getName();
            checkedItems[i] = selectedTags.contains(allTags.get(i).getName());
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("选择标签")
                .setMultiChoiceItems(tagNames, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("确定", (dialog, which) -> {
                    selectedTags.clear();
                    for (int i = 0; i < checkedItems.length; i++) {
                        if (checkedItems[i]) {
                            selectedTags.add(tagNames[i]);
                        }
                    }
                    updateTagChips(chipGroup);
                })
                .setNegativeButton("取消", null)
                .setNeutralButton("新建标签", (dialog, which) -> {
                    showCreateTagDialog(chipGroup);
                })
                .show();
    }

    private void showCreateTagDialog(ChipGroup chipGroup) {
        EditText editText = new EditText(getContext());
        editText.setHint("标签名称");
        editText.setPadding(64, 32, 64, 16);

        new AlertDialog.Builder(requireContext())
                .setTitle("新建标签")
                .setView(editText)
                .setPositiveButton("创建", (dialog, which) -> {
                    String name = editText.getText().toString().trim();
                    if (!name.isEmpty() && !tagViewModel.isTagExists(name)) {
                        Tag tag = new Tag(name, "#6200EE");
                        tagViewModel.insertTag(tag);
                        selectedTags.add(name);
                        updateTagChips(chipGroup);
                    } else if (tagViewModel.isTagExists(name)) {
                        Toast.makeText(getContext(), "标签已存在", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateTagChips(ChipGroup chipGroup) {
        chipGroup.removeAllViews();
        for (String tagName : selectedTags) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(requireContext());
            chip.setText(tagName);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                selectedTags.remove(tagName);
                updateTagChips(chipGroup);
            });
            chipGroup.addView(chip);
        }
    }

    private List<TodoItem> parseTodosFromContent(String content) {
        List<TodoItem> todos = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return todos;
        }

        String todoPrefix = "[TODO]";
        int todoStart = content.indexOf(todoPrefix);
        if (todoStart == -1) {
            return todos;
        }

        String todoSection = content.substring(todoStart + todoPrefix.length());
        String[] lines = todoSection.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("[x]")) {
                todos.add(new TodoItem(line.substring(3).trim(), true));
            } else if (line.startsWith("[ ]")) {
                todos.add(new TodoItem(line.substring(3).trim(), false));
            }
        }
        return todos;
    }

    private String buildContentWithTodos(String content) {
        String todoPrefix = "[TODO]";
        int todoStart = content.indexOf(todoPrefix);
        String baseContent = todoStart >= 0 ? content.substring(0, todoStart).trim() : content.trim();

        if (todoItems.isEmpty()) {
            return baseContent;
        }

        StringBuilder sb = new StringBuilder();
        if (!baseContent.isEmpty()) {
            sb.append(baseContent);
        }
        sb.append("\n\n").append(todoPrefix).append("\n");

        for (TodoItem item : todoItems) {
            String checkbox = item.isCompleted() ? "[x]" : "[ ]";
            sb.append(checkbox).append(" ").append(item.getText()).append("\n");
        }

        return sb.toString().trim();
    }
}
