package com.dev.marchenko.domain.vehicle;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicles")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "vehicle_type_id")
@Getter
@Setter
@NoArgsConstructor
public abstract class Vehicle {

    @Id
    @Column(name = "license_plate", length = 20)
    private String licensePlate;

    protected Vehicle(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public abstract VehicleType getType();
}
