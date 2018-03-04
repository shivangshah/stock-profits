package com.shivang.stock.profit.dtos;

import java.util.Date;
import java.util.Objects;

public class StockDetail {

    private Date date;
    private double high;
    private double low;
    private boolean stockAvailable = true;

    public boolean isStockAvailable() {
        return stockAvailable;
    }

    public void setStockAvailable(boolean stockAvailable) {
        this.stockAvailable = stockAvailable;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    @Override
    public String toString() {
        return "{" +
                "date=" + date +
                ", high=" + high +
                ", low=" + low +
                ", stockAvailable=" + stockAvailable +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockDetail that = (StockDetail) o;
        return Double.compare(that.high, high) == 0 &&
                Double.compare(that.low, low) == 0 &&
                stockAvailable == that.stockAvailable &&
                Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {

        return Objects.hash(date, high, low, stockAvailable);
    }
}
