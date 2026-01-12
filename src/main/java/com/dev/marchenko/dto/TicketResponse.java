package com.dev.marchenko.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TicketResponse(
        Long ticketId,
        String licensePlate,
        String vehicleType,
        LocalDateTime entryTime,
        String slotNumber,
        Integer levelFloor
) {
}
