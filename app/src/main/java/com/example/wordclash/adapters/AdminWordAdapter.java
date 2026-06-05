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

    // רשימה של אובייקטים מסוג Word (אחד מהmodels)
    private final List<Word> wordList;

    // הוגדר בשורה 73
    private final OnWordActionListener listener;

    // מאתחל רשימת מילים ריקה מסוג ArrayList, ומקבל את המאזין מבחוץ
    public AdminWordAdapter(OnWordActionListener listener) {
        this.wordList = new ArrayList<>();
        this.listener = listener;
    }

    // פונקציה של הRecyclerView
    // הפונקציה לוקחת את קובץ הxml של פריד בודד (item_word_admin),
    // מנפחת אותו (inflate) לאובייקט תצוגה בjava,
    // ועוטפת אותו בViewHolder שיוחזר
    @NonNull
    @Override
    public AdminWordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word_admin, parent, false);
        return new ViewHolder(view);
    }

    // הפונקציה מקבל שורה ספציפית ומיקום.
    // היא לוקחת את המילה שבמיקום הזה ומציגה את הטקסטים שלה (אנגלית, עברית, דרגה) על רכיבי ה-UI.
    // אני מגדיר כאן מה קורה כשלוחצים על כפתור המחיקה באותה שורה.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Word word = wordList.get(position);
        if (word == null) return;

        holder.tvEnglish.setText(word.getEnglish());
        holder.tvHebrew.setText(word.getHebrew());

        // אם מעבירים מספר ישירות ל-setText(),
        // המערכת בטוחה שהמספר הזה הוא מזהה של קובץ (Resource ID כמו R.string...).
        // היא תנסה לחפש קובץ כזה, לא תמצא, והאפליקציה תקרוס מיד.
        // בשביל לטפל בבעיה הזאת, המרתי את המספר למחרוזת
        holder.tvRankBadge.setText(word.getRank() + "");

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(word);
            }
        });
    }

    // מחזיר ל-RecyclerView את כמות הפריטים שיש ברשימה,
    // כדי שהמסך ידע כמה שורות הוא צריך לייצר בסך הכל.
    @Override
    public int getItemCount() {
        return wordList.size();
    }

    // כשמגיעים נתונים חדשים, היא מנקה את הרשימה הישנה, מוסיפה את כל המילים החדשות,
    // וקוראת ל-notifyDataSetChanged() שאומר ל-RecyclerView "להתרענן" כי יש נתונים חדשים על המסך.
    public void setWordList(List<Word> words) {
        wordList.clear();
        wordList.addAll(words);
        notifyDataSetChanged();
    }

    // מוצאת את המיקום של מילה מסוימת, מוחקת אותה מהרשימה המקומית,
    // ומעדכנת רק את השורה הספציפית הזו שנמחקה באמצעות notifyItemRemoved.
    // זה גורם לאנימציית מחיקה חלקה ויפה במסך (במקום לרענן את כל הרשימה מההתחלה).
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




    // תבנית עיצוב (Design Pattern) חובה ב-RecyclerView.
    // תפקידה להחזיק את רכיבי ה-UI של שורה בודדת בזיכרון (הטקסטים והכפתור)
    // ולקשר אותם באמצעות findViewById.
    // ----------------------------------------------------------------
    // זה קורה רק פעם אחת לכל שורה,
    // מה שמונע מהאפליקציה לקרוא ל-findViewById כל הזמן,
    // ומשפר את הביצועים והסוללה של המכשיר באופן דרמטי.
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