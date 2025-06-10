package com.example.app.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "portfolio_table")
public class PortfolioEntry {

    @PrimaryKey
    @NonNull
    private String coinId;

    private String name;
    private String symbol;
    private double amount;
    public PortfolioEntry(@NonNull String coinId, String name, String symbol, double amount) {
        this.coinId = coinId;
        this.name = name;
        this.symbol = symbol;
        this.amount = amount;
    }

    @NonNull
    public String getCoinId() { return coinId; }
    public String getName() { return name; }
    public String getSymbol() { return symbol; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}