package com.example.app;

import com.example.app.api.Coin;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DataCache {

    private static DataCache instance;
    private List<Coin> cachedCoins;
    private long lastFetchTimeMillis = 0;

    private static final long CACHE_DURATION_MS = TimeUnit.MINUTES.toMillis(2);

    private DataCache() {}

    public static synchronized DataCache getInstance() {
        if (instance == null) {
            instance = new DataCache();
        }
        return instance;
    }

    public List<Coin> getCachedCoins() {
        return cachedCoins;
    }

    public void setCachedCoins(List<Coin> coins) {
        this.cachedCoins = coins;
        this.lastFetchTimeMillis = System.currentTimeMillis();
    }

    public boolean isCacheValid() {
        if (cachedCoins == null || cachedCoins.isEmpty()) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastFetchTimeMillis) < CACHE_DURATION_MS;
    }
}