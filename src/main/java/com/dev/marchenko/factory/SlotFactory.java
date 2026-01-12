package com.dev.marchenko.factory;

import com.dev.marchenko.domain.lot.Level;
import com.dev.marchenko.domain.slot.ParkingSlot;
import com.dev.marchenko.domain.slot.SlotType;
import org.springframework.stereotype.Component;

@Component
public class SlotFactory {

    public ParkingSlot createSlot(String slotNumber, SlotType type, Level level) {
        ParkingSlot slot = new ParkingSlot();
        slot.setSlotNumber(slotNumber);
        slot.setType(type);
        slot.setLevel(level);
        slot.setAvailable(true);

        return slot;
    }
}
