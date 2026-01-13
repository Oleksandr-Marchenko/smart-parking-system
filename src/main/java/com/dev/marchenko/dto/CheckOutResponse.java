package com.dev.marchenko.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record CheckOutResponse(
        String licensePlate,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime entryTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime exitTime,
        long durationMinutes,
        BigDecimal totalFee
) {}
