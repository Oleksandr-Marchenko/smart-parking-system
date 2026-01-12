package com.dev.marchenko.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CheckOutResponse(
        String licensePlate,
        LocalDateTime entryTime,
        LocalDateTime exitTime,
        long durationMinutes,
        BigDecimal totalFee
) {}
