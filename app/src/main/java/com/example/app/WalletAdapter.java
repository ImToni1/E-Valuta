package com.example.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.app.api.Coin;
import java.util.List;
import java.util.Locale;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.WalletViewHolder> {

    private List<Coin> portfolioCoins;
    private Context context;

    public WalletAdapter(List<Coin> portfolioCoins, Context context) {
        this.portfolioCoins = portfolioCoins;
        this.context = context;
    }

    public void updateData(List<Coin> newPortfolioCoins) {
        this.portfolioCoins.clear();
        this.portfolioCoins.addAll(newPortfolioCoins);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WalletViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_coin, parent, false);
        return new WalletViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WalletViewHolder holder, int position) {
        Coin coin = portfolioCoins.get(position);
        holder.bind(coin);
    }

    @Override
    public int getItemCount() {
        return portfolioCoins.size();
    }

    class WalletViewHolder extends RecyclerView.ViewHolder {
        ImageView coinImage;
        TextView coinName, coinQuantity, totalValue;

        public WalletViewHolder(@NonNull View itemView) {
            super(itemView);
            coinImage = itemView.findViewById(R.id.coin_image);
            coinName = itemView.findViewById(R.id.coin_name);
            coinQuantity = itemView.findViewById(R.id.coin_quantity);
            totalValue = itemView.findViewById(R.id.total_value);
        }

        void bind(Coin coin) {
            coinName.setText(coin.getName());
            coinQuantity.setText(String.format(Locale.US, "%.6f %s", coin.getOwnedAmount(), coin.getSymbol().toUpperCase()));

            double value = coin.getOwnedAmount() * coin.getCurrentPrice();
            totalValue.setText(String.format(Locale.US, "$%,.2f", value));

            Glide.with(context)
                    .load(coin.getImage())
                    .circleCrop()
                    .into(coinImage);
        }
    }
}