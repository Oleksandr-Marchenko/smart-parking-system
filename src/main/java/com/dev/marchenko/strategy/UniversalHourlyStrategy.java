package com.dev.marchenko.strategy;

import com.dev.marchenko.domain.vehicle.VehicleType;

import java.math.BigDecimal;
import java.util.List;

public class UniversalHourlyStrategy implements PricingStrategy {
    private final BigDecimal rate;
    private final List<VehicleType> supportedTypes;

    public UniversalHourlyStrategy(BigDecimal rate, List<VehicleType> supportedTypes) {
        this.rate = rate;
        this.supportedTypes = supportedTypes;
    }

    @Override
    public BigDecimal calculateFee(long minutes) {
        long billableHours = (minutes + 59) / 60;
        if (billableHours == 0) {
            billableHours = 1;
        }
        return rate.multiply(BigDecimal.valueOf(billableHours));
    }

    @Override
    public List<VehicleType> getSupportedVehicleTypes() {
        return supportedTypes;
    }
}
