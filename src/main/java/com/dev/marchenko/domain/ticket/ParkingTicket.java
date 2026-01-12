package com.dev.marchenko.domain.ticket;

import com.dev.marchenko.domain.slot.ParkingSlot;
import com.dev.marchenko.domain.vehicle.Vehicle;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_tickets")
@Data
public class ParkingTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vehicle_plate", nullable = false)
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "slot_id", nullable = false)
    private ParkingSlot slot;

    private LocalDateTime entryTime;

    private LocalDateTime exitTime;
    private BigDecimal fee;

    public Integer getLevelNumber() {
        return slot != null && slot.getLevel() != null ? slot.getLevel().getFloorNumber() : null;
    }
}
