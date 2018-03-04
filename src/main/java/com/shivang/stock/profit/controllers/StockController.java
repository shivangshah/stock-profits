package com.shivang.stock.profit.controllers;

import com.shivang.stock.profit.dtos.MaximumStockProfit;
import com.shivang.stock.profit.services.StockService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

/**
 * Stocks Controller that provide Best time to Buy and Sell Stocks depending on a given time window.
 */
@Controller
@RequestMapping("/v1/stocks")
@Api(tags = {"Stock Profit Calculator Service"})
public class StockController {

    private final StockService stockService;
    private static Logger LOGGER = LoggerFactory.getLogger(StockController.class);

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @RequestMapping(value = "/{stockSymbol}/profits", method = RequestMethod.GET)
    @ApiOperation(value = "get profit based on stock symbol", notes = "Get maximum profit for a given stock within a given time window (only highs are considered per day)")
    public ResponseEntity<MaximumStockProfit> getProfitBasedOnTimeFrame(@ApiParam(value = "Stock Symbol (required)", defaultValue = "AAPL", required = true)
                                                                        @PathVariable("stockSymbol") String stockSymbol,

                                                                        @ApiParam(value = "Date from where you want to start the time window in yyyy-mm-dd format", defaultValue = "2018-01-01")
                                                                        @RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,

                                                                        @ApiParam(value = "Date where you want to end the time window in yyyy-mm-dd format", defaultValue = "2018-03-02")
                                                                        @RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate) throws Exception {

        LOGGER.debug("getProfitBasedOnTimeFrame - stockSymbol: {}, fromDate: {}, toDate: {}", stockSymbol, fromDate, toDate);
        MaximumStockProfit maximumStockProfit = this.stockService.getMaximumStockProfit(stockSymbol, fromDate, toDate);
        LOGGER.debug("maximumStockProfit - stockSymbol: {}, profitDetails: {}", stockSymbol, maximumStockProfit);
        return ResponseEntity.ok(maximumStockProfit);
    }
}

