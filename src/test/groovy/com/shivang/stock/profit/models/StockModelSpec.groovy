package com.shivang.stock.profit.models

import com.shivang.stock.profit.dtos.StockDetail
import org.joda.time.DateTime
import spock.lang.Specification


class StockModelSpec extends Specification {

    def "Test caching for Stock Details"() {

        given: "A list of stock details and last refresh date for a particular symbol"

        StockModel stockModel = new StockModel()
        String symbol = "AAPL"
        Date expectedLastRefresh = new Date()
        DateTime current = new DateTime()
        List<StockDetail> expectedList = new ArrayList<>()
        for (int i = 0; i < 100; i++) {
            StockDetail stockDetail = new StockDetail()
            stockDetail.setDate(current.minusDays(i).toDate())
            stockDetail.setHigh(i * 10)
            stockDetail.setLow(i * 2)
            stockDetail.setStockAvailable(true)
            expectedList.add(stockDetail)
        }

        when: "cache is updated"

        stockModel.updateCache(symbol, expectedLastRefresh, expectedList)
        List<StockDetail> actualList = stockModel.getCachedStockDetails(symbol)
        Date actualLastRefresh = stockModel.getStockLastRefreshDate(symbol)

        then: "verify that the cache got updated correctly and the data is valid"

        !actualList.empty
        actualList == expectedList
        actualLastRefresh == expectedLastRefresh

    }
}