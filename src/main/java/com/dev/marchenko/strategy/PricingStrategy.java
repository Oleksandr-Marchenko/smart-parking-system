package com.dev.marchenko.strategy;

import com.dev.marchenko.domain.vehicle.VehicleType;

import java.math.BigDecimal;
import java.util.List;

public interface PricingStrategy {
    BigDecimal calculateFee(long durationMinutes);

    List<VehicleType> getSupportedVehicleTypes();
}
