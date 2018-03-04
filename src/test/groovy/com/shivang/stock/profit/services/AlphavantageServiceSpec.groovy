package com.shivang.stock.profit.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.shivang.stock.profit.configs.AlphavantageProperties
import com.shivang.stock.profit.configs.StockCalculatorConfig
import com.shivang.stock.profit.dtos.alphavantage.ApiResponse
import com.shivang.stock.profit.exceptions.StockException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Unroll

class AlphavantageServiceSpec extends Specification {

    String alphavantageJsonResponse = "{\n" +
            "    \"Meta Data\": {\n" +
            "        \"1. Information\": \"Daily Prices (open, high, low, close) and Volumes\",\n" +
            "        \"2. Symbol\": \"MSFT\",\n" +
            "        \"3. Last Refreshed\": \"2018-03-02\",\n" +
            "        \"4. Output Size\": \"Compact\",\n" +
            "        \"5. Time Zone\": \"US/Eastern\"\n" +
            "    },\n" +
            "    \"Time Series (Daily)\": {\n" +
            "        \"2018-03-02\": {\n" +
            "            \"1. open\": \"91.5800\",\n" +
            "            \"2. high\": \"93.1500\",\n" +
            "            \"3. low\": \"90.8600\",\n" +
            "            \"4. close\": \"93.0500\",\n" +
            "            \"5. volume\": \"32830389\"\n" +
            "        },\n" +
            "        \"2018-03-01\": {\n" +
            "            \"1. open\": \"93.9900\",\n" +
            "            \"2. high\": \"94.5700\",\n" +
            "            \"3. low\": \"91.8400\",\n" +
            "            \"4. close\": \"92.8500\",\n" +
            "            \"5. volume\": \"37135561\"\n" +
            "        }\n" +
            "    }\n" +
            "}"

    @Unroll
    "#httpStatus - test alphavantage service json parsing logic"(HttpStatus httpStatus) {

        given: "A mock json response from alphavantage API"

        RestTemplate restTemplate = Mock(RestTemplate)
        ResponseEntity<String> responseEntity = Mock(ResponseEntity)
        ObjectMapper objectMapper = new StockCalculatorConfig().objectMapper()
        AlphavantageProperties properties = new AlphavantageProperties()
        properties.setApiKey("someKey")
        properties.setUrlTemplate("someTemplate")
        AlphavantageService alphavantageService = new AlphavantageService(restTemplate, objectMapper, properties)
        String symbol = "MSFT"

        when: "the json response is received by the service and parsed"

        1 * restTemplate.getForEntity(_, _, _, _) >> responseEntity
        1 * responseEntity.getBody() >> alphavantageJsonResponse
        1 * responseEntity.getStatusCode() >> httpStatus
        ApiResponse apiResponse = alphavantageService.getStockDetails(symbol)

        then: "Verify that the parsed API Response is correct"

        notThrown(StockException)
        apiResponse.getApiMetadata() != null
        apiResponse.getApiMetadata().getSymbol() == symbol
        apiResponse.getApiMetadata().getTimeZone() == "US/Eastern"
        apiResponse.getApiMetadata().getInformation() != null
        apiResponse.getApiMetadata().getOutputSize() != null
        apiResponse.getDailyTimeSeries() != null
        apiResponse.getDailyTimeSeries().size() == 2

        where:

        httpStatus << [HttpStatus.OK, HttpStatus.ACCEPTED]

    }

    def "test alphavantage service json parsing failure"() {

        given: "A mock json response with error from alphavantage API"

        RestTemplate restTemplate = Mock(RestTemplate)
        ResponseEntity<String> responseEntity = Mock(ResponseEntity)
        ObjectMapper objectMapper = new StockCalculatorConfig().objectMapper()
        AlphavantageProperties properties = new AlphavantageProperties()
        properties.setApiKey("someKey")
        properties.setUrlTemplate("someTemplate")
        AlphavantageService alphavantageService = new AlphavantageService(restTemplate, objectMapper, properties)
        String symbol = "MSFT"

        when: "the json response is received by the service and parsed as error"

        1 * restTemplate.getForEntity(_, _, _, _) >> responseEntity
        1 * responseEntity.getBody() >> "Error Message"
        1 * responseEntity.getStatusCode() >> HttpStatus.OK
        alphavantageService.getStockDetails(symbol)

        then: "Verify that the parsed API Response is correct"

        StockException e = thrown()
        e.httpStatus == HttpStatus.SERVICE_UNAVAILABLE
    }

    def "test alphavantage service apiResponse Error"() {

        given: "A mock http status of error returned by the API"

        RestTemplate restTemplate = Mock(RestTemplate)
        ResponseEntity<String> responseEntity = Mock(ResponseEntity)
        ObjectMapper objectMapper = new StockCalculatorConfig().objectMapper()
        AlphavantageProperties properties = new AlphavantageProperties()
        properties.setApiKey("someKey")
        properties.setUrlTemplate("someTemplate")
        AlphavantageService alphavantageService = new AlphavantageService(restTemplate, objectMapper, properties)
        String symbol = "MSFT"

        when: "the api returns http error status"

        1 * restTemplate.getForEntity(_, _, _, _) >> responseEntity
        1 * responseEntity.getBody() >> alphavantageJsonResponse
        1 * responseEntity.getStatusCode() >> HttpStatus.SERVICE_UNAVAILABLE
        alphavantageService.getStockDetails(symbol)

        then: "Verify that the parsed API Response is correct"

        StockException e = thrown()
        e.httpStatus == HttpStatus.SERVICE_UNAVAILABLE
    }

}