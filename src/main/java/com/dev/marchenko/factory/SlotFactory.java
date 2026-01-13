package com.dev.marchenko.factory;

import com.dev.marchenko.domain.lot.Level;
import com.dev.marchenko.domain.slot.ParkingSlot;
import com.dev.marchenko.domain.slot.SlotType;
import org.springframework.stereotype.Component;

@Component
public class SlotFactory {

    public ParkingSlot createSlot(String slotNumber, SlotType type, Level level) {
        if (slotNumber == null || slotNumber.isBlank()) {
            throw new IllegalArgumentException("slotNumber cannot be null or blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("SlotType cannot be null");
        }
        if (level == null) {
            throw new IllegalArgumentException("Level cannot be null");
        }

        return new ParkingSlot(slotNumber, type, level);
    }
}
