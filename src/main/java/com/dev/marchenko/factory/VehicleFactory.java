package com.dev.marchenko.factory;

import com.dev.marchenko.domain.vehicle.*;

import java.util.Map;
import java.util.function.Function;

public class VehicleFactory {

    private static final Map<VehicleType, Function<String, Vehicle>> registry = Map.of(
            VehicleType.CAR, Car::new,
            VehicleType.TRUCK, Truck::new,
            VehicleType.MOTORCYCLE, Motorcycle::new
    );

    public static Vehicle createVehicle(String licensePlate, VehicleType type) {
        var constructor = registry.get(type);
        if (constructor == null) {
            throw new IllegalArgumentException("Unknown vehicle type: " + type);
        }
        return constructor.apply(licensePlate);
    }
}
