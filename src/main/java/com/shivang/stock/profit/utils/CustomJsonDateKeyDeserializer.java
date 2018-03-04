package com.shivang.stock.profit.utils;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class CustomJsonDateKeyDeserializer extends KeyDeserializer {
    @Override
    public Object deserializeKey(String date, DeserializationContext deserializationContext) throws IOException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
        format.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
        date = date + " 17:00:00.000-0500";
        try {
            return format.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
