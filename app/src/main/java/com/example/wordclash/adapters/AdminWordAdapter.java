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

    public interface OnWordActionListener {
        void onDeleteClick(Word word);
    }

    private final List<Word> wordList;
    private final OnWordActionListener listener;

    public AdminWordAdapter(OnWordActionListener listener) {
        this.wordList = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminWordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Word word = wordList.get(position);
        if (word == null) return;

        holder.tvEnglish.setText(word.getEnglish());
        holder.tvHebrew.setText(word.getHebrew());
        holder.tvRankBadge.setText(String.valueOf(word.getRank()));

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(word);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public void setWordList(List<Word> words) {
        wordList.clear();
        wordList.addAll(words);
        notifyDataSetChanged();
    }

    public void removeWord(Word word) {
        int index = wordList.indexOf(word);
        if (index == -1) return;
        wordList.remove(index);
        notifyItemRemoved(index);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEnglish, tvHebrew, tvRankBadge;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEnglish = itemView.findViewById(R.id.tvEnglish);
            tvHebrew = itemView.findViewById(R.id.tvHebrew);
            tvRankBadge = itemView.findViewById(R.id.tvRankBadge);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}