package com.dev.marchenko.service;

import com.dev.marchenko.domain.lot.Level;
import com.dev.marchenko.domain.slot.ParkingSlot;
import com.dev.marchenko.domain.slot.SlotType;
import com.dev.marchenko.domain.ticket.ParkingTicket;
import com.dev.marchenko.domain.vehicle.*;
import com.dev.marchenko.dto.CheckOutResponse;
import com.dev.marchenko.dto.TicketResponse;
import com.dev.marchenko.exception.*;
import com.dev.marchenko.mapper.ParkingMapper;
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
    @Mock
    private ParkingMapper parkingMapper;

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

            TicketResponse response = parkingService.checkIn(plate, VehicleType.MOTORCYCLE);

            assertNotNull(response);
            assertEquals(plate, response.licensePlate());
            assertFalse(slot.isAvailable());
            verify(slotRepository).save(slot);
        }

        @Test
        void checkIn_PrioritySelection() {
            ParkingSlot compactSlot = createMockSlot("C1", SlotType.COMPACT);
            when(vehicleRepository.findById(plate)).thenReturn(Optional.of(new Motorcycle(plate)));

            when(slotRepository.findFirstByAvailableTrueAndTypeOrderByLevelFloorNumberAsc(SlotType.MOTORCYCLE))
                    .thenReturn(Optional.empty());
            when(slotRepository.findFirstByAvailableTrueAndTypeOrderByLevelFloorNumberAsc(SlotType.COMPACT))
                    .thenReturn(Optional.of(compactSlot));

            when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            parkingService.checkIn(plate, VehicleType.MOTORCYCLE);

            verify(slotRepository).findFirstByAvailableTrueAndTypeOrderByLevelFloorNumberAsc(SlotType.MOTORCYCLE);
            verify(slotRepository).findFirstByAvailableTrueAndTypeOrderByLevelFloorNumberAsc(SlotType.COMPACT);
            assertFalse(compactSlot.isAvailable());
        }

        @Test
        void checkIn_Throws_TypeMismatch() {
            Vehicle existingCar = new Car(plate);
            when(vehicleRepository.findById(plate)).thenReturn(Optional.of(existingCar));

            assertThrows(LicensePlateAlreadyRegisteredException.class,
                    () -> parkingService.checkIn(plate, VehicleType.TRUCK));
        }

        @Test
        void checkIn_Throws_AlreadyParked() {
            Vehicle vehicle = new Car(plate);
            when(vehicleRepository.findById(plate)).thenReturn(Optional.of(vehicle));
            when(ticketRepository.existsByVehicleAndExitTimeIsNull(vehicle)).thenReturn(true);

            assertThrows(VehicleAlreadyParkedException.class,
                    () -> parkingService.checkIn(plate, VehicleType.CAR));
        }

        @Test
        void checkIn_Throws_NoAvailableSlots() {
            when(vehicleRepository.findById(plate)).thenReturn(Optional.of(new Truck(plate)));
            when(slotRepository.findFirstByAvailableTrueAndTypeOrderByLevelFloorNumberAsc(any()))
                    .thenReturn(Optional.empty());

            assertThrows(NoAvailableSlotException.class,
                    () -> parkingService.checkIn(plate, VehicleType.TRUCK));
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

            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(pricingService.calculate(any(), any(), any())).thenReturn(new BigDecimal("50.0"));
            when(parkingMapper.toCheckOutResponse(any())).thenReturn(CheckOutResponse.builder().totalFee(new BigDecimal("50.0")).build());

            CheckOutResponse response = parkingService.checkOut(1L);

            assertTrue(slot.isAvailable());
            assertNotNull(ticket.getExitTime());
            assertEquals(new BigDecimal("50.0"), ticket.getFee());
            verify(ticketRepository).save(ticket);
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
        when(parkingMapper.toTicketResponse(any())).thenReturn(TicketResponse.builder().build());

        List<TicketResponse> sessions = parkingService.getActiveSessions();

        assertEquals(1, sessions.size());
        verify(ticketRepository).findAllByExitTimeIsNull();
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