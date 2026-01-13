package com.dev.marchenko.strategy;

import com.dev.marchenko.domain.vehicle.VehicleType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TruckPricingStrategy extends HourlyPricingStrategy {
    @Override
    protected BigDecimal getRate() {
        return BigDecimal.valueOf(3);
    }

    @Override
    public VehicleType getVehicleType() {
        return VehicleType.TRUCK;
    }
}
