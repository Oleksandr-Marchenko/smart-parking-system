package com.dev.marchenko.factory;

import com.dev.marchenko.domain.vehicle.*;

public class VehicleFactory {

    public static Vehicle createVehicle(String licensePlate, VehicleType type) {
        return switch (type) {
            case CAR -> new Car(licensePlate);
            case TRUCK -> new Truck(licensePlate);
            case MOTORCYCLE -> new Motorcycle(licensePlate);
        };
    }
}
