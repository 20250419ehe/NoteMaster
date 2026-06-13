package com.example.notemaster.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.notemaster.model.Note;
import com.example.notemaster.util.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class NoteDao {
    private DatabaseHelper dbHelper;
    private Context context;

    public NoteDao(Context context) {
        this.context = context;
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    // 插入笔记
    public long insertNote(Note note) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NOTE_TITLE, note.getTitle());
        values.put(DatabaseHelper.COLUMN_NOTE_CONTENT, note.getContent());
        values.put(DatabaseHelper.COLUMN_NOTE_CATEGORY, note.getCategory());
        values.put(DatabaseHelper.COLUMN_NOTE_IS_PINNED, note.isPinned() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_NOTE_IS_DELETED, note.isDeleted() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_NOTE_IS_LOCKED, note.isLocked() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_NOTE_PASSWORD, note.getPassword());
        values.put(DatabaseHelper.COLUMN_NOTE_CREATED_AT, note.getCreatedAt());
        values.put(DatabaseHelper.COLUMN_NOTE_UPDATED_AT, note.getUpdatedAt());
        values.put(DatabaseHelper.COLUMN_NOTE_SORT_ORDER, note.getSortOrder());
        
        long id = db.insert(DatabaseHelper.TABLE_NOTES, null, values);
        db.close();
        return id;
    }

    // 更新笔记
    public int updateNote(Note note) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NOTE_TITLE, note.getTitle());
        values.put(DatabaseHelper.COLUMN_NOTE_CONTENT, note.getContent());
        values.put(DatabaseHelper.COLUMN_NOTE_CATEGORY, note.getCategory());
        values.put(DatabaseHelper.COLUMN_NOTE_IS_PINNED, note.isPinned() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_NOTE_IS_DELETED, note.isDeleted() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_NOTE_IS_LOCKED, note.isLocked() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_NOTE_PASSWORD, note.getPassword());
        values.put(DatabaseHelper.COLUMN_NOTE_UPDATED_AT, System.currentTimeMillis());
        values.put(DatabaseHelper.COLUMN_NOTE_SORT_ORDER, note.getSortOrder());
        
        int rowsAffected = db.update(DatabaseHelper.TABLE_NOTES, values,
                DatabaseHelper.COLUMN_NOTE_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
        db.close();
        return rowsAffected;
    }

    // 删除笔记（软删除）
    public int deleteNote(long noteId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NOTE_IS_DELETED, 1);
        
        int rowsAffected = db.update(DatabaseHelper.TABLE_NOTES, values,
                DatabaseHelper.COLUMN_NOTE_ID + " = ?",
                new String[]{String.valueOf(noteId)});
        db.close();
        return rowsAffected;
    }

    // 获取所有未删除的笔记
    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_NOTES +
                " WHERE " + DatabaseHelper.COLUMN_NOTE_IS_DELETED + " = 0" +
                " ORDER BY " + DatabaseHelper.COLUMN_NOTE_IS_PINNED + " DESC," +
                DatabaseHelper.COLUMN_NOTE_SORT_ORDER + " ASC";
        
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_ID)));
                note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_TITLE)));
                note.setContent(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_CONTENT)));
                note.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_CATEGORY)));
                note.setPinned(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_IS_PINNED)) == 1);
                note.setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_IS_DELETED)) == 1);
                note.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_CREATED_AT)));
                note.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_UPDATED_AT)));
                note.setSortOrder(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_SORT_ORDER)));
                note.setLocked(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_IS_LOCKED)) == 1);
                note.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_PASSWORD)));
                
                notes.add(note);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return notes;
    }

    // 根据ID获取笔记
    public Note getNoteById(long noteId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_NOTES, null,
                DatabaseHelper.COLUMN_NOTE_ID + " = ?",
                new String[]{String.valueOf(noteId)},
                null, null, null);
        
        Note note = null;
        if (cursor.moveToFirst()) {
            note = new Note();
            note.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_ID)));
            note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_TITLE)));
            note.setContent(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_CONTENT)));
            note.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_CATEGORY)));
            note.setPinned(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_IS_PINNED)) == 1);
            note.setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_IS_DELETED)) == 1);
            note.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_CREATED_AT)));
            note.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_UPDATED_AT)));
            note.setSortOrder(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_SORT_ORDER)));
            note.setLocked(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_IS_LOCKED)) == 1);
            note.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_PASSWORD)));
        }
        
        cursor.close();
        db.close();
        return note;
    }

    // 搜索笔记
    public List<Note> searchNotes(String query) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String searchQuery = "SELECT * FROM " + DatabaseHelper.TABLE_NOTES +
                " WHERE " + DatabaseHelper.COLUMN_NOTE_IS_DELETED + " = 0" +
                " AND (" + DatabaseHelper.COLUMN_NOTE_TITLE + " LIKE ?" +
                " OR " + DatabaseHelper.COLUMN_NOTE_CONTENT + " LIKE ?)" +
                " ORDER BY " + DatabaseHelper.COLUMN_NOTE_IS_PINNED + " DESC," +
                DatabaseHelper.COLUMN_NOTE_UPDATED_AT + " DESC";
        
        String[] selectionArgs = {"%" + query + "%", "%" + query + "%"};
        
        Cursor cursor = db.rawQuery(searchQuery, selectionArgs);
        
        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_ID)));
                note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_TITLE)));
                note.setContent(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_CONTENT)));
                note.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_CATEGORY)));
                note.setPinned(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_IS_PINNED)) == 1);
                note.setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_IS_DELETED)) == 1);
                note.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_CREATED_AT)));
                note.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_UPDATED_AT)));
                note.setSortOrder(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_SORT_ORDER)));
                note.setLocked(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_IS_LOCKED)) == 1);
                note.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_PASSWORD)));
                
                notes.add(note);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return notes;
    }

    // 根据分类获取笔记
    public List<Note> getNotesByCategory(String category) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_NOTES +
                " WHERE " + DatabaseHelper.COLUMN_NOTE_IS_DELETED + " = 0" +
                " AND " + DatabaseHelper.COLUMN_NOTE_CATEGORY + " = ?" +
                " ORDER BY " + DatabaseHelper.COLUMN_NOTE_IS_PINNED + " DESC," +
                DatabaseHelper.COLUMN_NOTE_SORT_ORDER + " ASC";
        
        Cursor cursor = db.rawQuery(query, new String[]{category});
        
        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_ID)));
                note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_TITLE)));
                note.setContent(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_CONTENT)));
                note.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_CATEGORY)));
                note.setPinned(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_IS_PINNED)) == 1);
                note.setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_IS_DELETED)) == 1);
                note.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_CREATED_AT)));
                note.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_UPDATED_AT)));
                note.setSortOrder(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_SORT_ORDER)));
                note.setLocked(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_IS_LOCKED)) == 1);
                note.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_PASSWORD)));
                notes.add(note);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return notes;
    }

    // 获取回收站中的笔记
    public List<Note> getDeletedNotes() {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_NOTES +
                " WHERE " + DatabaseHelper.COLUMN_NOTE_IS_DELETED + " = 1" +
                " ORDER BY " + DatabaseHelper.COLUMN_NOTE_UPDATED_AT + " DESC";
        
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_ID)));
                note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_TITLE)));
                note.setContent(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_CONTENT)));
                note.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_CATEGORY)));
                note.setPinned(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_IS_PINNED)) == 1);
                note.setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_IS_DELETED)) == 1);
                note.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_CREATED_AT)));
                note.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_UPDATED_AT)));
                note.setSortOrder(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_SORT_ORDER)));
                note.setLocked(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_IS_LOCKED)) == 1);
                note.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_PASSWORD)));
                
                notes.add(note);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return notes;
    }

    // 恢复已删除的笔记
    public int restoreNote(long noteId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NOTE_IS_DELETED, 0);
        
        int rowsAffected = db.update(DatabaseHelper.TABLE_NOTES, values,
                DatabaseHelper.COLUMN_NOTE_ID + " = ?",
                new String[]{String.valueOf(noteId)});
        db.close();
        return rowsAffected;
    }

    // 永久删除笔记
    public int permanentDeleteNote(long noteId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = db.delete(DatabaseHelper.TABLE_NOTES,
                DatabaseHelper.COLUMN_NOTE_ID + " = ?",
                new String[]{String.valueOf(noteId)});
        db.close();
        return rowsAffected;
    }

    // 按分类和关键字搜索笔记
    public List<Note> searchNotesByCategory(String query, String category) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String searchQuery = "SELECT * FROM " + DatabaseHelper.TABLE_NOTES +
                " WHERE " + DatabaseHelper.COLUMN_NOTE_IS_DELETED + " = 0" +
                " AND " + DatabaseHelper.COLUMN_NOTE_CATEGORY + " = ?" +
                " AND (" + DatabaseHelper.COLUMN_NOTE_TITLE + " LIKE ?" +
                " OR " + DatabaseHelper.COLUMN_NOTE_CONTENT + " LIKE ?)" +
                " ORDER BY " + DatabaseHelper.COLUMN_NOTE_IS_PINNED + " DESC," +
                DatabaseHelper.COLUMN_NOTE_SORT_ORDER + " ASC";

        String[] selectionArgs = {category, "%" + query + "%", "%" + query + "%"};

        Cursor cursor = db.rawQuery(searchQuery, selectionArgs);

        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_ID)));
                note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_TITLE)));
                note.setContent(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_CONTENT)));
                note.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_CATEGORY)));
                note.setPinned(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_IS_PINNED)) == 1);
                note.setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_IS_DELETED)) == 1);
                note.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_CREATED_AT)));
                note.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_UPDATED_AT)));
                note.setSortOrder(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_SORT_ORDER)));
                note.setLocked(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_IS_LOCKED)) == 1);
                note.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_PASSWORD)));
                notes.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return notes;
    }

    // 更新笔记排序
    public void updateNoteOrder(long noteId, int newOrder) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NOTE_SORT_ORDER, newOrder);
        
        db.update(DatabaseHelper.TABLE_NOTES, values,
                DatabaseHelper.COLUMN_NOTE_ID + " = ?",
                new String[]{String.valueOf(noteId)});
        db.close();
    }
}