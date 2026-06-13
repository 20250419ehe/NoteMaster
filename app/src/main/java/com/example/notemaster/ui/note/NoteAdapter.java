package com.example.notemaster.ui.note;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notemaster.R;
import com.example.notemaster.model.Note;
import com.example.notemaster.viewmodel.TagViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Note> notes = new ArrayList<>();
    private OnNoteClickListener listener;
    private TagViewModel tagViewModel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
        void onNoteLongClick(Note note);
    }

    public void setOnNoteClickListener(OnNoteClickListener listener) {
        this.listener = listener;
    }

    public void setTagViewModel(TagViewModel tagViewModel) {
        this.tagViewModel = tagViewModel;
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

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note currentNote = notes.get(position);
        holder.bind(currentNote);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView contentPreviewTextView;
        private TextView categoryTextView;
        private TextView updatedAtTextView;
        private ImageView pinnedIcon;
        private com.google.android.material.chip.ChipGroup tagChipGroup;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            contentPreviewTextView = itemView.findViewById(R.id.contentPreviewTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            updatedAtTextView = itemView.findViewById(R.id.updatedAtTextView);
            pinnedIcon = itemView.findViewById(R.id.pinnedIcon);
            tagChipGroup = itemView.findViewById(R.id.noteTagChipGroup);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onNoteClick(notes.get(position));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onNoteLongClick(notes.get(position));
                    return true;
                }
                return false;
            });
        }

        public void bind(Note note) {
            titleTextView.setText(note.getTitle());
            
            // 内容预览（去除HTML标签）
            String contentPreview = note.getContent();
            if (contentPreview != null) {
                contentPreview = contentPreview.replaceAll("<[^>]*>", "");
                if (contentPreview.length() > 100) {
                    contentPreview = contentPreview.substring(0, 100) + "...";
                }
                contentPreviewTextView.setText(contentPreview);
                contentPreviewTextView.setVisibility(View.VISIBLE);
            } else {
                contentPreviewTextView.setVisibility(View.GONE);
            }

            // 分类
            if (note.getCategory() != null && !note.getCategory().isEmpty()) {
                categoryTextView.setText(note.getCategory());
                categoryTextView.setVisibility(View.VISIBLE);
            } else {
                categoryTextView.setVisibility(View.GONE);
            }

            // 更新时间
            updatedAtTextView.setText(dateFormat.format(new Date(note.getUpdatedAt())));

            // 置顶图标
            pinnedIcon.setVisibility(note.isPinned() ? View.VISIBLE : View.GONE);

            // 标签
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
        }
    }
}