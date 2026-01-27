package com.crm.smart_CRM.controller;



import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crm.smart_CRM.dto.request.BookingCancellationRequest;
import com.crm.smart_CRM.dto.request.BookingRequest;
import com.crm.smart_CRM.dto.response.ApiResponse;
import com.crm.smart_CRM.dto.response.AvailableSlotsResponse;
import com.crm.smart_CRM.dto.response.BookingResponse;
import com.crm.smart_CRM.dto.response.BookingStatistics;
import com.crm.smart_CRM.service.BookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BookingController {
    
    private final BookingService bookingService;
    
    /**
     * Create a new booking
     * POST /api/bookings
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request) {
        
        log.info("Create booking request received for user: {} and resource: {}", 
                request.getUserId(), request.getResourceId());
        
        BookingResponse booking = bookingService.createBooking(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully", booking));
    }
    
    /**
     * Get available time slots for a resource
     * GET /api/bookings/available-slots?resourceId={id}&date={date}
     */
    @GetMapping("/available-slots")
    public ResponseEntity<ApiResponse<AvailableSlotsResponse>> getAvailableSlots(
            @RequestParam Long resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Get available slots request for resource: {} on date: {}", resourceId, date);
        
        AvailableSlotsResponse slots = bookingService.getAvailableSlots(resourceId, date);
        
        return ResponseEntity.ok(ApiResponse.success("Available slots retrieved successfully", slots));
    }
    
    /**
     * Check booking availability
     * GET /api/bookings/check-availability?resourceId={id}&date={date}&startTime={time}&endTime={time}
     */
    @GetMapping("/check-availability")
    public ResponseEntity<ApiResponse<Boolean>> checkAvailability(
            @RequestParam Long resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        
        log.info("Check availability request for resource: {} on date: {} from {} to {}", 
                resourceId, date, startTime, endTime);
        
        boolean available = bookingService.checkAvailability(resourceId, date, startTime, endTime);
        
        return ResponseEntity.ok(ApiResponse.success(
                available ? "Slot is available" : "Slot is not available", 
                available));
    }
    
    /**
     * Get user's bookings
     * GET /api/bookings/my-bookings?userId={id}
     */
    @GetMapping("/my-bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getUserBookings(
            @RequestParam Long userId) {
        
        log.info("Get user bookings request for user ID: {}", userId);
        
        List<BookingResponse> bookings = bookingService.getUserBookings(userId);
        
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved successfully", bookings));
    }
    
    /**
     * Get all bookings (Admin only)
     * GET /api/bookings
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookings() {
        log.info("Get all bookings request received");
        
        List<BookingResponse> bookings = bookingService.getAllBookings();
        
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved successfully", bookings));
    }
    
    /**
     * Get booking by ID
     * GET /api/bookings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable Long id) {
        log.info("Get booking by ID request: {}", id);
        
        BookingResponse booking = bookingService.getBookingById(id);
        
        return ResponseEntity.ok(ApiResponse.success("Booking retrieved successfully", booking));
    }
    
    /**
     * Cancel booking
     * PUT /api/bookings/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            @RequestBody BookingCancellationRequest request) {
        
        log.info("Cancel booking request for ID: {}", id);
        
        BookingResponse booking = bookingService.cancelBooking(id, request);
        
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", booking));
    }
    
    /**
     * Get booking statistics (Admin only)
     * GET /api/bookings/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<BookingStatistics>> getBookingStatistics() {
        log.info("Get booking statistics request");
        
        BookingStatistics statistics = bookingService.getBookingStatistics();
        
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", statistics));
    }
}
