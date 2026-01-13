package com.dev.marchenko.service;

import com.dev.marchenko.domain.lot.Level;
import com.dev.marchenko.domain.lot.ParkingLot;
import com.dev.marchenko.domain.slot.ParkingSlot;
import com.dev.marchenko.domain.slot.SlotType;
import com.dev.marchenko.dto.LevelRequest;
import com.dev.marchenko.dto.ParkingLotRequest;
import com.dev.marchenko.dto.SlotRequest;
import com.dev.marchenko.exception.ResourceNotFoundException;
import com.dev.marchenko.factory.SlotFactory;
import com.dev.marchenko.repository.LevelRepository;
import com.dev.marchenko.repository.ParkingLotRepository;
import com.dev.marchenko.repository.SlotRepository;
import com.dev.marchenko.repository.TicketRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private ParkingLotRepository lotRepository;
    @Mock
    private LevelRepository levelRepository;
    @Mock
    private SlotRepository slotRepository;
    @Mock
    private SlotFactory slotFactory;
    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private AdminService adminService;

    @Nested
    class LotTests {
        @Test
        void createLot_Success() {
            ParkingLotRequest request = new ParkingLotRequest("Central Park");
            when(lotRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ParkingLot result = adminService.createLot(request);

            assertEquals("Central Park", result.getName());
            verify(lotRepository).save(any());
        }

        @Test
        void removeLot_NotFound() {
            when(lotRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> adminService.removeLot(1L));
        }

        @Test
        void removeLot_Occupied() {
            ParkingLot lot = new ParkingLot();
            Level level = new Level();
            ParkingSlot slot = new ParkingSlot();
            slot.setId(10L);
            level.setSlots(List.of(slot));
            lot.setLevels(new ArrayList<>(List.of(level)));

            when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
            when(ticketRepository.existsBySlotIdAndExitTimeIsNull(10L)).thenReturn(true);

            assertThrows(IllegalStateException.class, () -> adminService.removeLot(1L));
            verify(lotRepository, never()).deleteById(any());
        }
    }

    @Nested
    class LevelTests {
        @Test
        void addLevel_Success() {
            ParkingLot lot = new ParkingLot();
            when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
            when(levelRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Level level = adminService.addLevel(1L, new LevelRequest(2));

            assertEquals(2, level.getFloorNumber());
            assertEquals(lot, level.getParkingLot());
        }

        @Test
        void removeLevel_Occupied() {
            Level level = new Level();
            ParkingSlot slot = new ParkingSlot();
            slot.setId(5L);
            level.setSlots(List.of(slot));

            when(levelRepository.findById(1L)).thenReturn(Optional.of(level));
            when(ticketRepository.existsBySlotIdAndExitTimeIsNull(5L)).thenReturn(true);

            assertThrows(IllegalStateException.class, () -> adminService.removeLevel(1L));
        }
    }

    @Nested
    class SlotTests {
        @Test
        void addSlot_Success() {
            Level level = new Level();
            SlotRequest request = new SlotRequest("A-101", SlotType.COMPACT);
            ParkingSlot slot = new ParkingSlot();

            when(levelRepository.findById(1L)).thenReturn(Optional.of(level));
            when(slotFactory.createSlot(anyString(), any(), any())).thenReturn(slot);
            when(slotRepository.save(any())).thenReturn(slot);

            ParkingSlot result = adminService.addSlot(1L, request);

            assertNotNull(result);
            verify(slotRepository).save(any());
        }

        @Test
        void removeSlot_Success() {
            when(slotRepository.existsById(1L)).thenReturn(true);
            when(ticketRepository.existsBySlotIdAndExitTimeIsNull(1L)).thenReturn(false);

            adminService.removeSlot(1L);

            verify(slotRepository).deleteById(1L);
        }

        @Test
        void removeSlot_Occupied() {
            when(slotRepository.existsById(1L)).thenReturn(true);
            when(ticketRepository.existsBySlotIdAndExitTimeIsNull(1L)).thenReturn(true);

            assertThrows(IllegalStateException.class, () -> adminService.removeSlot(1L));
        }
    }

    @Nested
    class AvailabilityTests {
        @Test
        void updateAvailability_Success() {
            ParkingSlot slot = new ParkingSlot();
            slot.setAvailable(true);

            when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));
            when(ticketRepository.existsBySlotIdAndExitTimeIsNull(1L)).thenReturn(false);
            when(slotRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ParkingSlot result = adminService.updateSlotAvailability(1L, false);

            assertFalse(result.isAvailable());
        }

        @Test
        void updateAvailability_Occupied() {
            when(slotRepository.findById(1L)).thenReturn(Optional.of(new ParkingSlot()));
            when(ticketRepository.existsBySlotIdAndExitTimeIsNull(1L)).thenReturn(true);

            assertThrows(IllegalStateException.class,
                    () -> adminService.updateSlotAvailability(1L, false));
        }
    }
}