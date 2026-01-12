package com.dev.marchenko.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class NoAvailableSlotException extends RuntimeException {
    public NoAvailableSlotException(String vehicleType) {
        super("No available slots for " + vehicleType);
    }
}
