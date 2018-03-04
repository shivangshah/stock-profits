package com.shivang.stock.profit.dtos;

public class MaximumStockProfit {

    private StockDetail buy;
    private StockDetail sell;
    private double profit;

    public StockDetail getBuy() {
        return buy;
    }

    public void setBuy(StockDetail buy) {
        this.buy = buy;
    }

    public StockDetail getSell() {
        return sell;
    }

    public void setSell(StockDetail sell) {
        this.sell = sell;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    @Override
    public String toString() {
        return "{" +
                "buy=" + buy +
                ", sell=" + sell +
                ", profit=" + profit +
                '}';
    }
}
