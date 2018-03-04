package com.shivang.stock.profit.exceptions;

import org.springframework.http.HttpStatus;

public class StockException extends RuntimeException {
    private final HttpStatus httpStatus;

    public StockException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
