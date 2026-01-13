package com.dev.marchenko.service;

import com.dev.marchenko.domain.lot.Level;
import com.dev.marchenko.domain.lot.ParkingLot;
import com.dev.marchenko.domain.slot.ParkingSlot;
import com.dev.marchenko.dto.LevelRequest;
import com.dev.marchenko.dto.ParkingLotRequest;
import com.dev.marchenko.dto.SlotRequest;
import com.dev.marchenko.exception.ResourceNotFoundException;
import com.dev.marchenko.factory.SlotFactory;
import com.dev.marchenko.repository.LevelRepository;
import com.dev.marchenko.repository.ParkingLotRepository;
import com.dev.marchenko.repository.SlotRepository;
import com.dev.marchenko.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final ParkingLotRepository lotRepository;
    private final LevelRepository levelRepository;
    private final SlotRepository slotRepository;
    private final SlotFactory slotFactory;
    private final TicketRepository ticketRepository;

    @Transactional
    public ParkingLot createLot(ParkingLotRequest request) {
        ParkingLot parkingLot = new ParkingLot();
        parkingLot.setName(request.name());
        return lotRepository.save(parkingLot);
    }

    @Transactional
    public void removeLot(Long lotId) {
        ParkingLot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking Lot", lotId));

        boolean hasTickets = lot.getLevels().stream()
                .flatMap(level -> level.getSlots().stream())
                .anyMatch(slot -> ticketRepository.existsBySlotIdAndExitTimeIsNull(slot.getId()));

        if (hasTickets) {
            throw new IllegalStateException(
                    "Cannot delete parking lot: it contains slots with vehicles assigned."
            );
        }
        lotRepository.deleteById(lotId);
    }

    @Transactional
    public Level addLevel(Long lotId, LevelRequest request) {
        ParkingLot parkingLot = lotRepository.findById(lotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking Lot", lotId));

        Level level = new Level();
        level.setFloorNumber(request.floorNumber());
        level.setParkingLot(parkingLot);

        parkingLot.addLevel(level);

        return levelRepository.save(level);
    }

    @Transactional
    public void removeLevel(Long levelId) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new ResourceNotFoundException("Level", levelId));

        boolean hasTickets = level.getSlots().stream()
                .anyMatch(slot -> ticketRepository.existsBySlotIdAndExitTimeIsNull(slot.getId()));

        if (hasTickets) {
            throw new IllegalStateException("Cannot delete level: it contains slots with vehicles assigned.");
        }

        levelRepository.deleteById(levelId);
    }

    @Transactional
    public ParkingSlot addSlot(Long levelId, SlotRequest request) {

        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new ResourceNotFoundException("Level", levelId));

        ParkingSlot parkingSlot = slotFactory.createSlot(request.slotNumber(), request.type(), level);

        level.addSlot(parkingSlot);

        return slotRepository.save(parkingSlot);
    }

    @Transactional
    public void removeSlot(Long slotId) {
        if (!slotRepository.existsById(slotId)) {
            throw new ResourceNotFoundException("Slot", slotId);
        }
        if (ticketRepository.existsBySlotIdAndExitTimeIsNull(slotId)) {
            throw new IllegalStateException("Cannot delete slot: it is currently occupied by a vehicle.");
        }

        slotRepository.deleteById(slotId);
    }

    @Transactional
    public ParkingSlot updateSlotAvailability(Long slotId, boolean isAvailable) {
        ParkingSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot", slotId));

        if (!isAvailable && ticketRepository.existsBySlotIdAndExitTimeIsNull(slotId)) {
            throw new IllegalStateException("Cannot mark slot as unavailable: it is currently occupied by a vehicle.");
        }

        slot.setAvailable(isAvailable);

        return slotRepository.save(slot);

    }
}
