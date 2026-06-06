package com.example.wordclash.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordclash.R;
import com.example.wordclash.screens.LeaderboardActivity;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    // רשימה של אובייקטים מסוג LeaderboardEntry (מחלקה פנימית סטטית שהוגדרה בתוך LeaderboardActivity לייצוג שורה בטבלה)
    private final List<LeaderboardActivity.LeaderboardEntry> entries;

    // מאתחל ArrayList ריקה. אין כאן מאזין כי המסך מציג נתונים בלבד (Read-only) ללא אינטראקציה.
    public LeaderboardAdapter() {
        this.entries = new ArrayList<>();
    }

    @NonNull
    @Override
    // ה-ViewHolder הוא פשוט "מחסן קטן" שמחזיק את הרכיבים האלה בזיכרון, כדי שלא נצטרך לעשות findViewById בכל פעם ששורה זזה
    // ה-holder הוא פשוט הכינוי של השורה הספציפית שאותה המערכת ממחזרת ומציגה כרגע על המסך.

    //מנפח את ה-XML של שורת טבלה (item_leaderboard) ועוטף אותה ב-ViewHolder.
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    //מחבר את נתוני השחקן לרכיבי הגרפיקה, ומחליף את המקומות 1-3 באמוג'י של מדליות כלוגיקה עיצובית.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardActivity.LeaderboardEntry entry = entries.get(position);

        //שרשור המספר עם מחרוזת הופך אותו אוטומטית ל-String ומונע קריסה של setText באנדרואיד.
        holder.tvPosition.setText("#" + entry.position);
        holder.tvUsername.setText(entry.username);
        holder.tvScore.setText(entry.totalScore + " pts");

        if (entry.position == 1) {
            holder.tvPosition.setText("🥇");
        } else if (entry.position == 2) {
            holder.tvPosition.setText("🥈");
        } else if (entry.position == 3) {
            holder.tvPosition.setText("🥉");
        }
    }

    //מחזיר את גודל הרשימה המקומית עבור ה-RecyclerView.
    @Override
    public int getItemCount() {
        return entries.size();
    }

    //מנקה את הרשימה, מכניסה את המיקומים החדשים שנקראו מה-Firebase ומרעננת את התצוגה עם notifyDataSetChanged.
    public void setEntries(List<LeaderboardActivity.LeaderboardEntry> newEntries) {
        entries.clear();
        entries.addAll(newEntries);
        notifyDataSetChanged();
    }

    //מחזיק את רכיבי הטקסט של שורת הטבלה בזיכרון כדי למנוע שימוש חוזר ב-findViewById האיטית והכבדה.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvPosition;
        final TextView tvUsername;
        final TextView tvScore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }
}