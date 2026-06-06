package com.example.wordclash.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordclash.R;
import com.example.wordclash.models.Word;

import java.util.ArrayList;
import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.ViewHolder> {

    // רשימה של אובייקטים מסוג Word (אחד מהmodels)
    private final List<Word> wordList;

    // מאתחל ArrayList ריקה. אין צורך במאזין כי המסך מיועד לקריאה בלבד ללא כפתורי פעולה או אינטראקציה.
    public WordAdapter() {
        wordList = new ArrayList<>();
    }

    // מנפח את ה-XML של פריט מילה רגיל (item_word) ומחזיר ViewHolder.
    @NonNull
    @Override
    public WordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word, parent, false);
        return new ViewHolder(view);
    }

    // שולף את המילה ומציג את הטקסטים המובנים שלה באנגלית ובעברית ברכיבי ה-TextView המתאימים.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Word word = wordList.get(position);
        if (word == null) return;

        // שליפת הטקסטים של המילה באנגלית ובעברית והצגתם ברכיבי ה-TextView המתאימים
        // (שני הערכים הם כבר מסוג String ולכן אין סכנת קריסה)
        holder.tvEnglish.setText(word.getEnglish());
        holder.tvHebrew.setText(word.getHebrew());
    }

    // מחזיר את גודל הרשימה המקומית לצורך קביעת כמות השורות הכוללת במסך.
    @Override
    public int getItemCount() {
        return wordList.size();
    }

    //מנקה את הרשימה, מוסיפה מילים חדשות ומרעננת את התצוגה מיד בעזרת notifyDataSetChanged.
    public void setWordList(List<Word> words) {
        wordList.clear();
        wordList.addAll(words);
        notifyDataSetChanged();
    }

    // מחלקה סטטית השומרת את רכיבי הטקסט של השורה בזיכרון לחסכון במשאבים ובסוללה.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvEnglish;
        final TextView tvHebrew;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEnglish = itemView.findViewById(R.id.tvEnglish);
            tvHebrew = itemView.findViewById(R.id.tvHebrew);
        }
    }
}