package com.example.notemaster.ui.note;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.notemaster.R;
import com.example.notemaster.model.Note;
import com.example.notemaster.viewmodel.TagViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_LIST = 0;
    private static final int VIEW_TYPE_GRID = 1;

    private List<Note> notes = new ArrayList<>();
    private OnNoteClickListener listener;
    private OnSelectionChangedListener selectionListener;
    private TagViewModel tagViewModel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private SimpleDateFormat gridDateFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());

    private boolean multiSelectMode = false;
    private boolean isGridView = false;
    private Set<Long> selectedNoteIds = new HashSet<>();

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
        void onNoteLongClick(Note note);
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int count);
    }

    public void setOnNoteClickListener(OnNoteClickListener listener) {
        this.listener = listener;
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionListener = listener;
    }

    public void setTagViewModel(TagViewModel tagViewModel) {
        this.tagViewModel = tagViewModel;
    }

    public void setGridView(boolean isGridView) {
        this.isGridView = isGridView;
        notifyDataSetChanged();
    }

    public boolean isGridView() {
        return isGridView;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    public void onItemMove(int fromPosition, int toPosition) {
        Note movedNote = notes.remove(fromPosition);
        notes.add(toPosition, movedNote);
        notifyItemMoved(fromPosition, toPosition);
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void enterMultiSelectMode() {
        multiSelectMode = true;
        selectedNoteIds.clear();
        notifyDataSetChanged();
    }

    public void exitMultiSelectMode() {
        multiSelectMode = false;
        selectedNoteIds.clear();
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(0);
        }
    }

    public void toggleSelection(long noteId) {
        if (selectedNoteIds.contains(noteId)) {
            selectedNoteIds.remove(noteId);
        } else {
            selectedNoteIds.add(noteId);
        }
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(selectedNoteIds.size());
        }
    }

    public void selectAll() {
        selectedNoteIds.clear();
        for (Note note : notes) {
            selectedNoteIds.add(note.getId());
        }
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(selectedNoteIds.size());
        }
    }

    public void deselectAll() {
        selectedNoteIds.clear();
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(0);
        }
    }

    public boolean isAllSelected() {
        return selectedNoteIds.size() == notes.size() && !notes.isEmpty();
    }

    public List<Long> getSelectedNoteIds() {
        return new ArrayList<>(selectedNoteIds);
    }

    public int getSelectedCount() {
        return selectedNoteIds.size();
    }

    public boolean isMultiSelectMode() {
        return multiSelectMode;
    }

    @Override
    public int getItemViewType(int position) {
        return isGridView ? VIEW_TYPE_GRID : VIEW_TYPE_LIST;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_GRID) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_note_grid, parent, false);
            return new GridViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_note, parent, false);
            return new NoteViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Note currentNote = notes.get(position);
        if (holder instanceof GridViewHolder) {
            ((GridViewHolder) holder).bind(currentNote);
        } else if (holder instanceof NoteViewHolder) {
            ((NoteViewHolder) holder).bind(currentNote);
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    class GridViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView contentPreviewTextView;
        private TextView categoryTextView;
        private TextView updatedAtTextView;
        private ImageView noteImageView;
        private TextView todoProgressTextView;
        private CheckBox selectCheckBox;

        public GridViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            contentPreviewTextView = itemView.findViewById(R.id.contentPreviewTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            updatedAtTextView = itemView.findViewById(R.id.updatedAtTextView);
            noteImageView = itemView.findViewById(R.id.noteImageView);
            todoProgressTextView = itemView.findViewById(R.id.todoProgressTextView);
            selectCheckBox = itemView.findViewById(R.id.selectCheckBox);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                if (multiSelectMode) {
                    toggleSelection(notes.get(position).getId());
                } else if (listener != null) {
                    listener.onNoteClick(notes.get(position));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return false;

                if (!multiSelectMode && listener != null) {
                    listener.onNoteLongClick(notes.get(position));
                    return true;
                }
                return false;
            });
        }

        public void bind(Note note) {
            titleTextView.setText(note.getTitle());

            if (multiSelectMode) {
                selectCheckBox.setVisibility(View.VISIBLE);
                selectCheckBox.setChecked(selectedNoteIds.contains(note.getId()));
                itemView.setActivated(selectedNoteIds.contains(note.getId()));
            } else {
                selectCheckBox.setVisibility(View.GONE);
                itemView.setActivated(false);
            }

            String contentPreview = note.getContent();
            String firstImageUrl = null;
            if (contentPreview != null) {
                int todoIndex = contentPreview.indexOf("[TODO]");
                if (todoIndex > 0) {
                    contentPreview = contentPreview.substring(0, todoIndex);
                }

                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("!\\[图片\\]\\(([^)]+)\\)");
                java.util.regex.Matcher matcher = pattern.matcher(contentPreview);
                if (matcher.find()) {
                    firstImageUrl = matcher.group(1);
                }

                contentPreview = contentPreview.replaceAll("!\\[图片\\]\\([^)]+\\)", "").replaceAll("<[^>]*>", "").trim();
                if (contentPreview.length() > 60) {
                    contentPreview = contentPreview.substring(0, 60) + "...";
                }
                if (!contentPreview.isEmpty()) {
                    contentPreviewTextView.setText(contentPreview);
                    contentPreviewTextView.setVisibility(View.VISIBLE);
                } else {
                    contentPreviewTextView.setVisibility(View.GONE);
                }
            } else {
                contentPreviewTextView.setVisibility(View.GONE);
            }

            if (firstImageUrl != null && !firstImageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(firstImageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.ic_image)
                        .into(noteImageView);
                noteImageView.setVisibility(View.VISIBLE);
            } else {
                noteImageView.setVisibility(View.GONE);
            }

            if (note.getCategory() != null && !note.getCategory().isEmpty()) {
                categoryTextView.setText(note.getCategory());
                categoryTextView.setVisibility(View.VISIBLE);
            } else {
                categoryTextView.setVisibility(View.GONE);
            }

            updatedAtTextView.setText(gridDateFormat.format(new Date(note.getUpdatedAt())));

            String content = note.getContent();
            if (content != null && content.contains("[TODO]")) {
                int total = 0;
                int completed = 0;
                String[] lines = content.split("\n");
                boolean inTodoSection = false;
                for (String line : lines) {
                    line = line.trim();
                    if (line.equals("[TODO]")) {
                        inTodoSection = true;
                        continue;
                    }
                    if (inTodoSection) {
                        if (line.startsWith("[x]") || line.startsWith("[ ]")) {
                            total++;
                            if (line.startsWith("[x]")) {
                                completed++;
                            }
                        }
                    }
                }
                if (total > 0) {
                    todoProgressTextView.setText("待办: " + completed + "/" + total);
                    todoProgressTextView.setVisibility(View.VISIBLE);
                } else {
                    todoProgressTextView.setVisibility(View.GONE);
                }
            } else {
                todoProgressTextView.setVisibility(View.GONE);
            }
        }
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView contentPreviewTextView;
        private TextView categoryTextView;
        private TextView updatedAtTextView;
        private ImageView pinnedIcon;
        private ImageView noteImageView;
        private com.google.android.material.chip.ChipGroup tagChipGroup;
        private TextView todoProgressTextView;
        private CheckBox selectCheckBox;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            contentPreviewTextView = itemView.findViewById(R.id.contentPreviewTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            updatedAtTextView = itemView.findViewById(R.id.updatedAtTextView);
            pinnedIcon = itemView.findViewById(R.id.pinnedIcon);
            noteImageView = itemView.findViewById(R.id.noteImageView);
            tagChipGroup = itemView.findViewById(R.id.noteTagChipGroup);
            todoProgressTextView = itemView.findViewById(R.id.todoProgressTextView);
            selectCheckBox = itemView.findViewById(R.id.selectCheckBox);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                if (multiSelectMode) {
                    toggleSelection(notes.get(position).getId());
                } else if (listener != null) {
                    listener.onNoteClick(notes.get(position));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return false;

                if (!multiSelectMode && listener != null) {
                    listener.onNoteLongClick(notes.get(position));
                    return true;
                }
                return false;
            });
        }

        public void bind(Note note) {
            titleTextView.setText(note.getTitle());

            if (multiSelectMode) {
                selectCheckBox.setVisibility(View.VISIBLE);
                selectCheckBox.setChecked(selectedNoteIds.contains(note.getId()));
                itemView.setActivated(selectedNoteIds.contains(note.getId()));
            } else {
                selectCheckBox.setVisibility(View.GONE);
                itemView.setActivated(false);
            }

            String contentPreview = note.getContent();
            String firstImageUrl = null;
            if (contentPreview != null) {
                int todoIndex = contentPreview.indexOf("[TODO]");
                if (todoIndex > 0) {
                    contentPreview = contentPreview.substring(0, todoIndex);
                }

                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("!\\[图片\\]\\(([^)]+)\\)");
                java.util.regex.Matcher matcher = pattern.matcher(contentPreview);
                if (matcher.find()) {
                    firstImageUrl = matcher.group(1);
                }

                contentPreview = contentPreview.replaceAll("!\\[图片\\]\\([^)]+\\)", "").replaceAll("<[^>]*>", "").trim();
                if (contentPreview.length() > 100) {
                    contentPreview = contentPreview.substring(0, 100) + "...";
                }
                if (!contentPreview.isEmpty()) {
                    contentPreviewTextView.setText(contentPreview);
                    contentPreviewTextView.setVisibility(View.VISIBLE);
                } else {
                    contentPreviewTextView.setVisibility(View.GONE);
                }
            } else {
                contentPreviewTextView.setVisibility(View.GONE);
            }

            if (firstImageUrl != null && !firstImageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(firstImageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.ic_image)
                        .into(noteImageView);
                noteImageView.setVisibility(View.VISIBLE);
            } else {
                noteImageView.setVisibility(View.GONE);
            }

            if (note.getCategory() != null && !note.getCategory().isEmpty()) {
                categoryTextView.setText(note.getCategory());
                categoryTextView.setVisibility(View.VISIBLE);
            } else {
                categoryTextView.setVisibility(View.GONE);
            }

            updatedAtTextView.setText(dateFormat.format(new Date(note.getUpdatedAt())));
            pinnedIcon.setVisibility(note.isPinned() ? View.VISIBLE : View.GONE);

            tagChipGroup.removeAllViews();
            if (tagViewModel != null) {
                List<String> tags = tagViewModel.getTagsForNote(note.getId());
                for (String tagName : tags) {
                    com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(itemView.getContext());
                    chip.setText(tagName);
                    chip.setClickable(false);
                    chip.setTextSize(10);
                    tagChipGroup.addView(chip);
                }
                tagChipGroup.setVisibility(tags.isEmpty() ? View.GONE : View.VISIBLE);
            } else {
                tagChipGroup.setVisibility(View.GONE);
            }

            String content = note.getContent();
            if (content != null && content.contains("[TODO]")) {
                int total = 0;
                int completed = 0;
                String[] lines = content.split("\n");
                boolean inTodoSection = false;
                for (String line : lines) {
                    line = line.trim();
                    if (line.equals("[TODO]")) {
                        inTodoSection = true;
                        continue;
                    }
                    if (inTodoSection) {
                        if (line.startsWith("[x]") || line.startsWith("[ ]")) {
                            total++;
                            if (line.startsWith("[x]")) {
                                completed++;
                            }
                        }
                    }
                }
                if (total > 0) {
                    todoProgressTextView.setText("待办: " + completed + "/" + total);
                    todoProgressTextView.setVisibility(View.VISIBLE);
                } else {
                    todoProgressTextView.setVisibility(View.GONE);
                }
            } else {
                todoProgressTextView.setVisibility(View.GONE);
            }
        }
    }
}
