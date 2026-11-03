package com.example.parking.repository;

import com.example.parking.model.Booking;
import com.example.parking.model.BookingStatus;
import com.example.parking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Booking entity with optimized JOIN FETCH queries.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByUserOrderByCreatedAtDesc(User user);

    List<Booking> findByStatus(BookingStatus status);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.user LEFT JOIN FETCH b.parkingSlot WHERE b.user = :user AND b.status IN ('BOOKED', 'CHECKED_IN', 'ACTIVE', 'RESERVED')")
    Optional<Booking> findActiveBookingByUser(@Param("user") User user);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.user LEFT JOIN FETCH b.parkingSlot ORDER BY b.createdAt DESC")
    List<Booking> findAllWithDetails();

    long countByStatus(BookingStatus status);
}
