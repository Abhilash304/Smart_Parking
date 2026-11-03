package com.example.parking.service;

import com.example.parking.dto.BookingMapper;
import com.example.parking.dto.BookingResponseDto;
import com.example.parking.exception.BookingException;
import com.example.parking.exception.ResourceNotFoundException;
import com.example.parking.exception.SlotNotAvailableException;
import com.example.parking.model.*;
import com.example.parking.repository.BillRepository;
import com.example.parking.repository.BookingRepository;
import com.example.parking.repository.ParkingSlotRepository;
import com.example.parking.util.QrCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for managing vehicle slot bookings, QR Code generation, checkout calculations, and billing receipts.
 * Uses constructor injection.
 */
@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final ParkingSlotRepository slotRepository;
    private final BillRepository billRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                               ParkingSlotRepository slotRepository,
                               BillRepository billRepository) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
        this.billRepository = billRepository;
    }

    @Override
    @Transactional
    public Booking bookSlot(Long slotId, String vehicleNumber, User user) {
        // Enforce rule: If user already has an active booking, return it instead of throwing an error!
        Optional<Booking> existingActive = bookingRepository.findActiveBookingByUser(user);
        if (existingActive.isPresent()) {
            logger.info("User {} already has an active booking. Returning existing booking.", user.getEmail());
            return existingActive.get();
        }

        // Fetch parking slot entity
        ParkingSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("ParkingSlot", "id", slotId));

        // Check if slot is available
        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new SlotNotAvailableException("Parking slot '" + slot.getSlotNumber() + "' is currently " + slot.getStatus() + ".");
        }

        // Clean & format vehicle number plate
        String formattedVehicleNumber = vehicleNumber.trim().replaceAll("\\s+", "").toUpperCase();

        // Generate unique booking reference number
        String bookingRef = "SPK-" + System.currentTimeMillis() % 1000000 + "-" + (1000 + (int)(Math.random() * 9000));

        // Create booking instance
        Booking booking = new Booking();
        booking.setBookingReference(bookingRef);
        booking.setUser(user);
        booking.setParkingSlot(slot);
        booking.setVehicleNumber(formattedVehicleNumber);
        booking.setEntryTime(LocalDateTime.now());
        booking.setStatus(BookingStatus.CHECKED_IN);

        // Generate QR Code data
        try {
            String qrData = "SMART_PARKING_PASS|" + bookingRef + "|" + formattedVehicleNumber + "|" + slot.getSlotNumber();
            String qrBase64 = QrCodeGenerator.generateQrCodeBase64(qrData, 250, 250);
            booking.setQrCodePath(qrBase64);
        } catch (Exception e) {
            logger.warn("QR code generation warning during booking: {}", e.getMessage());
        }

        // Update slot status to OCCUPIED
        slot.setStatus(SlotStatus.OCCUPIED);
        slotRepository.save(slot);

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public BookingResponseDto reserveSlot(Long slotId, String vehicleNumber, User user) {
        Booking booking = bookSlot(slotId, vehicleNumber, user);
        return BookingMapper.toDto(booking, null);
    }

    @Override
    @Transactional
    public Bill processCheckout(Long bookingId, User user) {
        Booking booking = getBookingById(bookingId);

        // Security check: Ensure booking belongs to user (or admin)
        if (!booking.getUser().getId().equals(user.getId()) && user.getRoles().stream().noneMatch(r -> r.getName() == RoleName.ROLE_ADMIN)) {
            throw new BookingException("You are not authorized to check out this booking.");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            return billRepository.findByBooking(booking)
                    .orElseThrow(() -> new ResourceNotFoundException("Bill", "bookingId", bookingId));
        }

        LocalDateTime exitTime = LocalDateTime.now();
        booking.setExitTime(exitTime);
        booking.setStatus(BookingStatus.COMPLETED);

        // Calculate parking duration (minimum 1 hour, rounded up to nearest hour)
        Duration duration = Duration.between(booking.getEntryTime(), exitTime);
        long minutes = duration.toMinutes();
        int hours = (int) Math.ceil((double) Math.max(minutes, 1) / 60.0);

        Double hourlyRate = booking.getParkingSlot().getHourlyRate() != null ? booking.getParkingSlot().getHourlyRate() : 20.0;
        Double totalAmount = hours * hourlyRate;

        // Save Bill record (this increases revenue in BillRepository)
        Bill bill = new Bill(booking, hours, hourlyRate, totalAmount);
        billRepository.save(bill);

        // Release Parking Slot status back to AVAILABLE
        ParkingSlot slot = booking.getParkingSlot();
        slot.setStatus(SlotStatus.AVAILABLE);
        slotRepository.save(slot);

        bookingRepository.save(booking);

        return bill;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Booking> getActiveBooking(User user) {
        return bookingRepository.findActiveBookingByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getUserBookingHistory(User user) {
        return bookingRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBookingByReference(String reference) {
        return bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "reference", reference));
    }
}
