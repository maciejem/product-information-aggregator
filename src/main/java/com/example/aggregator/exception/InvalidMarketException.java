package com.example.aggregator.exception;

public class InvalidMarketException extends RuntimeException {
    public InvalidMarketException(String code) {
        super("Invalid market code: " + code + ". Expected format: language-REGION (e.g. nl-NL, de-DE)");
    }
}
