package com.dev.marchenko.formatter;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;

@Component
public class MoneyFormatter {

    private final NumberFormat currencyFormatter;

    public MoneyFormatter(NumberFormat currencyFormatter) {
        this.currencyFormatter = currencyFormatter;
    }

    public String map(BigDecimal value) {
        return value == null ? null : currencyFormatter.format(value);
    }
}
