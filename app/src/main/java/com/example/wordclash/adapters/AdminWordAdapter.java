package com.example.wordclash.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordclash.R;
import com.example.wordclash.models.Word;

import java.util.ArrayList;
import java.util.List;

public class AdminWordAdapter extends RecyclerView.Adapter<AdminWordAdapter.ViewHolder> {

    // רשימה מקומית של אובייקטים מסוג Word ומאזין OnWordActionListener (שורה 73) לניהול אירועים מול ה-Activity. (
    private final List<Word> wordList;
    private final OnWordActionListener listener;

    // מאתחל ArrayList ריקה ומקבל את המאזין (Callback) מהמסך הראשי.
    public AdminWordAdapter(OnWordActionListener listener) {
        this.wordList = new ArrayList<>();
        this.listener = listener;
    }

    // מנפח (Inflate) את ה-XML של פריט בודד (item_word_admin) ועוטף אותו ב-ViewHolder לצורך תצוגה
    @NonNull
    @Override
    public AdminWordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word_admin, parent, false);
        return new ViewHolder(view);
    }

    //מחבר את נתוני המילה לפי המיקום לרכיבי ה-UI ומגדיר מאזין ללחיצה על כפתור המחיקה
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Word word = wordList.get(position);
        if (word == null) return;

        holder.tvEnglish.setText(word.getEnglish());
        holder.tvHebrew.setText(word.getHebrew());

        //המרה של המספר (int) למחרוזת כדי למנוע קריסה של setText, שמצפה לקבל Resource ID של טקסט באנדרואיד.
        holder.tvRankBadge.setText(word.getRank() + "");

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(word);
            }
        });
    }

    // מחזיר את כמות הפריטים ברשימה המקומית כדי שה-RecyclerView ידע כמה שורות לייצר
    @Override
    public int getItemCount() {
        return wordList.size();
    }

    //מעדכן את האדפטר ברשימה חדשה וקורא ל-notifyDataSetChanged לרענון כל המסך
    public void setWordList(List<Word> words) {
        wordList.clear();
        wordList.addAll(words);
        notifyDataSetChanged();
    }

    //מוחק מילה מהרשימה ומעדכן רק את השורה הספציפית באמצעות notifyItemRemoved ליצירת אנימציית מחיקה חלקה
    public void removeWord(Word word) {
        int index = wordList.indexOf(word);
        if (index == -1) return;
        wordList.remove(index);
        notifyItemRemoved(index);
    }

    // האדפטר עצמו לא יודע למחוק מילים ישירות מהFireBase
    // אז הוא משתמש במאזין הזה כדי למחוק
    public interface OnWordActionListener {
        void onDeleteClick(Word word);
    }

    //מחלקה השומרת את רכיבי ה-UI של השורה בזיכרון וחוסכת ריצות חוזרות של findViewById, דבר המשפר ביצועים וסוללה (Optimization)
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvEnglish;
        final TextView tvHebrew;
        final TextView tvRankBadge;
        final Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEnglish = itemView.findViewById(R.id.tvEnglish);
            tvHebrew = itemView.findViewById(R.id.tvHebrew);
            tvRankBadge = itemView.findViewById(R.id.tvRankBadge);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}