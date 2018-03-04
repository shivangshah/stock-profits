package com.shivang.stock.profit.dtos.alphavantage;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.shivang.stock.profit.utils.CustomJsonDateKeyDeserializer;

import java.util.Date;
import java.util.Map;

public class ApiResponse {

    @JsonProperty("Meta Data")
    private Metadata apiMetadata;

    @JsonProperty("Time Series (Daily)")
    @JsonDeserialize(keyUsing = CustomJsonDateKeyDeserializer.class)
    private Map<Date, TimeSeries> dailyTimeSeries;

    public Metadata getApiMetadata() {
        return apiMetadata;
    }

    public void setApiMetadata(Metadata apiMetadata) {
        this.apiMetadata = apiMetadata;
    }

    public Map<Date, TimeSeries> getDailyTimeSeries() {
        return dailyTimeSeries;
    }

    public void setDailyTimeSeries(Map<Date, TimeSeries> dailyTimeSeries) {
        this.dailyTimeSeries = dailyTimeSeries;
    }
}
