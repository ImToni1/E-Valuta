package com.example.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.app.api.ApiServiceCoinGecko;
import com.example.app.api.Coin;
import com.example.app.api.RetrofitClientCoinGecko;
import com.example.app.database.AppDatabase;
import com.example.app.database.PortfolioDao;
import com.example.app.database.PortfolioEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Wallet extends AppCompatActivity {

    private RecyclerView walletRecyclerView;
    private WalletAdapter walletAdapter;
    private TextView totalValueTextView, balanceValue;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loadingProgressBar;

    private AppDatabase db;
    private PortfolioDao portfolioDao;
    private Call<List<Coin>> marketDataCall;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        db = AppDatabase.getDatabase(this);
        portfolioDao = db.portfolioDao();

        initViews();
        setupRecyclerView();
        setupBottomNavigation();
        setupSwipeRefresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPortfolioData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (marketDataCall != null) {
            marketDataCall.cancel();
        }
    }

    private void initViews() {
        walletRecyclerView = findViewById(R.id.wallet_recyclerview);
        totalValueTextView = findViewById(R.id.total_value_textview);
        balanceValue = findViewById(R.id.balance_value);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        loadingProgressBar = findViewById(R.id.loading_progressbar);
    }

    private void setupRecyclerView() {
        walletAdapter = new WalletAdapter(new ArrayList<>(), this);
        walletRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        walletRecyclerView.setAdapter(walletAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> fetchMarketData(true));
    }

    private void loadPortfolioData() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        walletRecyclerView.setVisibility(View.GONE);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<PortfolioEntry> portfolioEntries = portfolioDao.getPortfolio();
            if (portfolioEntries.isEmpty()) {
                runOnUiThread(() -> {
                    totalValueTextView.setText(String.format(Locale.US, "$%,.2f", 0.0));
                    walletAdapter.updateData(new ArrayList<>());
                    stopLoadingIndicators();
                });
            } else {
                runOnUiThread(() -> fetchMarketData(false));
            }
        });
        loadBalance();
    }

    private void fetchMarketData(boolean forceRefresh) {
        DataCache cache = DataCache.getInstance();

        if (cache.isCacheValid() && !forceRefresh) {
            Log.d("WalletActivity", "Učitavanje podataka iz CACHE-a.");
            updatePortfolioWithMarketData(cache.getCachedCoins());
            return;
        }

        if (isLoading) return;
        isLoading = true;

        ApiServiceCoinGecko apiService = RetrofitClientCoinGecko.getApi();
        marketDataCall = apiService.getMarketData("usd", "market_cap_desc", 250, 1, false);

        marketDataCall.enqueue(new Callback<List<Coin>>() {
            @Override
            public void onResponse(Call<List<Coin>> call, Response<List<Coin>> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    DataCache.getInstance().setCachedCoins(response.body());
                    updatePortfolioWithMarketData(response.body());
                } else {
                    Toast.makeText(Wallet.this, "Greška pri dohvaćanju cijena", Toast.LENGTH_SHORT).show();
                    stopLoadingIndicators();
                }
            }

            @Override
            public void onFailure(Call<List<Coin>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (!call.isCanceled()) {
                    Toast.makeText(Wallet.this, "Mrežna greška", Toast.LENGTH_SHORT).show();
                }
                stopLoadingIndicators();
            }
        });
    }

    private void updatePortfolioWithMarketData(List<Coin> marketCoins) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<PortfolioEntry> portfolioEntries = portfolioDao.getPortfolio();
            List<Coin> portfolioCoins = new ArrayList<>();
            double totalValue = 0;

            Map<String, Coin> marketDataMap = new HashMap<>();
            for (Coin coin : marketCoins) {
                marketDataMap.put(coin.getId(), coin);
            }

            for (PortfolioEntry entry : portfolioEntries) {
                Coin marketCoin = marketDataMap.get(entry.getCoinId());
                if (marketCoin != null) {
                    // Postavimo količinu koju imamo na objekt preuzet s tržišta
                    marketCoin.setOwnedAmount(entry.getAmount());
                    portfolioCoins.add(marketCoin);
                    totalValue += entry.getAmount() * marketCoin.getCurrentPrice();
                }
            }

            double finalTotalValue = totalValue;
            runOnUiThread(() -> {
                totalValueTextView.setText(String.format(Locale.US, "$%,.2f", finalTotalValue));
                walletAdapter.updateData(portfolioCoins);
                stopLoadingIndicators();
            });
        });
    }

    private void stopLoadingIndicators() {
        isLoading = false;
        loadingProgressBar.setVisibility(View.GONE);
        walletRecyclerView.setVisibility(View.VISIBLE);
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void loadBalance() {
        SharedPreferences prefs = getSharedPreferences("BalancePrefs", Context.MODE_PRIVATE);
        double userBalance = Double.longBitsToDouble(prefs.getLong("UserBalance", Double.doubleToLongBits(1000.0)));
        balanceValue.setText(String.format(Locale.US, "Gotovina: $%,.2f", userBalance));
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.nav_wallet);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), Home.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_wallet) {
                return true;
            } else if (itemId == R.id.nav_trade) {
                startActivity(new Intent(getApplicationContext(), Trade.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(getApplicationContext(), Settings.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }
}