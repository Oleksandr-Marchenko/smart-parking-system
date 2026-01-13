package com.dev.marchenko.strategy;

import com.dev.marchenko.domain.vehicle.VehicleType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CarPricingStrategy extends HourlyPricingStrategy {
    @Override
    protected BigDecimal getRate() {
        return BigDecimal.valueOf(2);
    }

    @Override
    public VehicleType getVehicleType() {
        return VehicleType.CAR;
    }
}
