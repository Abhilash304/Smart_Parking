package com.example.parking.repository;

import com.example.parking.model.ParkingSlot;
import com.example.parking.model.SlotStatus;
import com.example.parking.model.SlotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for ParkingSlot entity.
 */
@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {

    Optional<ParkingSlot> findBySlotNumber(String slotNumber);

    boolean existsBySlotNumber(String slotNumber);

    List<ParkingSlot> findByStatus(SlotStatus status);

    List<ParkingSlot> findBySlotTypeAndStatus(SlotType slotType, SlotStatus status);

    long countByStatus(SlotStatus status);
}
