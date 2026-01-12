package com.dev.marchenko.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class LicensePlateAlreadyRegisteredException extends RuntimeException {
    public LicensePlateAlreadyRegisteredException(String licensePlate) {
        super("License plate " + licensePlate + " is already registered as a different vehicle type");
    }
}
