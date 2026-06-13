package com.example.notemaster.ui.note;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notemaster.R;
import com.example.notemaster.model.Category;
import com.example.notemaster.model.Note;
import com.example.notemaster.viewmodel.CategoryViewModel;
import com.example.notemaster.viewmodel.NoteViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class NoteListFragment extends Fragment {
    private NoteViewModel noteViewModel;
    private CategoryViewModel categoryViewModel;
    private ArrayAdapter<String> categoryAdapter;
    private NoteAdapter noteAdapter;
    private EditText searchEditText;
    private Spinner categorySpinner;
    private RecyclerView notesRecyclerView;
    private TextView emptyStateTextView;
    private FloatingActionButton fabAddNote;
    private String currentCategory = "全部";
    private String currentSearchQuery = "";

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
    }

    private void initViews(View view) {
        searchEditText = view.findViewById(R.id.searchEditText);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        notesRecyclerView = view.findViewById(R.id.notesRecyclerView);
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView);
        fabAddNote = view.findViewById(R.id.fabAddNote);
    }

    private void setupRecyclerView() {
        noteAdapter = new NoteAdapter();
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notesRecyclerView.setAdapter(noteAdapter);
        
        noteAdapter.setOnNoteClickListener(new NoteAdapter.OnNoteClickListener() {
            @Override
            public void onNoteClick(Note note) {
                // 导航到编辑界面
                Bundle bundle = new Bundle();
                bundle.putLong("noteId", note.getId());
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_noteList_to_noteEdit, bundle);
            }

            @Override
            public void onNoteLongClick(Note note) {
                showNoteOptionsDialog(note);
            }
        });
    }

    private void setupViewModel() {
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        
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

        new android.app.AlertDialog.Builder(requireContext())
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

    private void showDeleteDialog(Note note) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("删除笔记")
                .setMessage("确定要删除\"" + note.getTitle() + "\"吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    noteViewModel.deleteNote(note.getId());
                })
                .setNegativeButton("取消", null)
                .show();
    }
}