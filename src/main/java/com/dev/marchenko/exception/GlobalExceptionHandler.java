package com.dev.marchenko.exception;


import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleBadRequest(IllegalStateException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<Object> handleTicketNotFound(TicketNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(DataIntegrityViolationException ex) {

        String constraint = extractConstraintName(ex);

        String message = "Database constraint violation";

        if (constraint != null) {
            String upper = constraint.toUpperCase();
            if (upper.contains("UK_PARKING_LOT_NAME")) {
                message = "Parking lot with this name already exists";
            } else if (upper.contains("UK_LEVEL_PARKING_LOT_FLOOR")) {
                message = "This parking lot already has a level with this floor number";
            } else if (upper.contains("UK_SLOT_LEVEL_NUMBER")) {
                message = "This level already has a slot with this number";
            }
        }

        return buildResponse(HttpStatus.CONFLICT, message);
    }

    private String extractConstraintName(Throwable ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause.getMessage() != null) {
                String msg = cause.getMessage().toUpperCase();

                int idx = msg.indexOf("CONSTRAINT");
                if (idx != -1) {
                    String substring = msg.substring(idx);
                    return substring.replaceAll("[^A-Z0-9_]", "");
                }

                if (msg.contains("UK_")) {
                    int start = msg.indexOf("UK_");
                    int end = msg.indexOf(" ", start);
                    if (end == -1) end = msg.length();
                    return msg.substring(start, end).replaceAll("[^A-Z0-9_]", "");
                }
            }
            cause = cause.getCause();
        }
        return null;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        String message = ex.getMessage();

        if (message != null && message.contains("already registered as a different vehicle type")) {
            message = "This license plate is already registered with a different vehicle type";
        }

        return buildResponse(HttpStatus.CONFLICT, message);
    }

    @ExceptionHandler(NoAvailableSlotException.class)
    public ResponseEntity<Object> handleNoAvailableSlot(NoAvailableSlotException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(VehicleAlreadyParkedException.class)
    public ResponseEntity<Object> handleVehicleAlreadyParked(VehicleAlreadyParkedException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(LicensePlateAlreadyRegisteredException.class)
    public ResponseEntity<Object> handleLicensePlateAlreadyRegistered(LicensePlateAlreadyRegisteredException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(TicketAlreadyClosedException.class)
    public ResponseEntity<Object> handleTicketAlreadyClosed(TicketAlreadyClosedException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return buildResponse(HttpStatus.BAD_REQUEST, errorMessage);
    }
}
