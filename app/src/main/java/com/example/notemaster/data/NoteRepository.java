package com.example.notemaster.data;

import android.content.Context;

import com.example.notemaster.model.Note;

import java.util.List;

public class NoteRepository {
    private NoteDao noteDao;

    public NoteRepository(Context context) {
        this.noteDao = new NoteDao(context);
    }

    public long insertNote(Note note) {
        return noteDao.insertNote(note);
    }

    public int updateNote(Note note) {
        return noteDao.updateNote(note);
    }

    public int deleteNote(long noteId) {
        return noteDao.deleteNote(noteId);
    }

    public List<Note> getAllNotes() {
        return noteDao.getAllNotes();
    }

    public Note getNoteById(long noteId) {
        return noteDao.getNoteById(noteId);
    }

    public List<Note> searchNotes(String query) {
        return noteDao.searchNotes(query);
    }

    public List<Note> getDeletedNotes() {
        return noteDao.getDeletedNotes();
    }

    public int restoreNote(long noteId) {
        return noteDao.restoreNote(noteId);
    }

    public int permanentDeleteNote(long noteId) {
        return noteDao.permanentDeleteNote(noteId);
    }

    public void updateNoteOrder(long noteId, int newOrder) {
        noteDao.updateNoteOrder(noteId, newOrder);
    }

    public List<Note> getNotesByCategory(String category) {
        return noteDao.getNotesByCategory(category);
    }

    public List<Note> searchNotesByCategory(String query, String category) {
        return noteDao.searchNotesByCategory(query, category);
    }
}