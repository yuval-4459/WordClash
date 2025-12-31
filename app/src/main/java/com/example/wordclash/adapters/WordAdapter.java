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

    private final List<Word> wordList;

    public WordAdapter() {
        wordList = new ArrayList<>();
    }

    @NonNull
    @Override
    public WordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Word word = wordList.get(position);
        if (word == null) return;

        holder.tvEnglish.setText(word.getEnglish());
        holder.tvHebrew.setText(word.getHebrew());
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEnglish, tvHebrew;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEnglish = itemView.findViewById(R.id.tvEnglish);
            tvHebrew = itemView.findViewById(R.id.tvHebrew);
        }
    }
}