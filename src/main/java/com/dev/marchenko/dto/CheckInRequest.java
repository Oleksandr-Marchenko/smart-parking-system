package com.dev.marchenko.dto;

import com.dev.marchenko.domain.vehicle.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckInRequest(
        @NotBlank(message = "License plate is required")
        String licensePlate,

        @NotNull(message = "Vehicle type is required")
        VehicleType vehicleType
) {}
