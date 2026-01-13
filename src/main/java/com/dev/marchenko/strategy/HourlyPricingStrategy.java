package com.dev.marchenko.strategy;

import java.math.BigDecimal;

public abstract class HourlyPricingStrategy implements PricingStrategy {

    protected abstract BigDecimal getRate();

    @Override
    public BigDecimal calculateFee(long minutes) {
        long billableHours = (minutes + 59) / 60;
        if (billableHours == 0) {
            billableHours = 1;
        }
        return getRate().multiply(BigDecimal.valueOf(billableHours));
    }
}
