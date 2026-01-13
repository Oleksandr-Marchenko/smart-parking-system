package com.dev.marchenko.domain.slot;

import com.dev.marchenko.domain.lot.Level;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "slots")
public class ParkingSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    private String slotNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    private SlotType type;

    private boolean available = true;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id")
    private Level level;

    public ParkingSlot(@NotBlank String slotNumber, @NotNull SlotType type, @NotNull Level level) {
        this.slotNumber = slotNumber;
        this.type = type;
        this.level = level;
    }
}
