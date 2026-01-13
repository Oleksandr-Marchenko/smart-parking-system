package com.dev.marchenko.service;

import com.dev.marchenko.domain.lot.Level;
import com.dev.marchenko.domain.lot.ParkingLot;
import com.dev.marchenko.domain.slot.ParkingSlot;
import com.dev.marchenko.domain.slot.SlotType;
import com.dev.marchenko.domain.ticket.ParkingTicket;
import com.dev.marchenko.domain.vehicle.Car;
import com.dev.marchenko.dto.LevelRequest;
import com.dev.marchenko.dto.ParkingLotRequest;
import com.dev.marchenko.exception.ResourceNotFoundException;
import com.dev.marchenko.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class AdminServiceIT {

    @Autowired
    private AdminService adminService;
    @Autowired
    private ParkingLotRepository lotRepository;
    @Autowired
    private LevelRepository levelRepository;
    @Autowired
    private SlotRepository slotRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private EntityManager entityManager;

    private Long lotId;
    private Long levelId;
    private Long slotId;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        slotRepository.deleteAll();
        levelRepository.deleteAll();
        lotRepository.deleteAll();
        vehicleRepository.deleteAll();

        ParkingLot lot = new ParkingLot();
        lot.setName("Admin Test Lot");
        lot = lotRepository.save(lot);
        lotId = lot.getId();

        Level level = new Level();
        level.setFloorNumber(1);
        level.setParkingLot(lot);
        level = levelRepository.save(level);
        levelId = level.getId();

        ParkingSlot slot = new ParkingSlot();
        slot.setSlotNumber("A-100");
        slot.setType(SlotType.COMPACT);
        slot.setAvailable(true);
        slot.setLevel(level);
        slot = slotRepository.save(slot);
        slotId = slot.getId();

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void createLot_ShouldSaveToDb() {
        ParkingLotRequest request = new ParkingLotRequest("New Mall Parking");

        ParkingLot created = adminService.createLot(request);

        assertNotNull(created.getId());
        assertTrue(lotRepository.existsById(created.getId()));
    }

    @Test
    void addLevel_ShouldLinkToLot() {
        LevelRequest request = new LevelRequest(2);

        Level createdLevel = adminService.addLevel(lotId, request);

        entityManager.flush();
        entityManager.clear();

        assertEquals(2, createdLevel.getFloorNumber());
        assertEquals(lotId, createdLevel.getParkingLot().getId());
    }

    @Test
    void removeLevel_ShouldThrowIfOccupied() {
        occupySlot(slotId);

        assertThrows(IllegalStateException.class, () -> adminService.removeLevel(levelId));
    }

    @Test
    void removeSlot_ShouldWorkIfEmpty() {
        adminService.removeSlot(slotId);

        entityManager.flush();
        assertFalse(slotRepository.existsById(slotId));
    }

    @Test
    void removeSlot_ShouldThrowIfOccupied() {
        occupySlot(slotId);

        assertThrows(IllegalStateException.class, () -> adminService.removeSlot(slotId));
    }

    @Test
    void updateSlotAvailability_ShouldUpdateStatus() {
        adminService.updateSlotAvailability(slotId, false);

        entityManager.flush();
        entityManager.clear();

        ParkingSlot updated = slotRepository.findById(slotId).orElseThrow();
        assertFalse(updated.isAvailable());
    }

    @Test
    void shouldThrowResourceNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> adminService.removeLot(999L));
    }

    private void occupySlot(Long sId) {
        Car car = new Car("TEST-CAR");
        vehicleRepository.save(car);

        ParkingTicket ticket = new ParkingTicket();
        ticket.setVehicle(car);
        ticket.setSlot(slotRepository.getReferenceById(sId));
        ticket.setEntryTime(LocalDateTime.now());
        ticketRepository.save(ticket);

        entityManager.flush();
        entityManager.clear();
    }
}