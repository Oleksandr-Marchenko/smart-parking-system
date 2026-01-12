package com.dev.marchenko.controller;

import com.dev.marchenko.dto.CheckInRequest;
import com.dev.marchenko.dto.CheckOutResponse;
import com.dev.marchenko.dto.TicketResponse;
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

    @PostMapping("/check-in")
    public ResponseEntity<TicketResponse> checkIn(@Valid @RequestBody CheckInRequest request) {
        return ResponseEntity.ok(parkingService.checkIn(request.licensePlate(), request.vehicleType()));
    }

    @PostMapping("/check-out/{ticketId}")
    public ResponseEntity<CheckOutResponse> checkOut(@PathVariable Long ticketId) {
        return ResponseEntity.ok(parkingService.checkOut(ticketId));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<TicketResponse>> getActiveSessions() {
        return ResponseEntity.ok(parkingService.getActiveSessions());
    }

}
