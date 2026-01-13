package com.dev.marchenko.config;

import com.dev.marchenko.domain.vehicle.VehicleType;
import com.dev.marchenko.strategy.PricingStrategy;
import com.dev.marchenko.strategy.UniversalHourlyStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class PricingConfig {

    @Bean
    public PricingStrategy motorcycleStrategy() {
        return new UniversalHourlyStrategy(BigDecimal.valueOf(1), List.of(VehicleType.MOTORCYCLE));
    }

    @Bean
    public PricingStrategy carStrategy() {
        return new UniversalHourlyStrategy(BigDecimal.valueOf(2), List.of(VehicleType.CAR));
    }

    @Bean
    public PricingStrategy truckStrategy() {
        return new UniversalHourlyStrategy(BigDecimal.valueOf(3), List.of(VehicleType.TRUCK));
    }
}
