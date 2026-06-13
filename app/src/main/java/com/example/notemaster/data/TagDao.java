package com.example.notemaster.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.notemaster.model.Tag;
import com.example.notemaster.util.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class TagDao {
    private DatabaseHelper dbHelper;

    public TagDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insertTag(Tag tag) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TAG_NAME, tag.getName());
        values.put(DatabaseHelper.COLUMN_TAG_COLOR, tag.getColor());
        long id = db.insert(DatabaseHelper.TABLE_TAGS, null, values);
        db.close();
        return id;
    }

    public int deleteTag(long tagId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_TAGS,
                DatabaseHelper.COLUMN_TAG_ID + " = ?",
                new String[]{String.valueOf(tagId)});
        db.close();
        return rows;
    }

    public List<Tag> getAllTags() {
        List<Tag> tags = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_TAGS, null, null, null, null, null,
                DatabaseHelper.COLUMN_TAG_NAME + " ASC");
        if (cursor.moveToFirst()) {
            do {
                Tag tag = new Tag();
                tag.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TAG_ID)));
                tag.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TAG_NAME)));
                tag.setColor(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TAG_COLOR)));
                tags.add(tag);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tags;
    }

    public boolean isTagNameExists(String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_TAGS, null,
                DatabaseHelper.COLUMN_TAG_NAME + " = ?",
                new String[]{name}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public long getTagIdByName(String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_TAGS, null,
                DatabaseHelper.COLUMN_TAG_NAME + " = ?",
                new String[]{name}, null, null, null);
        long tagId = -1;
        if (cursor.moveToFirst()) {
            tagId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TAG_ID));
        }
        cursor.close();
        db.close();
        return tagId;
    }

    public void addTagToNote(long noteId, long tagId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NOTE_TAG_NOTE_ID, noteId);
        values.put(DatabaseHelper.COLUMN_NOTE_TAG_TAG_ID, tagId);
        db.insertWithOnConflict(DatabaseHelper.TABLE_NOTE_TAGS, null, values,
                SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    public void removeTagFromNote(long noteId, long tagId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_NOTE_TAGS,
                DatabaseHelper.COLUMN_NOTE_TAG_NOTE_ID + " = ? AND " +
                DatabaseHelper.COLUMN_NOTE_TAG_TAG_ID + " = ?",
                new String[]{String.valueOf(noteId), String.valueOf(tagId)});
        db.close();
    }

    public List<String> getTagsForNote(long noteId) {
        List<String> tagNames = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT t." + DatabaseHelper.COLUMN_TAG_NAME +
                " FROM " + DatabaseHelper.TABLE_TAGS + " t" +
                " INNER JOIN " + DatabaseHelper.TABLE_NOTE_TAGS + " nt" +
                " ON t." + DatabaseHelper.COLUMN_TAG_ID + " = nt." + DatabaseHelper.COLUMN_NOTE_TAG_TAG_ID +
                " WHERE nt." + DatabaseHelper.COLUMN_NOTE_TAG_NOTE_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(noteId)});
        if (cursor.moveToFirst()) {
            do {
                tagNames.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tagNames;
    }

    public void setTagsForNote(long noteId, List<String> tagNames) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_NOTE_TAGS,
                DatabaseHelper.COLUMN_NOTE_TAG_NOTE_ID + " = ?",
                new String[]{String.valueOf(noteId)});
        for (String tagName : tagNames) {
            Cursor cursor = db.query(DatabaseHelper.TABLE_TAGS, null,
                    DatabaseHelper.COLUMN_TAG_NAME + " = ?",
                    new String[]{tagName}, null, null, null);
            if (cursor.moveToFirst()) {
                long tagId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TAG_ID));
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_NOTE_TAG_NOTE_ID, noteId);
                values.put(DatabaseHelper.COLUMN_NOTE_TAG_TAG_ID, tagId);
                db.insertWithOnConflict(DatabaseHelper.TABLE_NOTE_TAGS, null, values,
                        SQLiteDatabase.CONFLICT_IGNORE);
            }
            cursor.close();
        }
        db.close();
    }
}
