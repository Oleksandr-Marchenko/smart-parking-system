package com.dev.marchenko.mapper;

import com.dev.marchenko.domain.lot.Level;
import com.dev.marchenko.domain.lot.ParkingLot;
import com.dev.marchenko.domain.slot.ParkingSlot;
import com.dev.marchenko.domain.ticket.ParkingTicket;
import com.dev.marchenko.dto.*;
import com.dev.marchenko.formatter.MoneyFormatter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = MoneyFormatter.class)
public interface ParkingMapper {

    @Mapping(target = "ticketId", source = "id")
    @Mapping(target = "licensePlate", source = "vehicle.licensePlate")
    @Mapping(target = "vehicleType", source = "vehicle.type")
    @Mapping(target = "slotNumber", source = "slot.slotNumber")
    @Mapping(target = "levelFloor", source = "slot.level.floorNumber")
    TicketResponse toTicketResponse(ParkingTicket ticket);

    @Mapping(target = "licensePlate", source = "vehicle.licensePlate")
    @Mapping(target = "durationMinutes", expression = "java(calculateDuration(ticket))")
    @Mapping(target = "totalFee", source = "fee")
    CheckOutResponse toCheckOutResponse(ParkingTicket ticket);

    default BigDecimal mapBigDecimal(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    ParkingLotResponse toLotResponse(ParkingLot lot);

    @Mapping(target = "parkingLotId", source = "parkingLot.id")
    LevelResponse toLevelResponse(Level level);

    @Mapping(target = "isAvailable", source = "available")
    SlotResponse toSlotResponse(ParkingSlot slot);

    default Long calculateDuration(ParkingTicket ticket) {
        if (ticket.getEntryTime() == null || ticket.getExitTime() == null) {
            return 0L;
        }
        return Duration.between(ticket.getEntryTime(), ticket.getExitTime()).toMinutes();
    }
}
