package com.dev.marchenko.service;

import com.dev.marchenko.domain.lot.Level;
import com.dev.marchenko.domain.lot.ParkingLot;
import com.dev.marchenko.domain.slot.ParkingSlot;
import com.dev.marchenko.domain.slot.SlotType;
import com.dev.marchenko.domain.vehicle.VehicleType;
import com.dev.marchenko.dto.TicketResponse;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
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

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void checkIn_ShouldUpdateDatabaseState() {
        TicketResponse response = parkingService.checkIn("REAL-001", VehicleType.CAR);

        entityManager.flush();
        entityManager.clear();

        ParkingSlot savedSlot = slotRepository.findById(compactSlotId).orElseThrow();
        assertFalse(savedSlot.isAvailable(), "Слот в базе должен быть помечен как false (занят)");
    }

    @Test
    void checkIn_ShouldFallbackToCompactSlot() {
        TicketResponse response = parkingService.checkIn("MOTO-1", VehicleType.MOTORCYCLE);

        entityManager.flush();
        entityManager.clear();

        assertEquals("C-1", response.slotNumber());

        ParkingSlot slotInDb = slotRepository.findById(compactSlotId)
                .orElseThrow(() -> new AssertionError("Slot not found in DB"));

        assertFalse(slotInDb.isAvailable(), "Слот должен быть занят в базе данных");
    }

    @Test
    void checkIn_ShouldThrowIfAlreadyInDb() {
        parkingService.checkIn("DUP-111", VehicleType.CAR);

        entityManager.flush();
        entityManager.clear();

        assertThrows(VehicleAlreadyParkedException.class,
                () -> parkingService.checkIn("DUP-111", VehicleType.CAR));
    }

    @Test
    void checkIn_ShouldThrowWhenFull() {
        parkingService.checkIn("CAR-1", VehicleType.CAR);
        entityManager.flush();

        parkingService.checkIn("CAR-2", VehicleType.CAR);
        entityManager.flush();

        entityManager.clear();

        assertThrows(NoAvailableSlotException.class,
                () -> parkingService.checkIn("CAR-3", VehicleType.CAR));
    }

    @Test
    void checkOut_ShouldReleaseSlotAndCalculateFeeInDb() throws InterruptedException {
        TicketResponse checkIn = parkingService.checkIn("EXIT-99", VehicleType.CAR);

        entityManager.flush();
        entityManager.clear();

        Thread.sleep(1000);

        var checkOut = parkingService.checkOut(checkIn.ticketId());

        entityManager.flush();
        entityManager.clear();

        ParkingSlot slotAfter = slotRepository.findById(compactSlotId).orElseThrow();
        assertTrue(slotAfter.isAvailable());

        assertNotNull(checkOut.totalFee());
        assertTrue(checkOut.totalFee().compareTo(BigDecimal.ZERO) >= 0);
    }
}