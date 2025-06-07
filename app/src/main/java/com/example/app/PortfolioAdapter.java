package com.example.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.database.PortfolioEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.PortfolioViewHolder> {

    private List<PortfolioEntry> portfolio = new ArrayList<>();

    @NonNull
    @Override
    public PortfolioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new PortfolioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PortfolioViewHolder holder, int position) {
        PortfolioEntry currentEntry = portfolio.get(position);
        holder.text1.setText(currentEntry.getName());
        holder.text2.setText(String.format(Locale.US, "Količina: %.6f %s", currentEntry.getAmount(), currentEntry.getSymbol().toUpperCase()));
    }

    @Override
    public int getItemCount() {
        return portfolio.size();
    }

    public void setPortfolio(List<PortfolioEntry> portfolio) {
        this.portfolio = portfolio;
        notifyDataSetChanged();
    }

    static class PortfolioViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;
        public PortfolioViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}