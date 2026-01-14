package com.dev.marchenko.service;

import com.dev.marchenko.domain.lot.Level;
import com.dev.marchenko.domain.lot.ParkingLot;
import com.dev.marchenko.domain.slot.ParkingSlot;
import com.dev.marchenko.domain.slot.SlotType;
import com.dev.marchenko.domain.ticket.ParkingTicket;
import com.dev.marchenko.domain.vehicle.VehicleType;
import com.dev.marchenko.exception.NoAvailableSlotException;
import com.dev.marchenko.exception.VehicleAlreadyParkedException;
import com.dev.marchenko.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ParkingServiceIT extends BaseIntegrationTest {

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
    private VehicleRepository vehicleRepository;
    @Autowired
    private EntityManager entityManager;

    private Long compactSlotId;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAllInBatch();
        slotRepository.deleteAllInBatch();
        levelRepository.deleteAllInBatch();
        lotRepository.deleteAllInBatch();
        vehicleRepository.deleteAllInBatch();

        ParkingLot lot = lotRepository.save(new ParkingLot("Test Lot"));

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
        slotRepository.save(largeSlot);

        ParkingSlot handiSlot = new ParkingSlot();
        handiSlot.setSlotNumber("H-1");
        handiSlot.setType(SlotType.HANDICAPPED);
        handiSlot.setAvailable(true);
        handiSlot.setLevel(level);
        slotRepository.save(handiSlot);

    }

    @Test
    void checkIn_ShouldUpdateDatabaseState() {
        parkingService.checkIn("REAL-001", VehicleType.CAR, false);

        ParkingSlot savedSlot = slotRepository.findById(compactSlotId).orElseThrow();
        assertFalse(savedSlot.isAvailable(), "The database slot must be occupied.");
    }

    @Test
    void checkIn_ConcurrencyTest_WithPostgresLocking() throws InterruptedException {
        ticketRepository.deleteAllInBatch();
        slotRepository.deleteAllInBatch();
        setUp();
        slotRepository.findAll().stream()
                .filter(s -> !s.getSlotNumber().equals("C-1"))
                .forEach(s -> slotRepository.delete(s));

        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            final String plate = "CONC-" + i;
            executor.execute(() -> {
                try {
                    startLatch.await();
                    parkingService.checkIn(plate, VehicleType.CAR, false);
                    successCount.incrementAndGet();
                } catch (NoAvailableSlotException e) {
                    errorCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = endLatch.await(10, TimeUnit.SECONDS);

        assertTrue(completed, "The test did not complete on time.");
        assertEquals(1, successCount.get(), "Only one machine should occupy a slot");
        assertEquals(threads - 1, errorCount.get(), "The rest should be rejected due to lack of space.");
    }

    @Test
    void checkIn_ShouldThrowIfAlreadyInDb() {
        parkingService.checkIn("DUP-111", VehicleType.CAR, false);

        assertThrows(VehicleAlreadyParkedException.class,
                () -> parkingService.checkIn("DUP-111", VehicleType.CAR, false));
    }

    @Test
    void checkOut_ShouldReleaseSlotAndCalculateFee() {
        ParkingTicket ticket = parkingService.checkIn("L-123", VehicleType.CAR, false);
        Long ticketId = ticket.getId();

        ticket.setEntryTime(LocalDateTime.now().minusHours(2));
        ticketRepository.saveAndFlush(ticket);

        entityManager.clear();

        ParkingTicket finalTicket = parkingService.checkOut(ticketId);

        assertNotNull(finalTicket.getExitTime());
        assertNotNull(finalTicket.getFee());
        assertTrue(finalTicket.getFee().compareTo(BigDecimal.ZERO) > 0);

        ParkingSlot slot = slotRepository.findById(finalTicket.getSlot().getId()).orElseThrow();
        assertTrue(slot.isAvailable(), "The slot must become free after departure");
    }

    @Test
    void checkIn_HandicappedShouldGetSpecialSlot() {
        ParkingTicket response = parkingService.checkIn("HANDI-1", VehicleType.CAR, true);
        assertEquals("H-1", response.getSlot().getSlotNumber());

        ParkingSlot slotInDb = slotRepository.findAll().stream()
                .filter(s -> s.getSlotNumber().equals("H-1"))
                .findFirst().orElseThrow();
        assertFalse(slotInDb.isAvailable());
    }

    @Test
    void checkIn_HandicappedShouldFallbackToRegularSlotIfHandiFull() {
        parkingService.checkIn("HANDI-1", VehicleType.CAR, true);

        ParkingTicket response = parkingService.checkIn("HANDI-2", VehicleType.CAR, true);

        String slotNum = response.getSlot().getSlotNumber();
        assertTrue(slotNum.startsWith("C") || slotNum.startsWith("L"),
                "A disabled person must take a regular seat if a special seat is occupied.");
    }

    @Test
    void checkOut_ShouldCalculateCorrectFeeForTwoHours() {
        ParkingTicket ticket = parkingService.checkIn("FEE-123", VehicleType.CAR, false);

        ticket.setEntryTime(LocalDateTime.now().minusHours(2));
        ticketRepository.saveAndFlush(ticket);

        entityManager.clear();

        ParkingTicket result = parkingService.checkOut(ticket.getId());

        assertNotNull(result.getFee());
        assertEquals(0, new BigDecimal("4.00").compareTo(result.getFee()),
                "The cost for 2 hours (at 2.00/hour) should be 4.00");
    }

    @Test
    void checkIn_ConcurrencyTest_ShouldAllowOnlyOneVehicle() throws InterruptedException {
        ticketRepository.deleteAllInBatch();
        vehicleRepository.deleteAllInBatch();

        slotRepository.findAll().stream()
                .filter(s -> !s.getSlotNumber().equals("C-1"))
                .forEach(s -> slotRepository.delete(s));

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch allDoneLatch = new CountDownLatch(threadCount);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger failure = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            final String uniquePlate = "PLATE-" + i;
            executor.execute(() -> {
                try {
                    startLatch.await();
                    parkingService.checkIn(uniquePlate, VehicleType.CAR, false);
                    success.incrementAndGet();
                } catch (Exception e) {
                    failure.incrementAndGet();
                } finally {
                    allDoneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean finished = allDoneLatch.await(15, TimeUnit.SECONDS);

        assertTrue(finished, "The test did not have time to complete");
        assertEquals(1, success.get(), "Only one car should park");
        assertEquals(threadCount - 1, failure.get(), "The rest should be refused");

        executor.shutdown();
    }
}