package com.shivang.stock.profit.models;

import com.shivang.stock.profit.dtos.StockDetail;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StockModel {

    // in memory cache for Stocks so as to not reach out to the API everytime for the same static data set.
    private final ConcurrentHashMap<String, List<StockDetail>> STOCK_CACHE_FOR_PAST_20_YEARS = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Date> STOCK_LAST_REFRESH_DATES = new ConcurrentHashMap<>();

    public StockModel() {
    }

    public List<StockDetail> getCachedStockDetails(String stockSymbol) {
        return STOCK_CACHE_FOR_PAST_20_YEARS.keySet().contains(stockSymbol) ? STOCK_CACHE_FOR_PAST_20_YEARS.get(stockSymbol) : new ArrayList<>();
    }

    public Date getStockLastRefreshDate(String stockSymbol) {
        return STOCK_LAST_REFRESH_DATES.get(stockSymbol);
    }

    public synchronized void updateCache(String symbol, Date lastRefreshDate, List<StockDetail> stockDetails) {
        STOCK_CACHE_FOR_PAST_20_YEARS.put(symbol, stockDetails);
        STOCK_LAST_REFRESH_DATES.put(symbol, lastRefreshDate);
    }
}
