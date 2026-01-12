package com.dev.marchenko.domain.slot;

import com.dev.marchenko.domain.lot.Level;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "slots")
public class ParkingSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String slotNumber;

    @Enumerated(EnumType.STRING)
    private SlotType type;

    private boolean isAvailable = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id")
    private Level level;
}
