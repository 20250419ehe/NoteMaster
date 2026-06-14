package com.example.notemaster.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notemaster.R;
import com.example.notemaster.ui.note.NoteAdapter;
import com.example.notemaster.util.ThemeHelper;
import com.example.notemaster.viewmodel.NoteViewModel;
import com.google.android.material.appbar.MaterialToolbar;

public class SearchFragment extends Fragment {
    private NoteViewModel noteViewModel;
    private NoteAdapter noteAdapter;
    private EditText searchEditText;
    private RecyclerView resultsRecyclerView;
    private TextView emptyStateTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar(view);
        initViews(view);
        setupRecyclerView();
        setupViewModel();
        setupSearch();
    }

    private void setupToolbar(View view) {
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        ThemeHelper.applyToolbarColor(requireContext(), toolbar);
        toolbar.setTitle(R.string.search_results);
        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());
    }

    private void initViews(View view) {
        searchEditText = view.findViewById(R.id.searchEditText);
        resultsRecyclerView = view.findViewById(R.id.resultsRecyclerView);
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView);
    }

    private void setupRecyclerView() {
        noteAdapter = new NoteAdapter();
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        resultsRecyclerView.setAdapter(noteAdapter);

        noteAdapter.setOnNoteClickListener(new NoteAdapter.OnNoteClickListener() {
            @Override
            public void onNoteClick(com.example.notemaster.model.Note note) {
                Bundle bundle = new Bundle();
                bundle.putLong("noteId", note.getId());
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_search_to_noteEdit, bundle);
            }

            @Override
            public void onNoteLongClick(com.example.notemaster.model.Note note) {
                // No-op in search
            }
        });
    }

    private void setupViewModel() {
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(getViewLifecycleOwner(), notes -> {
            noteAdapter.setNotes(notes);
            emptyStateTextView.setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
            resultsRecyclerView.setVisibility(notes.isEmpty() ? View.GONE : View.VISIBLE);
        });
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    noteViewModel.loadAllNotes();
                } else {
                    noteViewModel.searchNotes(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Focus search on open
        searchEditText.requestFocus();
    }
}
