package com.example.notemaster.util;

import android.content.Context;
import android.net.Uri;

import com.example.notemaster.data.NoteDao;
import com.example.notemaster.data.CategoryDao;
import com.example.notemaster.data.TagDao;
import com.example.notemaster.model.Note;
import com.example.notemaster.model.Category;
import com.example.notemaster.model.Tag;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BackupHelper {

    public static String exportToJson(Context context) throws JSONException {
        NoteDao noteDao = new NoteDao(context);
        CategoryDao categoryDao = new CategoryDao(context);
        TagDao tagDao = new TagDao(context);

        JSONObject root = new JSONObject();
        root.put("version", 1);
        root.put("exportDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        JSONArray notesArray = new JSONArray();
        List<Note> notes = noteDao.getAllNotes();
        for (Note note : notes) {
            JSONObject noteObj = new JSONObject();
            noteObj.put("id", note.getId());
            noteObj.put("title", note.getTitle());
            noteObj.put("content", note.getContent());
            noteObj.put("category", note.getCategory());
            noteObj.put("isPinned", note.isPinned());
            noteObj.put("isDeleted", note.isDeleted());
            noteObj.put("createdAt", note.getCreatedAt());
            noteObj.put("updatedAt", note.getUpdatedAt());
            noteObj.put("sortOrder", note.getSortOrder());
            noteObj.put("reminderTime", note.getReminderTime());
            noteObj.put("isLocked", note.isLocked());
            noteObj.put("password", note.getPassword());

            List<String> tags = tagDao.getTagsForNote(note.getId());
            JSONArray tagsArray = new JSONArray();
            for (String tag : tags) {
                tagsArray.put(tag);
            }
            noteObj.put("tags", tagsArray);

            notesArray.put(noteObj);
        }
        root.put("notes", notesArray);

        JSONArray categoriesArray = new JSONArray();
        List<Category> categories = categoryDao.getAllCategories();
        for (Category category : categories) {
            JSONObject catObj = new JSONObject();
            catObj.put("id", category.getId());
            catObj.put("name", category.getName());
            catObj.put("createdAt", category.getCreatedAt());
            categoriesArray.put(catObj);
        }
        root.put("categories", categoriesArray);

        JSONArray tagsArray = new JSONArray();
        List<Tag> tags = tagDao.getAllTags();
        for (Tag tag : tags) {
            JSONObject tagObj = new JSONObject();
            tagObj.put("id", tag.getId());
            tagObj.put("name", tag.getName());
            tagObj.put("color", tag.getColor());
            tagsArray.put(tagObj);
        }
        root.put("tags", tagsArray);

        return root.toString(2);
    }

    public static File saveBackupFile(Context context, String json) throws Exception {
        File backupDir = new File(context.getFilesDir(), "backups");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        String fileName = "NoteMaster_backup_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".json";
        File backupFile = new File(backupDir, fileName);

        FileWriter writer = new FileWriter(backupFile);
        writer.write(json);
        writer.close();

        return backupFile;
    }

    public static String readFromUri(Context context, Uri uri) throws Exception {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        inputStream.close();
        return sb.toString();
    }

    public static int importFromJson(Context context, String json) throws JSONException {
        JSONObject root = new JSONObject(json);

        NoteDao noteDao = new NoteDao(context);
        CategoryDao categoryDao = new CategoryDao(context);
        TagDao tagDao = new TagDao(context);

        int count = 0;

        if (root.has("categories")) {
            JSONArray categoriesArray = root.getJSONArray("categories");
            for (int i = 0; i < categoriesArray.length(); i++) {
                JSONObject catObj = categoriesArray.getJSONObject(i);
                String name = catObj.getString("name");
                if (!categoryDao.isCategoryNameExists(name)) {
                    Category category = new Category(name);
                    category.setCreatedAt(catObj.optLong("createdAt", System.currentTimeMillis()));
                    categoryDao.insertCategory(category);
                }
            }
        }

        if (root.has("tags")) {
            JSONArray tagsArray = root.getJSONArray("tags");
            for (int i = 0; i < tagsArray.length(); i++) {
                JSONObject tagObj = tagsArray.getJSONObject(i);
                String name = tagObj.getString("name");
                if (!tagDao.isTagNameExists(name)) {
                    Tag tag = new Tag(name, tagObj.optString("color", "#6200EE"));
                    tagDao.insertTag(tag);
                }
            }
        }

        if (root.has("notes")) {
            JSONArray notesArray = root.getJSONArray("notes");
            for (int i = 0; i < notesArray.length(); i++) {
                JSONObject noteObj = notesArray.getJSONObject(i);
                Note note = new Note();
                note.setTitle(noteObj.getString("title"));
                note.setContent(noteObj.optString("content", ""));
                note.setCategory(noteObj.optString("category", ""));
                note.setPinned(noteObj.optBoolean("isPinned", false));
                note.setDeleted(noteObj.optBoolean("isDeleted", false));
                note.setCreatedAt(noteObj.optLong("createdAt", System.currentTimeMillis()));
                note.setUpdatedAt(noteObj.optLong("updatedAt", System.currentTimeMillis()));
                note.setSortOrder(noteObj.optInt("sortOrder", 0));
                note.setReminderTime(noteObj.optLong("reminderTime", 0));
                note.setLocked(noteObj.optBoolean("isLocked", false));
                note.setPassword(noteObj.optString("password", ""));

                long noteId = noteDao.insertNote(note);

                if (noteObj.has("tags")) {
                    JSONArray tagsArray = noteObj.getJSONArray("tags");
                    for (int j = 0; j < tagsArray.length(); j++) {
                        tagDao.addTagToNote(noteId, tagDao.getTagIdByName(tagsArray.getString(j)));
                    }
                }

                count++;
            }
        }

        return count;
    }
}
