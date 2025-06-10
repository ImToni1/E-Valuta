package com.example.app;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.app.api.Coin;
import com.example.app.database.PortfolioEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TradableCoinAdapter extends RecyclerView.Adapter<TradableCoinAdapter.ViewHolder> {

    private final Context context;
    private List<Coin> coinList;
    private final OnCoinClickListener onCoinClickListener;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private final Map<String, Double> ownedCoinsMap = new HashMap<>();

    public interface OnCoinClickListener {
        void onCoinClick(Coin coin);
    }

    public TradableCoinAdapter(Context context, List<Coin> coinList, OnCoinClickListener onCoinClickListener) {
        this.context = context;
        this.coinList = new ArrayList<>(coinList);
        this.onCoinClickListener = onCoinClickListener;
    }

    public void setCoins(List<Coin> newCoins) {
        this.coinList.clear();
        this.coinList.addAll(newCoins);
        notifyDataSetChanged();
    }

    public void setOwnedCoins(List<PortfolioEntry> ownedEntries) {
        ownedCoinsMap.clear();
        if (ownedEntries != null) {
            for (PortfolioEntry entry : ownedEntries) {
                ownedCoinsMap.put(entry.getCoinId(), entry.getAmount());
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_trade_coin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Coin coin = coinList.get(position);
        holder.coinName.setText(coin.getName());
        holder.coinSymbol.setText(coin.getSymbol().toUpperCase());
        holder.coinPrice.setText(String.format(Locale.US, "$%,.2f", coin.getCurrentPrice()));

        Glide.with(context).load(coin.getImage()).into(holder.coinImage);

        if (ownedCoinsMap.containsKey(coin.getId())) {
            Double amount = ownedCoinsMap.get(coin.getId());
            holder.ownedAmount.setVisibility(View.VISIBLE);
            holder.ownedAmount.setText(String.format(Locale.US, "Posjedujete: %.6f", amount));
        } else {
            holder.ownedAmount.setVisibility(View.GONE);
        }

        holder.itemView.setBackgroundColor(selectedPosition == position ? Color.parseColor("#E0E0E0") : Color.TRANSPARENT);

        holder.itemView.setOnClickListener(v -> {
            if (selectedPosition != holder.getAdapterPosition()) {
                notifyItemChanged(selectedPosition);
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(selectedPosition);
                onCoinClickListener.onCoinClick(coin);
            }
        });
    }

    @Override
    public int getItemCount() {
        return coinList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView coinImage;
        TextView coinName, coinSymbol, coinPrice, ownedAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            coinImage = itemView.findViewById(R.id.coin_image);
            coinName = itemView.findViewById(R.id.coin_name);
            coinSymbol = itemView.findViewById(R.id.coin_symbol);
            coinPrice = itemView.findViewById(R.id.coin_price);
            ownedAmount = itemView.findViewById(R.id.owned_amount_textview);
        }
    }
}