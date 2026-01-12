package com.dev.marchenko.domain.vehicle;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("MOTORCYCLE")
public class Motorcycle extends Vehicle {
    public Motorcycle(String licensePlate) {
        super(licensePlate);
    }

    public Motorcycle() {
    }

    @Override
    public VehicleType getType() {
        return VehicleType.MOTORCYCLE;
    }
}
