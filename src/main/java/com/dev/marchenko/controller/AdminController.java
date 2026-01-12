package com.dev.marchenko.controller;

import com.dev.marchenko.dto.*;
import com.dev.marchenko.mapper.ParkingMapper;
import com.dev.marchenko.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ParkingMapper mapper;

    @PostMapping("/lots")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ParkingLotResponse> createLot(@Valid @RequestBody ParkingLotRequest request) {
        var lot = adminService.createLot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toLotResponse(lot));
    }

    @DeleteMapping("/lots/{id}")
    public ResponseEntity<Void> removeLot(@PathVariable Long id) {
        adminService.removeLot(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/lots/{lotId}/levels")
    public ResponseEntity<LevelResponse> addLevel(@PathVariable Long lotId, @RequestBody LevelRequest request) {
        var level = adminService.addLevel(lotId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toLevelResponse(level));
    }

    @DeleteMapping("/levels/{levelId}")
    public ResponseEntity<Void> removeLevel(@PathVariable Long levelId) {
        adminService.removeLevel(levelId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/levels/{levelId}/slots")
    public ResponseEntity<SlotResponse> addSlot(@PathVariable Long levelId, @RequestBody SlotRequest request) {
        var slot = adminService.addSlot(levelId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toSlotResponse(slot));
    }

    @DeleteMapping("/slots/{slotId}")
    public ResponseEntity<Void> removeSlot(@PathVariable Long slotId) {
        adminService.removeSlot(slotId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/slots/{slotId}/availability")
    public ResponseEntity<SlotResponse> toggleSlot(@PathVariable Long slotId, @RequestParam boolean available) {
        var updatedSlot = adminService.updateSlotAvailability(slotId, available);
        return ResponseEntity.ok(mapper.toSlotResponse(updatedSlot));
    }
}
