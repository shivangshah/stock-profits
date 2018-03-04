package com.shivang.stock.profit.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.shivang.stock.profit.configs.StockCalculatorConfig
import com.shivang.stock.profit.dtos.MaximumStockProfit
import com.shivang.stock.profit.dtos.alphavantage.ApiResponse
import com.shivang.stock.profit.models.StockModel
import com.shivang.stock.profit.services.interfaces.StockHistoryAPIServiceIf
import org.joda.time.DateTime
import spock.lang.Specification
import spock.lang.Unroll

class StockServiceSpec extends Specification {

    private static ApiResponse apiResponse
    private static final String SYMBOL = "MSFT"

    def setupSpec() {
        ObjectMapper objectMapper = new StockCalculatorConfig().objectMapper()
        ClassLoader classLoader = getClass().getClassLoader()
        File jsonFile = new File(classLoader.getResource(SYMBOL + ".json").getFile())
        apiResponse = objectMapper.readValue(jsonFile, ApiResponse.class)
    }


    @Unroll
    "#fromDate | #toDate - Testing Stock Service maximum profit algorithm based on custom inputs"(DateTime fromDate, DateTime toDate, expectedProfit, expectedBuyDate, expectedSellDate) {

        given: "Symbol and a time window for a given stock"

        StockHistoryAPIServiceIf stockHistoryAPIService = Mock(StockHistoryAPIServiceIf)
        StockModel stockModel = new StockModel()
        StockService stockService = new StockService(stockHistoryAPIService, stockModel)


        when: "stock profit is calculated within a given time window"

        stockHistoryAPIService.getStockDetails(SYMBOL) >> apiResponse
        MaximumStockProfit stockProfit = stockService.getMaximumStockProfit(SYMBOL, fromDate != null ? fromDate.toDate() : null,
                toDate != null ? toDate.toDate() : null)

        then: "verify the maximum stock profit buy/sell days based on time window"

        stockProfit != null
        stockProfit.profit == expectedProfit
        DateTime actualBuyDate = new DateTime(stockProfit.buy.date)
        DateTime actualSellDate = new DateTime(stockProfit.sell.date)
        actualBuyDate.getDayOfYear() == expectedBuyDate.getDayOfYear()
        actualSellDate.getDayOfYear() == expectedSellDate.getDayOfYear()

        where:

        fromDate                   | toDate                     | expectedProfit | expectedBuyDate            | expectedSellDate
        new DateTime("2018-02-21") | new DateTime("2018-03-02") | 3.11           | new DateTime("2017-02-22") | new DateTime("2018-02-27")
        new DateTime("2017-03-02") | new DateTime("2018-03-02") | 31.79          | new DateTime("2017-03-03") | new DateTime("2018-02-01")
        new DateTime("2017-03-02") | new DateTime("2018-03-12") | 22.26          | new DateTime("2017-09-26") | new DateTime("2018-02-01")
        new DateTime("1970-03-02") | new DateTime("2018-03-02") | 0.0            | new DateTime("2000-01-03") | new DateTime("2000-01-03")
        null                       | new DateTime("2018-03-12") | 22.26          | new DateTime("2017-09-26") | new DateTime("2018-02-01")
        new DateTime("2017-03-02") | null                       | 22.26          | new DateTime("2017-09-26") | new DateTime("2018-02-01")
        new DateTime("2018-03-02") | new DateTime("2017-03-12") | 22.26          | new DateTime("2017-09-26") | new DateTime("2018-02-01")
        new DateTime("2019-03-02") | new DateTime("2017-03-12") | 22.26          | new DateTime("2017-09-26") | new DateTime("2018-02-01")
    }
}