package com.example.parking.service;

import com.example.parking.model.*;
import com.example.parking.repository.BookingRepository;
import com.example.parking.repository.ParkingSlotRepository;
import com.example.parking.repository.RoleRepository;
import com.example.parking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Component that guarantees default database roles, admin account, user account, 
 * initial slots, and sample bookings on application startup.
 */
@Component
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ParkingSlotRepository slotRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           ParkingSlotRepository slotRepository,
                           BookingRepository bookingRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initData() {
        logger.info("Initializing system default roles, users, slots, and bookings...");

        // 1. Initialize Roles
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_ADMIN)));

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_USER)));

        // 2. Initialize/Update Admin User (admin@parking.com / admin123)
        User admin;
        Optional<User> adminOpt = userRepository.findByEmail("admin@parking.com");
        if (adminOpt.isEmpty()) {
            admin = new User();
            admin.setName("System Administrator");
            admin.setEmail("admin@parking.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setPhone("9876543210");
            admin.setEnabled(true);
            admin.addRole(adminRole);
            admin = userRepository.save(admin);
            logger.info("Created default Admin account: admin@parking.com / admin123");
        } else {
            admin = adminOpt.get();
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEnabled(true);
            if (admin.getRoles().isEmpty()) {
                admin.addRole(adminRole);
            }
            admin = userRepository.save(admin);
        }

        // 3. Initialize/Update Driver User (user@parking.com / user123)
        User user;
        Optional<User> userOpt = userRepository.findByEmail("user@parking.com");
        if (userOpt.isEmpty()) {
            user = new User();
            user.setName("John Doe");
            user.setEmail("user@parking.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setPhone("9876543211");
            user.setEnabled(true);
            user.addRole(userRole);
            user = userRepository.save(user);
            logger.info("Created default User account: user@parking.com / user123");
        } else {
            user = userOpt.get();
            user.setPassword(passwordEncoder.encode("user123"));
            user.setEnabled(true);
            if (user.getRoles().isEmpty()) {
                user.addRole(userRole);
            }
            user = userRepository.save(user);
        }

        // 4. Initialize Parking Slots with default Rates (₹20/hr CAR, ₹10/hr BIKE, ₹30/hr EV)
        ParkingSlot slotA1, slotA2;
        if (slotRepository.count() == 0) {
            slotA1 = slotRepository.save(new ParkingSlot("A-01", SlotType.CAR, 20.0, "Ground"));
            slotA2 = slotRepository.save(new ParkingSlot("A-02", SlotType.CAR, 20.0, "Ground"));
            slotRepository.save(new ParkingSlot("A-03", SlotType.CAR, 20.0, "Ground"));
            slotRepository.save(new ParkingSlot("B-01", SlotType.BIKE, 10.0, "Ground"));
            slotRepository.save(new ParkingSlot("B-02", SlotType.BIKE, 10.0, "Ground"));
            slotRepository.save(new ParkingSlot("C-01", SlotType.CAR, 20.0, "1st Floor"));
            slotRepository.save(new ParkingSlot("E-01", SlotType.EV, 30.0, "1st Floor"));
            logger.info("Created default initial parking slots with rates.");
        } else {
            slotA1 = slotRepository.findBySlotNumber("A-01").orElseGet(() -> slotRepository.save(new ParkingSlot("A-01", SlotType.CAR, 20.0, "Ground")));
            slotA2 = slotRepository.findBySlotNumber("A-02").orElseGet(() -> slotRepository.save(new ParkingSlot("A-02", SlotType.CAR, 20.0, "Ground")));
            
            slotRepository.findAll().forEach(slot -> {
                if (slot.getHourlyRate() == null || slot.getHourlyRate() <= 0) {
                    slot.setHourlyRate(20.0);
                    slotRepository.save(slot);
                }
            });
        }

        // 5. Seed Sample Active Booking if none exists (so Admin Dashboard reflects live vehicle number plates)
        if (bookingRepository.count() == 0) {
            Booking sampleBooking = new Booking();
            sampleBooking.setBookingReference("SPK-889922-1042");
            sampleBooking.setUser(user);
            sampleBooking.setParkingSlot(slotA1);
            sampleBooking.setVehicleNumber("KA-01-AB-1234");
            sampleBooking.setEntryTime(LocalDateTime.now().minusHours(2));
            sampleBooking.setStatus(BookingStatus.CHECKED_IN);

            slotA1.setStatus(SlotStatus.OCCUPIED);
            slotRepository.save(slotA1);
            bookingRepository.save(sampleBooking);

            logger.info("Seeded initial sample booking with vehicle number plate KA-01-AB-1234.");
        }
    }
}
