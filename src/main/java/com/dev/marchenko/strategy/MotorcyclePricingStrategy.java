package com.dev.marchenko.strategy;

import com.dev.marchenko.domain.vehicle.VehicleType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MotorcyclePricingStrategy implements PricingStrategy {
    @Override
    public BigDecimal calculateFee(long minutes) {
        double hours = Math.ceil(minutes / 60.0);
        return BigDecimal.valueOf(hours);
    }

    @Override
    public VehicleType getVehicleType() {
        return VehicleType.MOTORCYCLE;
    }
}
