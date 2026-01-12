package com.dev.marchenko.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class VehicleAlreadyParkedException extends RuntimeException {
    public VehicleAlreadyParkedException(String licensePlate) {
        super("Vehicle " + licensePlate + " is already parked");
    }
}
