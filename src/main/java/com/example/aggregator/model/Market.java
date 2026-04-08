package com.example.aggregator.model;

import com.example.aggregator.exception.InvalidMarketException;

import java.util.Locale;

public record Market(String code, Locale locale) {

    public static Market of(String code) {
        if (code == null || !code.matches("[a-z]{2}-[A-Z]{2}")) {
            throw new InvalidMarketException(code);
        }
        Locale locale = Locale.forLanguageTag(code);
        if (locale.getLanguage().isBlank()) {
            throw new InvalidMarketException(code);
        }
        return new Market(code, locale);
    }

    public String language() { return locale.getLanguage(); }
    public String region()   { return locale.getCountry(); }
}
