package com.dev.marchenko.strategy;

import com.dev.marchenko.domain.vehicle.VehicleType;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calculateFee(long durationMinutes);
    VehicleType getVehicleType();
}
