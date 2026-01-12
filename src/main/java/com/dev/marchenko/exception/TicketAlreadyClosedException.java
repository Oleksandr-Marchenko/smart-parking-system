package com.dev.marchenko.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class TicketAlreadyClosedException extends RuntimeException {
    public TicketAlreadyClosedException(Long ticketId) {
        super("Ticket " + ticketId + " is already closed");
    }
}
