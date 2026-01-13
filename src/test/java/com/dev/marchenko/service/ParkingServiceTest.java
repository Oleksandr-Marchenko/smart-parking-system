package com.dev.marchenko.service;

import com.dev.marchenko.domain.lot.Level;
import com.dev.marchenko.domain.slot.ParkingSlot;
import com.dev.marchenko.domain.slot.SlotType;
import com.dev.marchenko.domain.ticket.ParkingTicket;
import com.dev.marchenko.domain.vehicle.*;
import com.dev.marchenko.exception.*;
import com.dev.marchenko.repository.SlotRepository;
import com.dev.marchenko.repository.TicketRepository;
import com.dev.marchenko.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private SlotRepository slotRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private PricingService pricingService;

    @InjectMocks
    private ParkingService parkingService;

    private String plate;
    private Level mockLevel;

    @BeforeEach
    void setUp() {
        plate = "ABC-123";
        mockLevel = new Level();
        mockLevel.setFloorNumber(1);
    }

    @Nested
    class CheckInTests {

        @Test
        void checkIn_Success_NewVehicle() {
            ParkingSlot slot = createMockSlot("A1", SlotType.MOTORCYCLE);
            when(vehicleRepository.findById(plate)).thenReturn(Optional.empty());
            when(vehicleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(ticketRepository.existsByVehicleAndExitTimeIsNull(any())).thenReturn(false);
            when(slotRepository.findFirstByAvailableTrueAndTypeOrderByLevelFloorNumberAsc(SlotType.MOTORCYCLE))
                    .thenReturn(Optional.of(slot));
            when(ticketRepository.save(any())).thenAnswer(inv -> {
                ParkingTicket t = inv.getArgument(0);
                t.setId(1L);
                return t;
            });

            ParkingTicket result = parkingService.checkIn(plate, VehicleType.MOTORCYCLE, false);

            assertNotNull(result);
            assertEquals(plate, result.getVehicle().getLicensePlate());
            assertFalse(slot.isAvailable());
            verify(slotRepository).save(slot);
        }

        @Test
        void checkIn_PrioritySelection() {
            ParkingSlot compactSlot = createMockSlot("C1", SlotType.COMPACT);
            when(vehicleRepository.findById(plate)).thenReturn(Optional.of(new Motorcycle(plate)));
            when(ticketRepository.existsByVehicleAndExitTimeIsNull(any())).thenReturn(false);

            when(slotRepository.findFirstByAvailableTrueAndTypeOrderByLevelFloorNumberAsc(SlotType.MOTORCYCLE))
                    .thenReturn(Optional.empty());
            when(slotRepository.findFirstByAvailableTrueAndTypeOrderByLevelFloorNumberAsc(SlotType.COMPACT))
                    .thenReturn(Optional.of(compactSlot));

            when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            parkingService.checkIn(plate, VehicleType.MOTORCYCLE, false);

            verify(slotRepository).findFirstByAvailableTrueAndTypeOrderByLevelFloorNumberAsc(SlotType.MOTORCYCLE);
            verify(slotRepository).findFirstByAvailableTrueAndTypeOrderByLevelFloorNumberAsc(SlotType.COMPACT);
            assertFalse(compactSlot.isAvailable());
        }

        @Test
        void checkIn_Throws_TypeMismatch() {
            Vehicle existingCar = new Car(plate);
            when(vehicleRepository.findById(plate)).thenReturn(Optional.of(existingCar));

            assertThrows(LicensePlateAlreadyRegisteredException.class,
                    () -> parkingService.checkIn(plate, VehicleType.TRUCK, false));
        }

        @Test
        void checkIn_Throws_AlreadyParked() {
            Vehicle vehicle = new Car(plate);
            when(vehicleRepository.findById(plate)).thenReturn(Optional.of(vehicle));
            when(ticketRepository.existsByVehicleAndExitTimeIsNull(vehicle)).thenReturn(true);

            assertThrows(VehicleAlreadyParkedException.class,
                    () -> parkingService.checkIn(plate, VehicleType.CAR, false));
        }

        @Test
        void checkIn_Throws_NoAvailableSlots() {
            when(vehicleRepository.findById(plate)).thenReturn(Optional.of(new Truck(plate)));
            when(ticketRepository.existsByVehicleAndExitTimeIsNull(any())).thenReturn(false);
            when(slotRepository.findFirstByAvailableTrueAndTypeOrderByLevelFloorNumberAsc(any()))
                    .thenReturn(Optional.empty());

            assertThrows(NoAvailableSlotException.class,
                    () -> parkingService.checkIn(plate, VehicleType.TRUCK, false));
        }

        @Test
        void checkIn_Handicapped_Priority_Success() {
            ParkingSlot handiSlot = createMockSlot("H1", SlotType.HANDICAPPED);
            when(vehicleRepository.findById(plate)).thenReturn(Optional.empty());
            when(vehicleRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(ticketRepository.existsByVehicleAndExitTimeIsNull(any())).thenReturn(false);

            when(slotRepository.findFirstByAvailableTrueAndTypeOrderByLevelFloorNumberAsc(SlotType.HANDICAPPED))
                    .thenReturn(Optional.of(handiSlot));
            when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            ParkingTicket response = parkingService.checkIn(plate, VehicleType.CAR, true);


            assertEquals(SlotType.HANDICAPPED.name(), response.getSlot().getSlotNumber().startsWith("H") ? SlotType.HANDICAPPED.name() : "");
            verify(slotRepository).findFirstByAvailableTrueAndTypeOrderByLevelFloorNumberAsc(SlotType.HANDICAPPED);
        }
    }

    @Nested
    class CheckOutTests {

        @Test
        void checkOut_Success() {
            ParkingSlot slot = createMockSlot("B1", SlotType.LARGE);
            slot.setAvailable(false);

            ParkingTicket ticket = new ParkingTicket();
            ticket.setSlot(slot);
            ticket.setVehicle(new Truck(plate));
            ticket.setEntryTime(LocalDateTime.now().minusHours(2));

            when(ticketRepository.save(any(ParkingTicket.class))).thenAnswer(i -> i.getArgument(0));
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(pricingService.calculate(any(), any(), any())).thenReturn(new BigDecimal("50.0"));

            ParkingTicket result = parkingService.checkOut(1L);

            assertTrue(slot.isAvailable());
            assertNotNull(result);
            assertEquals(new BigDecimal("50.0"), result.getFee());
        }

        @Test
        void checkOut_Throws_NotFound() {
            when(ticketRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(TicketNotFoundException.class, () -> parkingService.checkOut(1L));
        }

        @Test
        void checkOut_Throws_AlreadyClosed() {
            ParkingTicket closedTicket = new ParkingTicket();
            closedTicket.setExitTime(LocalDateTime.now());
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(closedTicket));

            assertThrows(TicketAlreadyClosedException.class, () -> parkingService.checkOut(1L));
        }
    }

    @Test
    void getActiveSessions_Success() {
        when(ticketRepository.findAllByExitTimeIsNull()).thenReturn(List.of(new ParkingTicket()));

        List<ParkingTicket> sessions = parkingService.getActiveSessions();

        assertEquals(1, sessions.size());
        verify(ticketRepository).findAllByExitTimeIsNull();
    }

    @Test
    void checkIn_Handicapped_Fallback_To_RegularSlot() {
        ParkingSlot regularSlot = createMockSlot("C1", SlotType.COMPACT);
        when(vehicleRepository.findById(plate)).thenReturn(Optional.of(new Car(plate)));
        when(ticketRepository.existsByVehicleAndExitTimeIsNull(any())).thenReturn(false);

        when(slotRepository.findFirstByAvailableTrueAndTypeOrderByLevelFloorNumberAsc(SlotType.HANDICAPPED))
                .thenReturn(Optional.empty());
        when(slotRepository.findFirstByAvailableTrueAndTypeOrderByLevelFloorNumberAsc(SlotType.COMPACT))
                .thenReturn(Optional.of(regularSlot));
        when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ParkingTicket response = parkingService.checkIn(plate, VehicleType.CAR, true);

        assertNotNull(response);
        assertEquals("C1", response.getSlot().getSlotNumber());
        assertFalse(regularSlot.isAvailable());
    }

    @Test
    void checkIn_RegularVehicle_Cannot_Occupy_HandicappedSlot() {
        when(vehicleRepository.findById(plate)).thenReturn(Optional.of(new Car(plate)));
        when(ticketRepository.existsByVehicleAndExitTimeIsNull(any())).thenReturn(false);

        when(slotRepository.findFirstByAvailableTrueAndTypeOrderByLevelFloorNumberAsc(SlotType.COMPACT))
                .thenReturn(Optional.empty());
        when(slotRepository.findFirstByAvailableTrueAndTypeOrderByLevelFloorNumberAsc(SlotType.LARGE))
                .thenReturn(Optional.empty());

        assertThrows(NoAvailableSlotException.class,
                () -> parkingService.checkIn(plate, VehicleType.CAR, false));
    }

    private ParkingSlot createMockSlot(String number, SlotType type) {
        ParkingSlot slot = new ParkingSlot();
        slot.setSlotNumber(number);
        slot.setType(type);
        slot.setAvailable(true);
        slot.setLevel(mockLevel);
        return slot;
    }
}