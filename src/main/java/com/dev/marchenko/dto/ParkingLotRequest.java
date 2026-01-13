package com.dev.marchenko.dto;

import jakarta.validation.constraints.NotBlank;

public record ParkingLotRequest(
        @NotBlank(message = "Name cannot be empty")
        String name
) {}
