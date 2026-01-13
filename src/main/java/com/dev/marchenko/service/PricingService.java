package com.dev.marchenko.service;

import com.dev.marchenko.domain.vehicle.VehicleType;
import com.dev.marchenko.strategy.PricingStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PricingService {

    private final Map<VehicleType, PricingStrategy> strategies;

    public PricingService(List<PricingStrategy> strategyList) {
        this.strategies = new HashMap<>();
        for (PricingStrategy strategy : strategyList) {
            for (VehicleType type : strategy.getSupportedVehicleTypes()) {
                strategies.put(type, strategy);
            }
        }
    }

    public BigDecimal calculate(VehicleType type, LocalDateTime entry, LocalDateTime exit) {
        if (exit.isBefore(entry)) {
            throw new IllegalArgumentException("Exit time must be after entry time");
        }

        long minutes = Duration.between(entry, exit).toMinutes();

        PricingStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No pricing strategy found for " + type);
        }

        return strategy.calculateFee(minutes);
    }
}
