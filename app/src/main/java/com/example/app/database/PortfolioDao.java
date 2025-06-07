package com.example.app.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PortfolioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PortfolioEntry entry);

    @Update
    void update(PortfolioEntry entry);

    @Delete
    void delete(PortfolioEntry entry);

    @Query("SELECT * FROM portfolio_table ORDER BY name ASC")
    List<PortfolioEntry> getPortfolio();

    @Query("SELECT * FROM portfolio_table WHERE coinId = :id")
    PortfolioEntry getSingleCoin(String id);
}