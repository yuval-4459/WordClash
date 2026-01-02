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

    private final List<LeaderboardActivity.LeaderboardEntry> entries;

    public LeaderboardAdapter() {
        this.entries = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardActivity.LeaderboardEntry entry = entries.get(position);

        holder.tvPosition.setText("#" + entry.position);
        holder.tvUsername.setText(entry.username);
        holder.tvScore.setText(entry.totalScore + " pts");

        // Add medal emoji for top 3
        if (entry.position == 1) {
            holder.tvPosition.setText("🥇");
        } else if (entry.position == 2) {
            holder.tvPosition.setText("🥈");
        } else if (entry.position == 3) {
            holder.tvPosition.setText("🥉");
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public void setEntries(List<LeaderboardActivity.LeaderboardEntry> newEntries) {
        entries.clear();
        entries.addAll(newEntries);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPosition, tvUsername, tvScore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }
}