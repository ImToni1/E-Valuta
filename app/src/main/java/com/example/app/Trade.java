package com.example.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.api.ApiServiceCoinGecko;
import com.example.app.api.Coin;
import com.example.app.api.RetrofitClientCoinGecko;
import com.example.app.database.AppDatabase;
import com.example.app.database.PortfolioDao;
import com.example.app.database.PortfolioEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Trade extends AppCompatActivity implements TradableCoinAdapter.OnCoinClickListener {

    private RecyclerView tradeRecyclerView;
    private TextView selectedCoinTextView, balanceValue;
    private TextInputEditText amountEditText;
    private Button buyButton, sellButton, addMoneyButton;
    private BottomNavigationView bottomNavigationView;
    private ProgressBar loadingProgressBar;
    private TradableCoinAdapter tradableCoinAdapter;
    private Coin selectedCoin = null;
    private AppDatabase db;
    private PortfolioDao portfolioDao;
    private static final String BALANCE_PREFS = "BalancePrefs";
    private static final String BALANCE_KEY = "UserBalance";
    private boolean isLoading = false;
    private Call<List<Coin>> marketDataCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade);

        db = AppDatabase.getDatabase(this);
        portfolioDao = db.portfolioDao();

        initViews();
        setupRecyclerView();
        setupClickListeners();
        setupBottomNavigation();

        loadBalance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPortfolioAndFetchMarketData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (marketDataCall != null) {
            marketDataCall.cancel();
        }
    }

    private void buyCrypto() {
        if (!isInputValid()) return;
        double amountToBuyUsd = Double.parseDouble(amountEditText.getText().toString());
        double currentBalance = getBalance();

        if (amountToBuyUsd > currentBalance) {
            Toast.makeText(this, "Nemate dovoljno sredstava.", Toast.LENGTH_SHORT).show();
            return;
        }

        double coinPrice = selectedCoin.getCurrentPrice();
        if (coinPrice <= 0) {
            Toast.makeText(this, "Nije moguće dohvatiti cijenu valute.", Toast.LENGTH_SHORT).show();
            return;
        }
        double amountOfCoinToBuy = amountToBuyUsd / coinPrice;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            PortfolioEntry existingEntry = portfolioDao.getSingleCoin(selectedCoin.getId());
            if (existingEntry != null) {
                existingEntry.setAmount(existingEntry.getAmount() + amountOfCoinToBuy);
                portfolioDao.update(existingEntry);
            } else {
                PortfolioEntry newEntry = new PortfolioEntry(selectedCoin.getId(), selectedCoin.getName(), selectedCoin.getSymbol(), amountOfCoinToBuy);
                portfolioDao.insert(newEntry);
            }
            runOnUiThread(() -> {
                saveBalance(currentBalance - amountToBuyUsd);
                amountEditText.setText("");
                Toast.makeText(this, "Kupnja uspješna.", Toast.LENGTH_SHORT).show();
                refreshPortfolioState();
            });
        });
    }

    private void sellCrypto() {
        if (!isInputValid()) return;
        double amountToSellUsd = Double.parseDouble(amountEditText.getText().toString());
        double coinPrice = selectedCoin.getCurrentPrice();

        if (coinPrice <= 0) {
            Toast.makeText(this, "Nije moguće dohvatiti cijenu valute.", Toast.LENGTH_SHORT).show();
            return;
        }
        double amountOfCoinToSell = amountToSellUsd / coinPrice;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            PortfolioEntry coinToSell = portfolioDao.getSingleCoin(selectedCoin.getId());

            if (coinToSell == null || amountOfCoinToSell > coinToSell.getAmount()) {
                runOnUiThread(() -> Toast.makeText(this, String.format("Nemate dovoljno %s za prodaju.", selectedCoin.getSymbol().toUpperCase()), Toast.LENGTH_SHORT).show());
                return;
            }

            double moneyToGet = amountOfCoinToSell * coinPrice;
            double newAmountInPortfolio = coinToSell.getAmount() - amountOfCoinToSell;

            if (newAmountInPortfolio < 0.000001) {
                portfolioDao.delete(coinToSell);
            } else {
                coinToSell.setAmount(newAmountInPortfolio);
                portfolioDao.update(coinToSell);
            }
            runOnUiThread(() -> {
                saveBalance(getBalance() + moneyToGet);
                amountEditText.setText("");
                Toast.makeText(this, "Prodaja uspješna.", Toast.LENGTH_SHORT).show();
                refreshPortfolioState();
            });
        });
    }
    private void refreshPortfolioState() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<PortfolioEntry> ownedCoins = portfolioDao.getPortfolio();
            runOnUiThread(() -> {
                tradableCoinAdapter.setOwnedCoins(ownedCoins);
                tradableCoinAdapter.notifyDataSetChanged();
            });
        });
    }

    private void initViews() {
        tradeRecyclerView = findViewById(R.id.trade_recyclerview);
        selectedCoinTextView = findViewById(R.id.selected_coin_textview);
        amountEditText = findViewById(R.id.amount_edittext);
        buyButton = findViewById(R.id.buy_button);
        sellButton = findViewById(R.id.sell_button);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        balanceValue = findViewById(R.id.balance_value);
        addMoneyButton = findViewById(R.id.add_money_button);
        loadingProgressBar = findViewById(R.id.loading_progressbar);
    }

    private void setupRecyclerView() {
        tradableCoinAdapter = new TradableCoinAdapter(this, new ArrayList<>(), this);
        tradeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tradeRecyclerView.setAdapter(tradableCoinAdapter);
    }

    private void setupClickListeners() {
        buyButton.setOnClickListener(v -> buyCrypto());
        sellButton.setOnClickListener(v -> sellCrypto());
        addMoneyButton.setOnClickListener(v -> showAddMoneyDialog());
    }

    private void loadPortfolioAndFetchMarketData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<PortfolioEntry> ownedCoins = portfolioDao.getPortfolio();
            runOnUiThread(() -> {
                tradableCoinAdapter.setOwnedCoins(ownedCoins);
                fetchMarketData();
            });
        });
    }

    private void fetchMarketData() {
        DataCache cache = DataCache.getInstance();
        if (cache.isCacheValid()) {
            Log.d("TradeActivity", "Učitavanje podataka iz CACHE-a.");
            tradableCoinAdapter.setCoins(cache.getCachedCoins());
            loadingProgressBar.setVisibility(View.GONE);
            tradeRecyclerView.setVisibility(View.VISIBLE);
            return;
        }

        Log.d("TradeActivity", "Cache nije validan. Dohvaćanje s API-ja.");
        if (isLoading) {
            return;
        }
        isLoading = true;
        loadingProgressBar.setVisibility(View.VISIBLE);
        tradeRecyclerView.setVisibility(View.GONE);

        ApiServiceCoinGecko apiService = RetrofitClientCoinGecko.getApi();
        marketDataCall = apiService.getMarketData("usd", "market_cap_desc", 50, 1, false);

        marketDataCall.enqueue(new Callback<List<Coin>>() {
            @Override
            public void onResponse(Call<List<Coin>> call, Response<List<Coin>> response) {
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null) {
                    DataCache.getInstance().setCachedCoins(response.body());
                    tradableCoinAdapter.setCoins(response.body());
                } else {
                    if (hasWindowFocus()) {
                        Toast.makeText(Trade.this, "Greška pri dohvaćanju valuta", Toast.LENGTH_SHORT).show();
                    }
                }
                isLoading = false;
                loadingProgressBar.setVisibility(View.GONE);
                tradeRecyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<List<Coin>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (!call.isCanceled()) {
                    if (hasWindowFocus()) {
                        Toast.makeText(Trade.this, "Greška u mrežnom pozivu", Toast.LENGTH_SHORT).show();
                    }
                }
                isLoading = false;
                loadingProgressBar.setVisibility(View.GONE);
                tradeRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onCoinClick(Coin coin) {
        selectedCoin = coin;
        selectedCoinTextView.setText(String.format("Odabrano: %s", coin.getName()));
        amountEditText.setEnabled(true);
        buyButton.setEnabled(true);
        sellButton.setEnabled(true);
    }

    private boolean isInputValid() {
        if (selectedCoin == null) {
            Toast.makeText(this, "Prvo odaberite valutu.", Toast.LENGTH_SHORT).show();
            return false;
        }
        String amountStr = amountEditText.getText().toString();
        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(this, "Unesite iznos.", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            if (Double.parseDouble(amountStr) <= 0) {
                Toast.makeText(this, "Iznos mora biti pozitivan broj.", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Neispravan format iznosa.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showAddMoneyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_money, null);
        builder.setView(dialogView);
        final EditText input = dialogView.findViewById(R.id.edit_text_amount);

        builder.setTitle("Dodaj novac")
                .setPositiveButton("Dodaj", (dialog, which) -> {
                    String amountStr = input.getText().toString();
                    if (!TextUtils.isEmpty(amountStr)) {
                        try {
                            double amount = Double.parseDouble(amountStr);
                            addMoney(amount);
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Neispravan format iznosa.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Odustani", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void loadBalance() {
        balanceValue.setText(String.format(Locale.US, "$%,.2f", getBalance()));
    }

    private void addMoney(double amount) {
        if (amount > 0) {
            saveBalance(getBalance() + amount);
        }
    }

    private double getBalance() {
        SharedPreferences prefs = getSharedPreferences(BALANCE_PREFS, Context.MODE_PRIVATE);
        return Double.longBitsToDouble(prefs.getLong(BALANCE_KEY, Double.doubleToLongBits(1000.0)));
    }

    private void saveBalance(double newBalance) {
        SharedPreferences prefs = getSharedPreferences(BALANCE_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putLong(BALANCE_KEY, Double.doubleToLongBits(newBalance)).apply();
        loadBalance();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_trade);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), Home.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_wallet) {
                startActivity(new Intent(getApplicationContext(), Wallet.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_trade) {
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