package com.dev.marchenko.dto;

import com.dev.marchenko.domain.vehicle.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CheckInRequest(
        @NotBlank(message = "License plate is required")
        @Size(max = 20, message = "License plate must not exceed 20 characters")
        String licensePlate,

        @NotNull(message = "Vehicle type is required")
        VehicleType vehicleType,

        boolean isHandicapped
) {}
