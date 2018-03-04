package com.shivang.stock.profit.controllers;

import com.shivang.stock.profit.exceptions.StockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionControllerAdvice.class);

    @ExceptionHandler(StockException.class)
    public ResponseEntity handleCustomException(StockException ex, WebRequest request) {
        return generateErrorResponse(request, ex.getHttpStatus(), ex);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity handleHystrixException(RuntimeException ex, WebRequest request) {

        Throwable t = ex.getCause();
        if (t instanceof StockException) {
            StockException appException = (StockException) t;
            return handleCustomException(appException, request);
        }
        return generateErrorResponse(request, HttpStatus.SERVICE_UNAVAILABLE, ex);
    }

    private ResponseEntity generateErrorResponse(WebRequest request, HttpStatus status, Exception ex) {
        LOGGER.error("Error occurred", ex);
        request.setAttribute("javax.servlet.error.status_code", status.value(), RequestAttributes.SCOPE_REQUEST);
        request.setAttribute("javax.servlet.error.exception", ex, RequestAttributes.SCOPE_REQUEST);
        ErrorAttributes errorAttributes = new DefaultErrorAttributes();
        Map<String, Object> body = errorAttributes.getErrorAttributes(request, false);
        return ResponseEntity.status(status).body(body);
    }

}
