package com.shivang.stock.profit

import com.fasterxml.jackson.databind.ObjectMapper
import com.shivang.stock.profit.configs.StockCalculatorConfig
import com.shivang.stock.profit.dtos.MaximumStockProfit
import com.shivang.stock.profit.dtos.alphavantage.ApiResponse
import com.shivang.stock.profit.services.AlphavantageService
import com.shivang.stock.profit.services.interfaces.StockHistoryAPIServiceIf
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Unroll
import spock.mock.DetachedMockFactory

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class StockProfitCalculatorIntegrationSpec extends Specification {

    @Autowired
    RestTemplate restTemplate

    @LocalServerPort
    int port

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    StockHistoryAPIServiceIf stockHistoryAPIService

    private static ApiResponse apiResponse
    private static final String SYMBOL = "MSFT"

    def setupSpec() {
        ClassLoader classLoader = getClass().getClassLoader()
        File jsonFile = new File(classLoader.getResource(SYMBOL + ".json").getFile())
        apiResponse = new StockCalculatorConfig().objectMapper().readValue(jsonFile, ApiResponse.class)
    }

    @Unroll
    "#fromDate | #toDate - Testing Stock Service maximum profit algorithm based on custom inputs"(String fromDate, String toDate, expectedProfit, expectedBuyDate, expectedSellDate) {

        given: "Symbol and a time window for a given stock"

        String baseUrl = "http://localhost:" + port + "/v1/stocks/{symbol}/profits"
        String queryParams = ""
        if (toDate != null) {
            queryParams = queryParams + "?to={toDate}"
        }
        if (fromDate != null) {
            queryParams = toDate != null ? queryParams + "&from={fromDate}" : queryParams + "?from={fromDate}"
        }
        if (!queryParams.isEmpty()) {
            baseUrl = baseUrl + queryParams
        }

        when: "stock profit is calculated within a given time window"

        stockHistoryAPIService.getStockDetails(SYMBOL) >> apiResponse
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(baseUrl, String.class, "MSFT", toDate, fromDate)

        then: "verify the maximum stock profit buy/sell days based on time window"

        responseEntity.getStatusCode() == HttpStatus.OK
        String jsonBody = responseEntity.getBody()
        MaximumStockProfit stockProfit = objectMapper.readValue(jsonBody, MaximumStockProfit)
        stockProfit != null
        stockProfit.profit == expectedProfit
        DateTime actualBuyDate = new DateTime(stockProfit.buy.date)
        DateTime actualSellDate = new DateTime(stockProfit.sell.date)
        actualBuyDate.getDayOfYear() == expectedBuyDate.getDayOfYear()
        actualSellDate.getDayOfYear() == expectedSellDate.getDayOfYear()

        where:

        fromDate     | toDate       | expectedProfit | expectedBuyDate            | expectedSellDate
        // all parameters valid
        "2017-03-02" | "2018-03-02" | 31.79          | new DateTime("2017-03-03") | new DateTime("2018-02-01")
        // toDate is greater than lastRefresh date (03/02/2018) so going to default to lastRefresh-180
        "2017-03-02" | "2018-03-12" | 22.26          | new DateTime("2017-09-26") | new DateTime("2018-02-01")
        // The very first price fo MSFT on 01/03/2000 was the best
        "1970-03-02" | "2018-03-02" | 0.0            | new DateTime("2000-01-03") | new DateTime("2000-01-03")
        null         | "2018-03-12" | 22.26          | new DateTime("2017-09-26") | new DateTime("2018-02-01")
        "2017-03-02" | null         | 22.26          | new DateTime("2017-09-26") | new DateTime("2018-02-01")
        "2018-03-02" | "2017-03-12" | 22.26          | new DateTime("2017-09-26") | new DateTime("2018-02-01")
        "2019-03-02" | "2017-03-12" | 22.26          | new DateTime("2017-09-26") | new DateTime("2018-02-01")
    }

    @TestConfiguration
    static class Config {
        private DetachedMockFactory factory = new DetachedMockFactory()

        @Bean
        AlphavantageService alphavantageService() {
            return factory.Stub(AlphavantageService)
        }
    }
}