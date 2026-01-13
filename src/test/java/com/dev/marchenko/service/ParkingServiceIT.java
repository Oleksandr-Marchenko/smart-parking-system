package com.dev.marchenko.service;

import com.dev.marchenko.domain.lot.Level;
import com.dev.marchenko.domain.lot.ParkingLot;
import com.dev.marchenko.domain.slot.ParkingSlot;
import com.dev.marchenko.domain.slot.SlotType;
import com.dev.marchenko.domain.ticket.ParkingTicket;
import com.dev.marchenko.domain.vehicle.VehicleType;
import com.dev.marchenko.exception.NoAvailableSlotException;
import com.dev.marchenko.exception.VehicleAlreadyParkedException;
import com.dev.marchenko.repository.LevelRepository;
import com.dev.marchenko.repository.ParkingLotRepository;
import com.dev.marchenko.repository.SlotRepository;
import com.dev.marchenko.repository.TicketRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ParkingServiceIT {

    @Autowired
    private ParkingService parkingService;
    @Autowired
    private ParkingLotRepository lotRepository;
    @Autowired
    private LevelRepository levelRepository;
    @Autowired
    private SlotRepository slotRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private EntityManager entityManager;

    private Long compactSlotId;
    private Long largeSlotId;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        slotRepository.deleteAll();
        levelRepository.deleteAll();
        lotRepository.deleteAll();

        ParkingLot lot = new ParkingLot();
        lot.setName("Test Lot");
        lot = lotRepository.save(lot);

        Level level = new Level();
        level.setFloorNumber(1);
        level.setParkingLot(lot);
        level = levelRepository.save(level);

        ParkingSlot compactSlot = new ParkingSlot();
        compactSlot.setSlotNumber("C-1");
        compactSlot.setType(SlotType.COMPACT);
        compactSlot.setAvailable(true);
        compactSlot.setLevel(level);
        compactSlotId = slotRepository.save(compactSlot).getId();

        ParkingSlot largeSlot = new ParkingSlot();
        largeSlot.setSlotNumber("L-1");
        largeSlot.setType(SlotType.LARGE);
        largeSlot.setAvailable(true);
        largeSlot.setLevel(level);
        largeSlotId = slotRepository.save(largeSlot).getId();

        ParkingSlot handiSlot = new ParkingSlot();
        handiSlot.setSlotNumber("H-1");
        handiSlot.setType(SlotType.HANDICAPPED);
        handiSlot.setAvailable(true);
        handiSlot.setLevel(level);
        slotRepository.save(handiSlot);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void checkIn_ShouldUpdateDatabaseState() {
        ParkingTicket response = parkingService.checkIn("REAL-001", VehicleType.CAR, false);

        entityManager.flush();
        entityManager.clear();

        ParkingSlot savedSlot = slotRepository.findById(compactSlotId).orElseThrow();
        assertFalse(savedSlot.isAvailable(), "The slot in the database must be marked as false (occupied)");
    }

    @Test
    void checkIn_ShouldFallbackToCompactSlot() {
        ParkingTicket response = parkingService.checkIn("MOTO-1", VehicleType.MOTORCYCLE, false);
        entityManager.flush();
        entityManager.clear();

        assertEquals("C-1", response.getSlot().getSlotNumber());

        ParkingSlot slotInDb = slotRepository.findById(compactSlotId)
                .orElseThrow(() -> new AssertionError("Slot not found in DB"));

        assertFalse(slotInDb.isAvailable(), "The slot must be occupied in the database.");
    }

    @Test
    void checkIn_ShouldThrowIfAlreadyInDb() {
        parkingService.checkIn("DUP-111", VehicleType.CAR, false);

        entityManager.flush();
        entityManager.clear();

        assertThrows(VehicleAlreadyParkedException.class,
                () -> parkingService.checkIn("DUP-111", VehicleType.CAR, false));
    }

    @Test
    void checkIn_ShouldThrowWhenFull() {
        parkingService.checkIn("CAR-1", VehicleType.CAR, false);
        entityManager.flush();

        parkingService.checkIn("CAR-2", VehicleType.CAR, false);
        entityManager.flush();

        entityManager.clear();

        assertThrows(NoAvailableSlotException.class,
                () -> parkingService.checkIn("CAR-3", VehicleType.CAR, false));
    }

    @Test
    void checkOut_ShouldReleaseSlotAndCalculateFeeInDb() throws InterruptedException {
        ParkingTicket checkIn = parkingService.checkIn("EXIT-99", VehicleType.CAR, false);
        entityManager.flush();
        entityManager.clear();

        Thread.sleep(1000);

        var checkOut = parkingService.checkOut(checkIn.getId());

        entityManager.flush();
        entityManager.clear();

        ParkingSlot slotAfter = slotRepository.findById(compactSlotId).orElseThrow();
        assertTrue(slotAfter.isAvailable());

        assertNotNull(checkOut.getFee());
        assertTrue(checkOut.getFee().compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void checkIn_HandicappedShouldGetSpecialSlot() {
        ParkingTicket response = parkingService.checkIn("HANDI-1", VehicleType.CAR, true);

        assertEquals("H-1", response.getSlot().getSlotNumber(), "\n" +
                "Persons with disabilities must obtain a special H-1 visa.");

        entityManager.flush();
        entityManager.clear();

        ParkingSlot slotInDb = slotRepository.findAll().stream()
                .filter(s -> s.getSlotNumber().equals("H-1"))
                .findFirst().orElseThrow();
        assertFalse(slotInDb.isAvailable());
    }

    @Test
    void checkIn_RegularVehicleShouldNotOccupyHandicappedSlot() {
        parkingService.checkIn("CAR-1", VehicleType.CAR, false);
        parkingService.checkIn("CAR-2", VehicleType.CAR, false);

        entityManager.flush();

        assertThrows(NoAvailableSlotException.class,
                () -> parkingService.checkIn("REGULAR-CAR", VehicleType.CAR, false));
    }

    @Test
    void checkIn_HandicappedShouldFallbackToRegularSlotIfHandiFull() {
        parkingService.checkIn("HANDI-FIRST", VehicleType.CAR, true);

        ParkingTicket response = parkingService.checkIn("HANDI-SECOND", VehicleType.CAR, true);

        assertTrue(response.getSlot().getSlotNumber().startsWith("C") || response.getSlot().getSlotNumber().startsWith("L"),
                "The second disabled person must take a regular seat if there are no special seats available.");
    }
}