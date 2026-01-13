package com.dev.marchenko.service;

import com.dev.marchenko.domain.vehicle.VehicleType;
import com.dev.marchenko.strategy.PricingStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PricingService {

    private final Map<VehicleType, PricingStrategy> strategies;

    public PricingService(List<PricingStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(PricingStrategy::getVehicleType, s -> s));
    }

    public BigDecimal calculate(VehicleType type, LocalDateTime entry, LocalDateTime exit) {
        if (exit.isBefore(entry)) {
            throw new IllegalArgumentException("Exit time must be after entry time");
        }

        long minutes = Duration.between(entry, exit).toMinutes();

        if (minutes == 0 && !entry.equals(exit)) {
            minutes = 1;
        }

        if (!strategies.containsKey(type)) {
            throw new IllegalArgumentException("No pricing strategy found for " + type);
        }

        return strategies.get(type).calculateFee(minutes);
    }
}
