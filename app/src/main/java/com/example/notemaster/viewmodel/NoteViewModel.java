package com.example.notemaster.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.notemaster.data.NoteRepository;
import com.example.notemaster.model.Note;

import java.util.List;

public class NoteViewModel extends AndroidViewModel {
    private NoteRepository noteRepository;
    private MutableLiveData<List<Note>> allNotes;
    private MutableLiveData<List<Note>> deletedNotes;
    private MutableLiveData<Note> currentNote;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        noteRepository = new NoteRepository(application);
        allNotes = new MutableLiveData<>();
        deletedNotes = new MutableLiveData<>();
        currentNote = new MutableLiveData<>();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public LiveData<List<Note>> getDeletedNotes() {
        return deletedNotes;
    }

    public LiveData<Note> getCurrentNote() {
        return currentNote;
    }

    public void loadAllNotes() {
        List<Note> notes = noteRepository.getAllNotes();
        allNotes.setValue(notes);
    }

    public void loadDeletedNotes() {
        List<Note> notes = noteRepository.getDeletedNotes();
        deletedNotes.setValue(notes);
    }

    public void loadNoteById(long noteId) {
        Note note = noteRepository.getNoteById(noteId);
        currentNote.setValue(note);
    }

    public void insertNote(Note note) {
        noteRepository.insertNote(note);
        loadAllNotes();
    }

    public void updateNote(Note note) {
        noteRepository.updateNote(note);
        loadAllNotes();
    }

    public void deleteNote(long noteId) {
        noteRepository.deleteNote(noteId);
        loadAllNotes();
    }

    public void restoreNote(long noteId) {
        noteRepository.restoreNote(noteId);
        loadDeletedNotes();
        loadAllNotes();
    }

    public void permanentDeleteNote(long noteId) {
        noteRepository.permanentDeleteNote(noteId);
        loadDeletedNotes();
    }

    public void searchNotes(String query) {
        List<Note> notes = noteRepository.searchNotes(query);
        allNotes.setValue(notes);
    }

    public void updateNoteOrder(long noteId, int newOrder) {
        noteRepository.updateNoteOrder(noteId, newOrder);
        loadAllNotes();
    }

    public void loadNotesByCategory(String category) {
        List<Note> notes = noteRepository.getNotesByCategory(category);
        allNotes.setValue(notes);
    }
}