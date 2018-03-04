package com.shivang.stock.profit.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivang.stock.profit.configs.AlphavantageProperties;
import com.shivang.stock.profit.dtos.alphavantage.ApiResponse;
import com.shivang.stock.profit.dtos.alphavantage.TimeSeries;
import com.shivang.stock.profit.exceptions.StockException;
import com.shivang.stock.profit.services.interfaces.StockHistoryAPIServiceIf;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.Date;
import java.util.TreeMap;

@Component
public class AlphavantageService implements StockHistoryAPIServiceIf {

    private final String apiKey;
    private final String urlTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AlphavantageService(RestTemplate restTemplate, ObjectMapper objectMapper, AlphavantageProperties properties) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = properties.getApiKey();
        this.urlTemplate = properties.getUrlTemplate();
    }

    public ApiResponse getStockDetails(String stockSymbol) throws Exception {

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(urlTemplate, String.class, stockSymbol, apiKey);
        String jsonBody = responseEntity.getBody();
        if (responseEntity.getStatusCode().is2xxSuccessful() && !jsonBody.contains("Error Message")) {
            ApiResponse apiResponse = objectMapper.readValue(jsonBody, ApiResponse.class);
            TreeMap<Date, TimeSeries> sorted = new TreeMap<>(Comparator.reverseOrder());
            sorted.putAll(apiResponse.getDailyTimeSeries());
            apiResponse.setDailyTimeSeries(sorted);
            return apiResponse;
        }
        throw new StockException(HttpStatus.SERVICE_UNAVAILABLE, jsonBody);
    }
}
