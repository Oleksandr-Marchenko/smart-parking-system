package com.dev.marchenko.service;

import com.dev.marchenko.domain.slot.ParkingSlot;
import com.dev.marchenko.domain.slot.SlotType;
import com.dev.marchenko.domain.ticket.ParkingTicket;
import com.dev.marchenko.domain.vehicle.*;
import com.dev.marchenko.dto.CheckOutResponse;
import com.dev.marchenko.dto.TicketResponse;
import com.dev.marchenko.exception.*;
import com.dev.marchenko.factory.VehicleFactory;
import com.dev.marchenko.mapper.ParkingMapper;
import com.dev.marchenko.repository.SlotRepository;
import com.dev.marchenko.repository.TicketRepository;
import com.dev.marchenko.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParkingService {

    private final VehicleRepository vehicleRepository;
    private final SlotRepository slotRepository;
    private final TicketRepository ticketRepository;
    private final PricingService pricingService;
    private final ParkingMapper parkingMapper;

    private static final Map<VehicleType, List<SlotType>> COMPATIBILITY_MAP = Map.of(
            VehicleType.MOTORCYCLE, List.of(SlotType.MOTORCYCLE, SlotType.COMPACT, SlotType.LARGE),
            VehicleType.CAR, List.of(SlotType.COMPACT, SlotType.LARGE),
            VehicleType.TRUCK, List.of(SlotType.LARGE)
    );

    @Transactional
    public TicketResponse checkIn(String licensePlate, VehicleType type) {
        Vehicle vehicle = vehicleRepository.findById(licensePlate)
                .orElseGet(() -> vehicleRepository.save(VehicleFactory.createVehicle(licensePlate, type)));

        if (!isInstanceValid(vehicle, type)) {
            throw new LicensePlateAlreadyRegisteredException(licensePlate);
        }

        if (ticketRepository.existsByVehicleAndExitTimeIsNull(vehicle)) {
            throw new VehicleAlreadyParkedException(licensePlate);
        }

        List<SlotType> allowedTypes = COMPATIBILITY_MAP.get(type);

        ParkingSlot slot = allowedTypes.stream()
                .map(slotRepository::findFirstByAvailableTrueAndTypeOrderByLevelFloorNumberAsc)
                .flatMap(Optional::stream)
                .findFirst()
                .orElseThrow(() -> new NoAvailableSlotException(type.name()));

        slot.setAvailable(false);
        slotRepository.save(slot);

        ParkingTicket ticket = new ParkingTicket();
        ticket.setVehicle(vehicle);
        ticket.setSlot(slot);
        ticket.setEntryTime(LocalDateTime.now());

        ParkingTicket savedTicket = ticketRepository.save(ticket);

        return TicketResponse.builder()
                .ticketId(savedTicket.getId())
                .licensePlate(vehicle.getLicensePlate())
                .vehicleType(vehicle.getType().name())
                .entryTime(savedTicket.getEntryTime())
                .slotNumber(slot.getSlotNumber())
                .levelFloor(slot.getLevel().getFloorNumber())
                .build();
    }

    @Transactional
    public CheckOutResponse checkOut(Long ticketId) {
        ParkingTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (ticket.getExitTime() != null) {
            throw new TicketAlreadyClosedException(ticketId);
        }

        ParkingSlot slot = ticket.getSlot();
        slot.setAvailable(true);
        slotRepository.save(slot);

        ticket.setExitTime(LocalDateTime.now());
        BigDecimal fee = pricingService.calculate(
                ticket.getVehicle().getType(),
                ticket.getEntryTime(),
                ticket.getExitTime()
        );
        ticket.setFee(fee);
        ticketRepository.save(ticket);

        return parkingMapper.toCheckOutResponse(ticket);
    }

    public List<TicketResponse> getActiveSessions() {
        return ticketRepository.findAllByExitTimeIsNull().stream()
                .map(parkingMapper::toTicketResponse)
                .toList();
    }

    private boolean isInstanceValid(Vehicle vehicle, VehicleType type) {
        return switch (type) {
            case CAR -> vehicle instanceof Car;
            case TRUCK -> vehicle instanceof Truck;
            case MOTORCYCLE -> vehicle instanceof Motorcycle;
        };
    }

}
