package com.example.notemaster.ui.note;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notemaster.R;
import com.example.notemaster.model.Category;
import com.example.notemaster.model.Note;
import com.example.notemaster.viewmodel.CategoryViewModel;
import com.example.notemaster.viewmodel.NoteViewModel;
import com.example.notemaster.viewmodel.TagViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class NoteListFragment extends Fragment {
    private NoteViewModel noteViewModel;
    private CategoryViewModel categoryViewModel;
    private TagViewModel tagViewModel;
    private ArrayAdapter<String> categoryAdapter;
    private NoteAdapter noteAdapter;
    private EditText searchEditText;
    private Spinner categorySpinner;
    private RecyclerView notesRecyclerView;
    private TextView emptyStateTextView;
    private FloatingActionButton fabAddNote;
    private LinearLayout batchActionBar;
    private TextView selectedCountTextView;
    private CheckBox selectAllCheckBox;
    private String currentCategory = "全部";
    private String currentSearchQuery = "";
    private boolean isGridView = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        SharedPreferences prefs = requireContext().getSharedPreferences("settings", 0);
        isGridView = prefs.getBoolean("grid_view", false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerView();
        setupViewModel();
        setupListeners();
        setupBatchActionBar(view);
        applyViewMode();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_view_toggle) {
            toggleViewMode();
            return true;
        } else if (item.getItemId() == R.id.action_search) {
            searchEditText.requestFocus();
            return true;
        } else if (item.getItemId() == R.id.action_category) {
            Navigation.findNavController(requireView()).navigate(R.id.action_noteList_to_categoryList);
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            Navigation.findNavController(requireView()).navigate(R.id.action_noteList_to_settings);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleViewMode() {
        isGridView = !isGridView;
        SharedPreferences prefs = requireContext().getSharedPreferences("settings", 0);
        prefs.edit().putBoolean("grid_view", isGridView).apply();
        applyViewMode();
    }

    private void applyViewMode() {
        if (isGridView) {
            notesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        } else {
            notesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        noteAdapter.setGridView(isGridView);
    }

    private void initViews(View view) {
        searchEditText = view.findViewById(R.id.searchEditText);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        notesRecyclerView = view.findViewById(R.id.notesRecyclerView);
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView);
        fabAddNote = view.findViewById(R.id.fabAddNote);
        batchActionBar = view.findViewById(R.id.batchActionBar);
        selectedCountTextView = view.findViewById(R.id.selectedCountTextView);
        selectAllCheckBox = view.findViewById(R.id.selectAllCheckBox);
    }

    private void setupRecyclerView() {
        noteAdapter = new NoteAdapter();
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notesRecyclerView.setAdapter(noteAdapter);
        
        noteAdapter.setOnNoteClickListener(new NoteAdapter.OnNoteClickListener() {
            @Override
            public void onNoteClick(Note note) {
                if (noteAdapter.isMultiSelectMode()) {
                    noteAdapter.toggleSelection(note.getId());
                } else if (note.isLocked()) {
                    showUnlockDialog(note);
                } else {
                    navigateToEdit(note.getId());
                }
            }

            @Override
            public void onNoteLongClick(Note note) {
                if (!noteAdapter.isMultiSelectMode()) {
                    enterMultiSelectMode();
                    noteAdapter.toggleSelection(note.getId());
                }
            }
        });

        noteAdapter.setOnSelectionChangedListener(count -> {
            if (noteAdapter.isMultiSelectMode()) {
                updateBatchActionBar(count);
            }
        });

        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                if (noteAdapter.isMultiSelectMode()) return false;
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                noteAdapter.onItemMove(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                List<Note> currentNotes = noteAdapter.getNotes();
                for (int i = 0; i < currentNotes.size(); i++) {
                    Note note = currentNotes.get(i);
                    if (note.getSortOrder() != i) {
                        noteViewModel.updateNoteOrder(note.getId(), i);
                    }
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(notesRecyclerView);
    }

    private void setupViewModel() {
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        tagViewModel = new ViewModelProvider(this).get(TagViewModel.class);
        tagViewModel.loadAllTags();
        noteAdapter.setTagViewModel(tagViewModel);
        
        noteViewModel.getAllNotes().observe(getViewLifecycleOwner(), notes -> {
            noteAdapter.setNotes(notes);
            emptyStateTextView.setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
            notesRecyclerView.setVisibility(notes.isEmpty() ? View.GONE : View.VISIBLE);
        });
        
        categoryViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            List<String> categoryNames = new ArrayList<>();
            categoryNames.add("全部");
            if (categories != null) {
                for (Category cat : categories) {
                    categoryNames.add(cat.getName());
                }
            }
            categoryAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, categoryNames);
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(categoryAdapter);
        });
        
        categoryViewModel.loadAllCategories();
    }

    private void setupListeners() {
        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        fabAddNote.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R.id.action_noteList_to_noteEdit);
        });

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCategory = parent.getItemAtPosition(position).toString();
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupBatchActionBar(View view) {
        view.findViewById(R.id.btnCloseSelection).setOnClickListener(v -> exitMultiSelectMode());

        selectAllCheckBox.setOnClickListener(v -> {
            if (selectAllCheckBox.isChecked()) {
                noteAdapter.selectAll();
            } else {
                noteAdapter.deselectAll();
            }
        });

        view.findViewById(R.id.btnBatchDelete).setOnClickListener(v -> batchDelete());

        view.findViewById(R.id.btnBatchMove).setOnClickListener(v -> batchMoveToCategory());
    }

    private void enterMultiSelectMode() {
        noteAdapter.enterMultiSelectMode();
        batchActionBar.setVisibility(View.VISIBLE);
        fabAddNote.setVisibility(View.GONE);
        searchEditText.setEnabled(false);
        categorySpinner.setEnabled(false);
        updateBatchActionBar(0);
    }

    private void exitMultiSelectMode() {
        noteAdapter.exitMultiSelectMode();
        batchActionBar.setVisibility(View.GONE);
        fabAddNote.setVisibility(View.VISIBLE);
        searchEditText.setEnabled(true);
        categorySpinner.setEnabled(true);
        selectAllCheckBox.setChecked(false);
    }

    private void updateBatchActionBar(int count) {
        selectedCountTextView.setText("已选择 " + count + " 项");
        selectAllCheckBox.setChecked(noteAdapter.isAllSelected());
    }

    private void batchDelete() {
        List<Long> selectedIds = noteAdapter.getSelectedNoteIds();
        if (selectedIds.isEmpty()) {
            Toast.makeText(getContext(), "请先选择笔记", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("批量删除")
                .setMessage("确定要删除选中的 " + selectedIds.size() + " 条笔记吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    for (Long noteId : selectedIds) {
                        noteViewModel.deleteNote(noteId);
                    }
                    exitMultiSelectMode();
                    Toast.makeText(getContext(), "已删除 " + selectedIds.size() + " 条笔记", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void batchMoveToCategory() {
        List<Long> selectedIds = noteAdapter.getSelectedNoteIds();
        if (selectedIds.isEmpty()) {
            Toast.makeText(getContext(), "请先选择笔记", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Category> categories = categoryViewModel.getAllCategories().getValue();
        if (categories == null || categories.isEmpty()) {
            Toast.makeText(getContext(), "没有可选分类", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] categoryNames = new String[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            categoryNames[i] = categories.get(i).getName();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("移动到分类")
                .setItems(categoryNames, (dialog, which) -> {
                    String targetCategory = categoryNames[which];
                    for (Long noteId : selectedIds) {
                        Note note = noteViewModel.getNoteById(noteId);
                        if (note != null) {
                            note.setCategory(targetCategory);
                            noteViewModel.updateNote(note);
                        }
                    }
                    exitMultiSelectMode();
                    Toast.makeText(getContext(), "已移动 " + selectedIds.size() + " 条笔记到 " + targetCategory, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void applyFilters() {
        boolean categoryAll = currentCategory.equals("全部") || currentCategory.isEmpty();
        boolean searchEmpty = currentSearchQuery.isEmpty();

        if (searchEmpty && categoryAll) {
            noteViewModel.loadAllNotes();
        } else if (searchEmpty) {
            noteViewModel.loadNotesByCategory(currentCategory);
        } else if (categoryAll) {
            noteViewModel.searchNotes(currentSearchQuery);
        } else {
            noteViewModel.searchNotesByCategory(currentSearchQuery, currentCategory);
        }
    }

    private void showNoteOptionsDialog(Note note) {
        String[] options;
        if (note.isPinned()) {
            options = new String[]{"取消置顶", "删除"};
        } else {
            options = new String[]{"置顶", "删除"};
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(note.getTitle())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        noteViewModel.togglePinNote(note);
                        Toast.makeText(getContext(),
                                note.isPinned() ? "已取消置顶" : "已置顶",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        showDeleteDialog(note);
                    }
                })
                .show();
    }

    private void showUnlockDialog(Note note) {
        EditText passwordInput = new EditText(getContext());
        passwordInput.setHint("输入密码");
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setPadding(64, 32, 64, 16);

        new AlertDialog.Builder(requireContext())
                .setTitle("笔记已锁定")
                .setView(passwordInput)
                .setPositiveButton("解锁", (dialog, which) -> {
                    String password = passwordInput.getText().toString().trim();
                    if (password.equals(note.getPassword())) {
                        navigateToEdit(note.getId());
                    } else {
                        Toast.makeText(getContext(), "密码错误", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void navigateToEdit(long noteId) {
        Bundle bundle = new Bundle();
        bundle.putLong("noteId", noteId);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_noteList_to_noteEdit, bundle);
    }

    private void showDeleteDialog(Note note) {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除笔记")
                .setMessage("确定要删除\"" + note.getTitle() + "\"吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    noteViewModel.deleteNote(note.getId());
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
