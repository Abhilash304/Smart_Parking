package com.example.parking.controller;

import com.example.parking.dto.BookingMapper;
import com.example.parking.dto.BookingResponseDto;
import com.example.parking.exception.BookingException;
import com.example.parking.exception.ResourceNotFoundException;
import com.example.parking.exception.SlotNotAvailableException;
import com.example.parking.model.Bill;
import com.example.parking.model.Booking;
import com.example.parking.model.User;
import com.example.parking.repository.BillRepository;
import com.example.parking.service.BookingService;
import com.example.parking.service.UserService;
import com.example.parking.util.PdfReceiptGenerator;
import com.example.parking.util.QrCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller managing User slot booking requests, Booking Confirmation view, 
 * QR Code Pass downloads, vehicle checkout, and downloadable PDF receipts.
 */
@Controller
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;
    private final UserService userService;
    private final BillRepository billRepository;

    public BookingController(BookingService bookingService,
                             UserService userService,
                             BillRepository billRepository) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.billRepository = billRepository;
    }

    /**
     * Handle Slot Booking Form Submission & Redirect to Booking Confirmation Page.
     */
    @PostMapping("/user/book")
    public String handleBookSlot(@RequestParam(value = "slotId", required = false) Long slotId,
                                 @RequestParam(value = "vehicleNumber", required = false) String vehicleNumber,
                                 RedirectAttributes redirectAttributes) {
        if (slotId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid slot selection. Please select an available slot.");
            return "redirect:/user/dashboard";
        }

        if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vehicle registration number is required.");
            return "redirect:/user/dashboard";
        }

        try {
            User currentUser = userService.getAuthenticatedUser();
            Booking booking = bookingService.bookSlot(slotId, vehicleNumber, currentUser);
            
            redirectAttributes.addFlashAttribute("successMessage", "Booking Confirmed Successfully!");
            redirectAttributes.addFlashAttribute("success", "Booking Confirmed Successfully!");
            return "redirect:/booking/confirmation/" + booking.getId();
        } catch (BookingException | SlotNotAvailableException | ResourceNotFoundException ex) {
            logger.warn("Booking failed: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/user/dashboard";
        } catch (Exception ex) {
            logger.error("Unexpected error during booking: ", ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to process booking. " + ex.getMessage());
            return "redirect:/user/dashboard";
        }
    }

    /**
     * Display Booking Confirmation Page with complete DTO mapping & QR Code.
     */
    @GetMapping("/booking/confirmation/{bookingId}")
    public String showBookingConfirmation(@PathVariable("bookingId") Long bookingId, Model model) {
        User currentUser = userService.getAuthenticatedUser();
        Booking booking = bookingService.getBookingById(bookingId);

        // Security check
        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new BookingException("Unauthorized access to booking confirmation.");
        }

        BookingResponseDto dto = BookingMapper.toDto(booking, null);

        try {
            String qrText = "SMART_PARKING_PASS|" + booking.getBookingReference() 
                    + "|" + booking.getVehicleNumber() 
                    + "|" + booking.getParkingSlot().getSlotNumber();
            String qrCodeBase64 = QrCodeGenerator.generateQrCodeBase64(qrText, 250, 250);
            dto.setQrCodeBase64(qrCodeBase64);
        } catch (Exception e) {
            logger.error("QR Code generation error: ", e);
        }

        model.addAttribute("booking", dto);
        model.addAttribute("rawBooking", booking);
        model.addAttribute("user", currentUser);

        return "booking/confirmation";
    }

    /**
     * Download QR Code Pass PNG Image.
     */
    @GetMapping("/booking/qr/download/{bookingId}")
    public ResponseEntity<byte[]> downloadQrCode(@PathVariable("bookingId") Long bookingId) {
        User currentUser = userService.getAuthenticatedUser();
        Booking booking = bookingService.getBookingById(bookingId);

        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new BookingException("Unauthorized access to QR Code.");
        }

        try {
            String qrText = "SMART_PARKING_PASS|" + booking.getBookingReference() 
                    + "|" + booking.getVehicleNumber() 
                    + "|" + booking.getParkingSlot().getSlotNumber();
            byte[] qrBytes = QrCodeGenerator.generateQrCodeBytes(qrText, 350, 350);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", "qr-pass-" + booking.getBookingReference() + ".png");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(qrBytes);
        } catch (Exception e) {
            logger.error("QR Code download error: ", e);
            throw new BookingException("Failed to generate QR code download image.");
        }
    }

    /**
     * Handle Vehicle Checkout & Bill Processing (Increases Revenue ONLY on Checkout).
     */
    @PostMapping("/user/exit")
    public String handleCheckout(@RequestParam("bookingId") Long bookingId,
                                 RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getAuthenticatedUser();
            Bill bill = bookingService.processCheckout(bookingId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Vehicle Check-Out Successful! Bill Calculated.");
            redirectAttributes.addFlashAttribute("success", "Vehicle Check-Out Successful! Bill Calculated.");
            return "redirect:/booking/receipt/" + bill.getBooking().getId();
        } catch (Exception ex) {
            logger.error("Checkout failed: ", ex);
            redirectAttributes.addFlashAttribute("errorMessage", "Checkout failed: " + ex.getMessage());
            return "redirect:/user/dashboard";
        }
    }

    /**
     * Display Calculated Bill Receipt View.
     */
    @GetMapping("/booking/receipt/{bookingId}")
    public String showReceipt(@PathVariable("bookingId") Long bookingId, Model model) {
        User currentUser = userService.getAuthenticatedUser();
        Booking booking = bookingService.getBookingById(bookingId);

        Bill bill = billRepository.findByBooking(booking)
                .orElseThrow(() -> new BookingException("Bill not found for booking ID " + bookingId));

        BookingResponseDto dto = BookingMapper.toDto(booking, bill);
        model.addAttribute("booking", dto);
        model.addAttribute("rawBooking", booking);
        model.addAttribute("bill", bill);
        model.addAttribute("user", currentUser);

        return "booking/receipt";
    }

    /**
     * Download Professional PDF Billing Receipt with Embedded QR Code.
     */
    @GetMapping("/booking/pdf/{bookingId}")
    public ResponseEntity<byte[]> downloadPdfReceipt(@PathVariable("bookingId") Long bookingId) {
        User currentUser = userService.getAuthenticatedUser();
        Booking booking = bookingService.getBookingById(bookingId);

        Bill bill = billRepository.findByBooking(booking)
                .orElseThrow(() -> new BookingException("Bill not found for booking ID " + bookingId));

        try {
            String qrText = "SMART_PARKING_PASS|" + booking.getBookingReference() 
                    + "|" + booking.getVehicleNumber() 
                    + "|" + booking.getParkingSlot().getSlotNumber();
            byte[] qrBytes = QrCodeGenerator.generateQrCodeBytes(qrText, 150, 150);

            byte[] pdfBytes = PdfReceiptGenerator.generateBillPdf(booking, bill, qrBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "receipt-" + booking.getBookingReference() + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            logger.error("PDF generation error: ", e);
            throw new BookingException("Failed to generate PDF receipt document.");
        }
    }
}
