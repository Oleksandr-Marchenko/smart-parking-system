package com.dev.marchenko.controller;

import com.dev.marchenko.dto.CheckInRequest;
import com.dev.marchenko.dto.CheckOutResponse;
import com.dev.marchenko.dto.TicketResponse;
import com.dev.marchenko.mapper.ParkingMapper;
import com.dev.marchenko.service.ParkingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;
    private final ParkingMapper mapper;

    @PostMapping("/check-in")
    public ResponseEntity<TicketResponse> checkIn(@Valid @RequestBody CheckInRequest request) {
        var ticket = parkingService.checkIn(request.licensePlate(), request.vehicleType(), request.isHandicapped());
        return ResponseEntity.ok(mapper.toTicketResponse(ticket));
    }

    @PostMapping("/check-out/{ticketId}")
    public ResponseEntity<CheckOutResponse> checkOut(@PathVariable Long ticketId) {
        var ticket = parkingService.checkOut(ticketId);
        return ResponseEntity.ok(mapper.toCheckOutResponse(ticket));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<TicketResponse>> getActiveSessions() {
        var tickets = parkingService.getActiveSessions();
        return ResponseEntity.ok(tickets.stream()
                .map(mapper::toTicketResponse)
                .toList());
    }

}
