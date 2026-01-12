package com.dev.marchenko.domain.vehicle;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CAR")
public class Car extends Vehicle {
    public Car(String licensePlate) {
        super(licensePlate);
    }

    public Car() {
    }

    @Override
    public VehicleType getType() {
        return VehicleType.CAR;
    }
}
