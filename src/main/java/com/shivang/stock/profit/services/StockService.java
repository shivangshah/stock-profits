package com.shivang.stock.profit.services;

import com.shivang.stock.profit.dtos.MaximumStockProfit;
import com.shivang.stock.profit.dtos.StockDetail;
import com.shivang.stock.profit.dtos.alphavantage.ApiResponse;
import com.shivang.stock.profit.dtos.alphavantage.TimeSeries;
import com.shivang.stock.profit.models.StockModel;
import com.shivang.stock.profit.services.interfaces.StockHistoryAPIServiceIf;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class StockService {

    private static final int DEFAULT_DAYS_WINDOW_FROM_CURRENT = 180;
    private final StockHistoryAPIServiceIf stockHistoryApiService;
    private final StockModel stockModel;
    private static Logger LOGGER = LoggerFactory.getLogger(StockService.class);

    public StockService(StockHistoryAPIServiceIf stockHistoryApiService, StockModel stockModel) {
        this.stockHistoryApiService = stockHistoryApiService;
        this.stockModel = stockModel;
    }

    public MaximumStockProfit getMaximumStockProfit(String stockSymbol, Date fromDate, Date toDate) throws Exception {

        // First get a list of all the stock details across the full time window
        // If it's cached .. great ! If not, make API call to Alphavantage and download all the data and also put it in cache
        List<StockDetail> stockDetails = this.stockModel.getCachedStockDetails(stockSymbol);
        if (stockDetails.isEmpty()) {
            LOGGER.info("Stock details for symbol {} not found in cache", stockSymbol);
            stockDetails = getStockDetailsAndUpdateCache(stockSymbol);
        }

        // From the data retrieved (from cache or from alphavantage) get the last date when the price for the stock was refreshed
        // This will be important specifically to verify the incoming dates
        Date lastRefreshDate = this.stockModel.getStockLastRefreshDate(stockSymbol);

        // Verify all different conditions of dates provided by the user fall in the right time window
        // 1) check for nulls
        // 2) make sure the dates are BEFORE the last refresh date (otherwise we fall off the time window)
        // 3) make sure the 'from' is before 'to'
        // If any of the above conditions are not true, default to lastRefreshDate as the 'to' and 'from' date to be 180 days before
        // last refresh date
        if (fromDate == null || toDate == null ||
                toDate.compareTo(lastRefreshDate) > 0 ||
                fromDate.compareTo(lastRefreshDate) > 0 ||
                fromDate.compareTo(toDate) > 0) {
            LOGGER.info("Provided time window does not match the standards, defaulting to {} days from current day", DEFAULT_DAYS_WINDOW_FROM_CURRENT);
            toDate = lastRefreshDate;
            fromDate = new DateTime(lastRefreshDate).minusDays(DEFAULT_DAYS_WINDOW_FROM_CURRENT).toDate();
        }
        // Get exact time window stock details to work on (don't need to go through the full list)
        List<StockDetail> stockDetailsInGivenTimeWindow = getStockDetailsInGivenTimeWindow(fromDate, toDate, lastRefreshDate, stockDetails);
        // Calculate maximum profit
        return getMaximumStockProfitFromList(stockDetailsInGivenTimeWindow);
    }

    public List<StockDetail> getStockDetailsAndUpdateCache(String stockSymbol) throws Exception {
        ApiResponse apiResponse = this.stockHistoryApiService.getStockDetails(stockSymbol);
        List<StockDetail> stockDetails = convertResponseToStockDetailsList(apiResponse);
        this.stockModel.updateCache(stockSymbol, apiResponse.getApiMetadata().getLastRefreshed(), stockDetails);
        return stockDetails;
    }

    public List<StockDetail> convertResponseToStockDetailsList(ApiResponse input) {

        List<StockDetail> stockDetails = new ArrayList<>();
        DateTime current = new DateTime(input.getApiMetadata().getLastRefreshed());
        for (Map.Entry<Date, TimeSeries> entry : input.getDailyTimeSeries().entrySet()) {

            DateTime stockDate = new DateTime(entry.getKey());
            while (current.dayOfWeek().get() != stockDate.dayOfWeek().get()) {
                StockDetail unavailableStockDay = new StockDetail();
                unavailableStockDay.setDate(current.toDate());
                unavailableStockDay.setStockAvailable(false);
                stockDetails.add(unavailableStockDay);
                current = current.minusDays(1);
            }
            StockDetail stockDetail = new StockDetail();
            stockDetail.setDate(entry.getKey());
            stockDetail.setHigh(entry.getValue().getHigh());
            stockDetail.setLow(entry.getValue().getLow());
            stockDetails.add(stockDetail);
            current = current.minusDays(1);
        }
        // We want to reverse sort by date so we always have a sorted list of data to work with (easy for sliding windows of time)
        stockDetails.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));
        return stockDetails;
    }

    public List<StockDetail> getStockDetailsInGivenTimeWindow(Date fromDate, Date toDate, Date lastRefresh, List<StockDetail> stockDetails) {
        int fromIndex = Math.abs(Days.daysBetween(new DateTime(lastRefresh), new DateTime(toDate)).getDays());
        int toIndex = Math.abs(Days.daysBetween(new DateTime(lastRefresh), new DateTime(fromDate)).getDays()) + 1;
        // if fromDate is beyond what we have in history, we take the last in history
        if (toIndex > stockDetails.size()) {
            toIndex = stockDetails.size();
        }
        // because we are sorted in descending order the "from" and "to" kind of switch places
        return stockDetails.subList(fromIndex, toIndex);
    }

    public MaximumStockProfit getMaximumStockProfitFromList(List<StockDetail> stockDetails) {
        double bestBuy = Double.MAX_VALUE;
        double bestSell = Double.MIN_VALUE;

        StockDetail bestBoughtStockDetail = null;
        StockDetail bestSoldStockDetail = null;
        for (StockDetail stockDetail : stockDetails) {
            if (!stockDetail.isStockAvailable()) {
                continue;
            }
            if (stockDetail.getHigh() > bestSell) {
                bestSell = stockDetail.getHigh();
                // This here is extremely important because you don't want the best buy to be AFTER the best sell
                // So you reset the bestBuy and start from this point onwards
                bestBuy = Double.MAX_VALUE;
                bestSoldStockDetail = stockDetail;
            }
            if (stockDetail.getHigh() < bestBuy) {
                bestBuy = stockDetail.getHigh();
                bestBoughtStockDetail = stockDetail;
            }
        }
        MaximumStockProfit maximumStockProfit = new MaximumStockProfit();
        maximumStockProfit.setBuy(bestBoughtStockDetail);
        maximumStockProfit.setSell(bestSoldStockDetail);
        maximumStockProfit.setProfit(bestSell - bestBuy);
        // Round it to 2 decimals. If accuracy is important, this can be changed
        DecimalFormat df = new DecimalFormat("#.##");
        maximumStockProfit.setProfit(Double.valueOf(df.format(maximumStockProfit.getProfit())));
        return maximumStockProfit;
    }
}
