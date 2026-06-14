package com.example.notemaster.ui.note;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notemaster.R;
import com.example.notemaster.util.ThemeHelper;
import com.example.notemaster.viewmodel.NoteViewModel;
import com.google.android.material.appbar.MaterialToolbar;

public class TrashFragment extends Fragment {
    private NoteViewModel noteViewModel;
    private TrashAdapter trashAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        ThemeHelper.applyToolbarColor(requireContext(), toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        RecyclerView recyclerView = view.findViewById(R.id.trashRecyclerView);
        TextView emptyView = view.findViewById(R.id.emptyTrashTextView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        trashAdapter = new TrashAdapter();
        recyclerView.setAdapter(trashAdapter);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.loadDeletedNotes();

        noteViewModel.getDeletedNotes().observe(getViewLifecycleOwner(), notes -> {
            trashAdapter.setNotes(notes);
            emptyView.setVisibility(notes == null || notes.isEmpty() ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(notes == null || notes.isEmpty() ? View.GONE : View.VISIBLE);
        });

        trashAdapter.setOnTrashActionListener(new TrashAdapter.OnTrashActionListener() {
            @Override
            public void onRestore(com.example.notemaster.model.Note note) {
                noteViewModel.restoreNote(note.getId());
                Toast.makeText(getContext(), "笔记已恢复", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermanentDelete(com.example.notemaster.model.Note note) {
                new AlertDialog.Builder(requireContext())
                    .setTitle("永久删除")
                    .setMessage("确定永久删除\"" + note.getTitle() + "\"吗？此操作不可恢复！")
                    .setPositiveButton("删除", (d, w) -> {
                        noteViewModel.permanentDeleteNote(note.getId());
                        Toast.makeText(getContext(), "已永久删除", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
            }
        });
    }
}
