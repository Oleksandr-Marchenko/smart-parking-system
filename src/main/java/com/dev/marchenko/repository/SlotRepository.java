package com.dev.marchenko.repository;

import com.dev.marchenko.domain.slot.ParkingSlot;
import com.dev.marchenko.domain.slot.SlotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlotRepository extends JpaRepository<ParkingSlot, Long> {

    Optional<ParkingSlot> findFirstByAvailableTrueAndTypeOrderByLevelFloorNumberAsc(SlotType type);

}