package com.dev.marchenko.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TicketResponse(
        Long ticketId,
        String licensePlate,
        String vehicleType,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime entryTime,
        String slotNumber,
        Integer levelFloor
) {
}
