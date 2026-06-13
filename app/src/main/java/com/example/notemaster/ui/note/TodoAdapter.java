package com.example.notemaster.ui.note;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notemaster.R;
import com.example.notemaster.model.TodoItem;

import java.util.ArrayList;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
    private List<TodoItem> todoItems = new ArrayList<>();
    private OnTodoActionListener listener;

    public interface OnTodoActionListener {
        void onToggle(TodoItem item, int position);
        void onDelete(TodoItem item, int position);
        void onTextChanged(TodoItem item, String text);
    }

    public void setOnTodoActionListener(OnTodoActionListener listener) {
        this.listener = listener;
    }

    public void setTodoItems(List<TodoItem> items) {
        this.todoItems = items;
        notifyDataSetChanged();
    }

    public List<TodoItem> getTodoItems() {
        return todoItems;
    }

    public void addItem(TodoItem item) {
        todoItems.add(item);
        notifyItemInserted(todoItems.size() - 1);
    }

    public void removeItem(int position) {
        if (position >= 0 && position < todoItems.size()) {
            todoItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        TodoItem item = todoItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return todoItems.size();
    }

    class TodoViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        EditText editText;
        ImageButton deleteButton;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.todoCheckBox);
            editText = itemView.findViewById(R.id.todoEditText);
            deleteButton = itemView.findViewById(R.id.todoDeleteButton);
        }

        public void bind(TodoItem item) {
            checkBox.setChecked(item.isCompleted());
            editText.setText(item.getText());

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setCompleted(isChecked);
                if (listener != null) {
                    listener.onToggle(item, getAdapterPosition());
                }
            });

            editText.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus && listener != null) {
                    String newText = editText.getText().toString().trim();
                    listener.onTextChanged(item, newText);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(item, getAdapterPosition());
                }
            });
        }
    }
}
