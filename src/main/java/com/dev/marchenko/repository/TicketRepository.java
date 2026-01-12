package com.dev.marchenko.repository;

import com.dev.marchenko.domain.ticket.ParkingTicket;
import com.dev.marchenko.domain.vehicle.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<ParkingTicket, Long> {

    boolean existsByVehicleAndExitTimeIsNull(Vehicle vehicle);

    List<ParkingTicket> findAllByExitTimeIsNull();

    boolean existsBySlotIdAndExitTimeIsNull(Long slotId);
}
