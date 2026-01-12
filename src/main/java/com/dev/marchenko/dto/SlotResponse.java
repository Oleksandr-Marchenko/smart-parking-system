package com.dev.marchenko.dto;

public record SlotResponse(
        Long id,
        String slotNumber,
        String type,
        boolean isOccupied,
        boolean isAvailable
) {}
