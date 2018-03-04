package com.shivang.stock.profit.services.interfaces;

import com.shivang.stock.profit.dtos.alphavantage.ApiResponse;

public interface StockHistoryAPIServiceIf {

    ApiResponse getStockDetails(String stockSymbol) throws Exception;
}
