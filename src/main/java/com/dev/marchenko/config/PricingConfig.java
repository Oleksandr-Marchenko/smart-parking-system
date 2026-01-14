package com.dev.marchenko.config;

import com.dev.marchenko.domain.vehicle.VehicleType;
import com.dev.marchenko.strategy.PricingStrategy;
import com.dev.marchenko.strategy.UniversalHourlyStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Configuration
public class PricingConfig {

    @Bean
    public NumberFormat currencyFormatter(@Value("${parking.locale:en-US}") String localeTag) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag(localeTag));
    }

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
