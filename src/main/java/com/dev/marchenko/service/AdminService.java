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

    @Transactional
    public ParkingLot createLot(ParkingLotRequest request) {
        ParkingLot parkingLot = new ParkingLot();
        parkingLot.setName(request.name());
        return lotRepository.save(parkingLot);
    }

    @Transactional
    public void removeLot(Long id) {
        if (!lotRepository.existsById(id)) {
            throw new ResourceNotFoundException("Parking Lot", id);
        }
        lotRepository.deleteById(id);
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
        if (!levelRepository.existsById(levelId)) {
            throw new ResourceNotFoundException("Level", levelId);
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
        slotRepository.deleteById(slotId);
    }

    @Transactional
    public ParkingSlot updateSlotAvailability(Long slotId, boolean isAvailable) {
        ParkingSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot", slotId));

        if (slot.isAvailable() && !isAvailable) {
            throw new IllegalStateException("Cannot mark an occupied slot as unavailable for maintenance.");
        }

        slot.setAvailable(isAvailable);

        return slotRepository.save(slot);

    }
}
