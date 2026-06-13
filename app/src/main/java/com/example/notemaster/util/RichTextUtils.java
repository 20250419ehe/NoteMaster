package com.example.notemaster.util;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class RichTextUtils {

    public static void toggleBold(EditText editText) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        if (start == end) {
            return;
        }

        SpannableStringBuilder spannable = new SpannableStringBuilder(editText.getText());
        StyleSpan[] spans = spannable.getSpans(start, end, StyleSpan.class);

        boolean isBold = false;
        for (StyleSpan span : spans) {
            if (span.getStyle() == Typeface.BOLD) {
                isBold = true;
                break;
            }
        }

        if (isBold) {
            for (StyleSpan span : spans) {
                if (span.getStyle() == Typeface.BOLD) {
                    spannable.removeSpan(span);
                }
            }
        } else {
            spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        editText.setText(spannable);
        editText.setSelection(start, end);
    }

    public static void toggleItalic(EditText editText) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        if (start == end) {
            return;
        }

        SpannableStringBuilder spannable = new SpannableStringBuilder(editText.getText());
        StyleSpan[] spans = spannable.getSpans(start, end, StyleSpan.class);

        boolean isItalic = false;
        for (StyleSpan span : spans) {
            if (span.getStyle() == Typeface.ITALIC) {
                isItalic = true;
                break;
            }
        }

        if (isItalic) {
            for (StyleSpan span : spans) {
                if (span.getStyle() == Typeface.ITALIC) {
                    spannable.removeSpan(span);
                }
            }
        } else {
            spannable.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        editText.setText(spannable);
        editText.setSelection(start, end);
    }

    public static void toggleUnderline(EditText editText) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        if (start == end) {
            return;
        }

        SpannableStringBuilder spannable = new SpannableStringBuilder(editText.getText());
        UnderlineSpan[] spans = spannable.getSpans(start, end, UnderlineSpan.class);

        if (spans.length > 0) {
            for (UnderlineSpan span : spans) {
                spannable.removeSpan(span);
            }
        } else {
            spannable.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        editText.setText(spannable);
        editText.setSelection(start, end);
    }

    public static void toggleHeading(EditText editText) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        Layout layout = editText.getLayout();
        if (layout == null) return;

        int line = layout.getLineForOffset(start);
        int lineStart = layout.getLineStart(line);
        int lineEnd = layout.getLineEnd(line);

        String lineText = editText.getText().toString().substring(lineStart, lineEnd);

        SpannableStringBuilder spannable = new SpannableStringBuilder(editText.getText());

        if (lineText.startsWith("# ")) {
            spannable.delete(lineStart, lineStart + 2);
            spannable.removeSpan(spannable.getSpans(lineStart, lineStart, AbsoluteSizeSpan.class));
        } else {
            spannable.insert(lineStart, "# ");
            spannable.setSpan(new AbsoluteSizeSpan(24, true), lineStart, lineEnd + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        editText.setText(spannable);
        editText.setSelection(Math.min(start + 2, editText.getText().length()));
    }

    public static void toggleQuote(EditText editText) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        Layout layout = editText.getLayout();
        if (layout == null) return;

        int line = layout.getLineForOffset(start);
        int lineStart = layout.getLineStart(line);
        int lineEnd = layout.getLineEnd(line);

        String lineText = editText.getText().toString().substring(lineStart, lineEnd);

        SpannableStringBuilder spannable = new SpannableStringBuilder(editText.getText());

        if (lineText.startsWith("> ")) {
            spannable.delete(lineStart, lineStart + 2);
        } else {
            spannable.insert(lineStart, "> ");
        }

        editText.setText(spannable);
        editText.setSelection(Math.min(start + 2, editText.getText().length()));
    }

    public static void toggleBulletList(EditText editText) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        Layout layout = editText.getLayout();
        if (layout == null) return;

        int line = layout.getLineForOffset(start);
        int lineStart = layout.getLineStart(line);
        int lineEnd = layout.getLineEnd(line);

        String lineText = editText.getText().toString().substring(lineStart, lineEnd);

        SpannableStringBuilder spannable = new SpannableStringBuilder(editText.getText());

        if (lineText.startsWith("• ")) {
            spannable.delete(lineStart, lineStart + 2);
            BulletSpan[] spans = spannable.getSpans(lineStart, lineStart + 2, BulletSpan.class);
            for (BulletSpan span : spans) {
                spannable.removeSpan(span);
            }
        } else {
            spannable.insert(lineStart, "• ");
            spannable.setSpan(new BulletSpan(16), lineStart, lineEnd + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        editText.setText(spannable);
        editText.setSelection(Math.min(start + 2, editText.getText().length()));
    }

    public static void toggleNumberedList(EditText editText) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        Layout layout = editText.getLayout();
        if (layout == null) return;

        int line = layout.getLineForOffset(start);
        int lineStart = layout.getLineStart(line);
        int lineEnd = layout.getLineEnd(line);

        String lineText = editText.getText().toString().substring(lineStart, lineEnd);

        SpannableStringBuilder spannable = new SpannableStringBuilder(editText.getText());

        if (lineText.matches("^\\d+\\.\\s.*")) {
            int dotIndex = lineText.indexOf(". ");
            spannable.delete(lineStart, lineStart + dotIndex + 2);
        } else {
            int lineNumber = 1;
            for (int i = 0; i < line; i++) {
                String prevLine = editText.getText().toString().substring(
                        layout.getLineStart(i), layout.getLineEnd(i));
                if (prevLine.matches("^\\d+\\.\\s.*")) {
                    lineNumber++;
                }
            }
            spannable.insert(lineStart, lineNumber + ". ");
        }

        editText.setText(spannable);
        editText.setSelection(Math.min(start + 3, editText.getText().length()));
    }

    public static String getPlainText(EditText editText) {
        return editText.getText().toString();
    }
}
