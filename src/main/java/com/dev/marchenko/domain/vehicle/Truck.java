package com.dev.marchenko.domain.vehicle;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TRUCK")
public class Truck extends Vehicle {
    public Truck(String licensePlate) {
        super(licensePlate);
    }

    public Truck() {
    }

    @Override
    public VehicleType getType() {
        return VehicleType.TRUCK;
    }
}

