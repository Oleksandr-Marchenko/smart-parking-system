package com.dev.marchenko.service;

import com.dev.marchenko.domain.vehicle.VehicleType;
import com.dev.marchenko.strategy.PricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PricingServiceTest {

    @Mock
    private PricingStrategy strategy;
    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        when(strategy.getSupportedVehicleTypes()).thenReturn(List.of(VehicleType.CAR));
        pricingService = new PricingService(List.of(strategy));
    }

    @Test
    void calculate_ShouldCallStrategy() {
        LocalDateTime entry = LocalDateTime.now().minusHours(2);
        LocalDateTime exit = LocalDateTime.now();
        when(strategy.calculateFee(anyLong())).thenReturn(new BigDecimal("20.00"));

        BigDecimal fee = pricingService.calculate(VehicleType.CAR, entry, exit);

        assertEquals(new BigDecimal("20.00"), fee);
        verify(strategy).calculateFee(anyLong());
    }

    @Test
    void calculate_ShouldThrowException_WhenStrategyNotFound() {
        LocalDateTime entry = LocalDateTime.now();
        LocalDateTime exit = LocalDateTime.now().plusHours(1);

        assertThrows(IllegalArgumentException.class, () ->
                pricingService.calculate(VehicleType.MOTORCYCLE, entry, exit)
        );
    }

    @Test
    void calculate_ShouldPassCorrectMinutesToStrategy() {
        LocalDateTime entry = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2024, 1, 1, 11, 30);

        pricingService.calculate(VehicleType.CAR, entry, exit);

        verify(strategy).calculateFee(90L);
    }

    @Test
    void calculate_ShouldThrowException_WhenExitBeforeEntry() {
        LocalDateTime entry = LocalDateTime.now();
        LocalDateTime exit = entry.minusHours(1);

        assertThrows(Exception.class, () ->
                pricingService.calculate(VehicleType.CAR, entry, exit)
        );
    }
}