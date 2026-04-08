package com.example.aggregator.controller;

import com.example.aggregator.exception.CatalogServiceException;
import com.example.aggregator.exception.InvalidMarketException;
import com.example.aggregator.exception.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidMarketException.class)
    public ProblemDetail handleInvalidMarket(InvalidMarketException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid Market Code");
        return problem;
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleProductNotFound(ProductNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Product Not Found");
        return problem;
    }

    @ExceptionHandler(CatalogServiceException.class)
    public ProblemDetail handleCatalogServiceFailure(CatalogServiceException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Product information is temporarily unavailable. Please try again shortly."
        );
        problem.setTitle("Service Unavailable");
        return problem;
    }
}
