package com.crm.smart_CRM.service;


import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crm.smart_CRM.Enum.BookingStatus;
import com.crm.smart_CRM.Enum.ResourceStatus;
import com.crm.smart_CRM.Enum.SlotStatus;
import com.crm.smart_CRM.Enum.UserRole;
import com.crm.smart_CRM.dto.request.BookingCancellationRequest;
import com.crm.smart_CRM.dto.request.BookingRequest;
import com.crm.smart_CRM.dto.response.AvailableSlotsResponse;
import com.crm.smart_CRM.dto.response.BookingResponse;
import com.crm.smart_CRM.dto.response.BookingStatistics;
import com.crm.smart_CRM.dto.response.ResourceResponse;
import com.crm.smart_CRM.dto.response.TimeSlot;
import com.crm.smart_CRM.dto.response.UserResponse;
import com.crm.smart_CRM.exception.BookingConflictException;
import com.crm.smart_CRM.exception.ResourceNotFoundException;
import com.crm.smart_CRM.model.Booking;
import com.crm.smart_CRM.model.Resource;
import com.crm.smart_CRM.model.User;
import com.crm.smart_CRM.repository.BookingRepository;
import com.crm.smart_CRM.repository.ResourceRepository;
import com.crm.smart_CRM.repository.SystemConfigRepository;
import com.crm.smart_CRM.repository.UserRepository;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final EmailService emailService;
    private final ResourceService resourceService;
    private final UserService userService;
    
    // Constants for booking rules
    private static final int STUDENT_MAX_BOOKINGS = 3;
    private static final int FACULTY_MAX_BOOKINGS = 5;
    private static final int STUDENT_ADVANCE_DAYS = 7;
    private static final int FACULTY_ADVANCE_DAYS = 14;
    private static final int STUDENT_MAX_DURATION = 180; // 3 hours in minutes
    private static final int FACULTY_MAX_DURATION = 360; // 6 hours in minutes
    private static final int STUDENT_CANCEL_HOURS = 2;
    private static final int FACULTY_CANCEL_HOURS = 1;
    
    /**
     * Create a new booking
     */
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Creating booking for user: {} and resource: {}", request.getUserId(), request.getResourceId());
        
        // Fetch user and resource
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        
        // Validate booking
        validateBooking(user, resource, request);
        
        // Calculate duration
        Duration duration = Duration.between(request.getStartTime(), request.getEndTime());
        int durationMinutes = (int) duration.toMinutes();
        
        // Create booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setResource(resource);
        booking.setBookingDate(request.getBookingDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setDuration(durationMinutes);
        booking.setPurpose(request.getPurpose());
        booking.setAttendeesCount(request.getAttendeesCount());
        booking.setStatus(BookingStatus.CONFIRMED);
        
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully with ID: {}", savedBooking.getId());
        
        // Send confirmation email
        try {
            emailService.sendBookingConfirmation(savedBooking);
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email", e);
        }
        
        return mapToBookingResponse(savedBooking);
    }
    
    /**
     * Validate booking request
     */
    private void validateBooking(User user, Resource resource, BookingRequest request) {
        log.debug("Validating booking for user: {} and resource: {}", user.getId(), resource.getId());
        
        // 1. Check resource status
        if (resource.getStatus() != ResourceStatus.AVAILABLE) {
            throw new ValidationException("Resource is not available for booking");
        }
        
        // 2. Check if resource is under maintenance during booking time
        if (resource.getStatus() == ResourceStatus.UNDER_MAINTENANCE) {
            LocalDateTime bookingStart = LocalDateTime.of(request.getBookingDate(), request.getStartTime());
            LocalDateTime bookingEnd = LocalDateTime.of(request.getBookingDate(), request.getEndTime());
            
            if (resource.getMaintenanceStart() != null && resource.getMaintenanceEnd() != null) {
                if (bookingStart.isBefore(resource.getMaintenanceEnd()) && 
                    bookingEnd.isAfter(resource.getMaintenanceStart())) {
                    throw new ValidationException("Resource is under maintenance during selected time");
                }
            }
        }
        
        // 3. Check active bookings limit
        List<BookingStatus> activeStatuses = Arrays.asList(BookingStatus.CONFIRMED);
        Long activeBookingsCount = bookingRepository.countActiveBookingsByUser(user.getId(), activeStatuses);
        
        int maxBookings = getMaxBookingsByRole(user.getRole());
        if (activeBookingsCount >= maxBookings) {
            throw new ValidationException("Maximum active bookings limit reached (" + maxBookings + ")");
        }
        
        // 4. Check advance booking days
        int advanceDays = getAdvanceBookingDays(user.getRole());
        LocalDate maxAdvanceDate = LocalDate.now().plusDays(advanceDays);
        if (request.getBookingDate().isAfter(maxAdvanceDate)) {
            throw new ValidationException("Cannot book more than " + advanceDays + " days in advance");
        }
        
        // 5. Check booking date is not in the past
        if (request.getBookingDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Cannot book for past dates");
        }
        
        // 6. Check booking duration
        Duration duration = Duration.between(request.getStartTime(), request.getEndTime());
        int durationMinutes = (int) duration.toMinutes();
        
        if (durationMinutes < resource.getMinBookingDuration()) {
            throw new ValidationException("Booking duration must be at least " + 
                    resource.getMinBookingDuration() + " minutes");
        }
        
        int maxDuration = getMaxBookingDuration(user.getRole(), resource);
        if (durationMinutes > maxDuration) {
            throw new ValidationException("Booking duration cannot exceed " + 
                    (maxDuration / 60) + " hours");
        }
        
        // 7. Check time slot availability (no conflicts)
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                resource.getId(),
                request.getBookingDate(),
                request.getStartTime(),
                request.getEndTime()
        );
        
        if (!conflicts.isEmpty()) {
            throw new BookingConflictException("Selected time slot is already booked");
        }
        
        // 8. Check attendees count
        if (request.getAttendeesCount() != null && request.getAttendeesCount() > resource.getCapacity()) {
            throw new ValidationException("Attendees count (" + request.getAttendeesCount() + 
                    ") exceeds resource capacity (" + resource.getCapacity() + ")");
        }
        
        // 9. Check business hours (8 AM - 8 PM weekdays)
        if (request.getStartTime().isBefore(LocalTime.of(8, 0)) || 
            request.getEndTime().isAfter(LocalTime.of(20, 0))) {
            throw new ValidationException("Bookings must be between 8:00 AM and 8:00 PM");
        }
        
        // 10. Check if it's a weekend (Sunday closed)
        DayOfWeek dayOfWeek = request.getBookingDate().getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            throw new ValidationException("Bookings not allowed on Sundays");
        }
        
        log.debug("Booking validation passed");
    }
    
    /**
     * Get available time slots for a resource on a specific date
     */
    public AvailableSlotsResponse getAvailableSlots(Long resourceId, LocalDate date) {
        log.debug("Getting available slots for resource: {} on date: {}", resourceId, date);
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        
        // Get all confirmed bookings for this resource on this date
        List<Booking> bookings = bookingRepository.findByResourceIdAndDateAndStatus(
                resourceId, date, BookingStatus.CONFIRMED);
        
        // Generate time slots from 8 AM to 8 PM (hourly slots)
        List<TimeSlot> slots = generateTimeSlots();
        
        // Mark slots as booked
        for (Booking booking : bookings) {
            markSlotsAsBooked(slots, booking.getStartTime(), booking.getEndTime());
        }
        
        return new AvailableSlotsResponse(date, resourceId, resource.getName(), slots);
    }
    
    /**
     * Generate time slots for the day (8 AM - 8 PM, hourly)
     */
    private List<TimeSlot> generateTimeSlots() {
        List<TimeSlot> slots = new ArrayList<>();
        for (int hour = 8; hour < 20; hour++) {
            slots.add(new TimeSlot(LocalTime.of(hour, 0), SlotStatus.AVAILABLE));
        }
        return slots;
    }
    
    /**
     * Mark time slots as booked
     */
    private void markSlotsAsBooked(List<TimeSlot> slots, LocalTime startTime, LocalTime endTime) {
        for (TimeSlot slot : slots) {
            if (!slot.getTime().isBefore(startTime) && slot.getTime().isBefore(endTime)) {
                slot.setStatus(SlotStatus.BOOKED);
            }
        }
    }
    
    /**
     * Get user's bookings
     */
    public List<BookingResponse> getUserBookings(Long userId) {
        log.debug("Fetching bookings for user: {}", userId);
        
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        return bookings.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get booking by ID
     */
    public BookingResponse getBookingById(Long id) {
        log.debug("Fetching booking with ID: {}", id);
        
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));
        return mapToBookingResponse(booking);
    }
    
    /**
     * Get all bookings (Admin)
     */
    public List<BookingResponse> getAllBookings() {
        log.debug("Fetching all bookings");
        
        return bookingRepository.findAll().stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Cancel booking
     */
    @Transactional
    public BookingResponse cancelBooking(Long bookingId, BookingCancellationRequest request) {
        log.info("Cancelling booking ID: {}", bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        
        // Check if booking is already cancelled or completed
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ValidationException("Can only cancel confirmed bookings");
        }
        
        // Check cancellation eligibility based on user role
        if (request.getCancelledByUserId() != null) {
            User cancelledBy = userRepository.findById(request.getCancelledByUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            // Admins can cancel anytime
            if (cancelledBy.getRole() != UserRole.ADMIN) {
                // Check if user is the booking owner
                if (!booking.getUser().getId().equals(cancelledBy.getId())) {
                    throw new ValidationException("You can only cancel your own bookings");
                }
                
                // Check cancellation time limit
                validateCancellationTime(booking, cancelledBy.getRole());
            }
            
            booking.setCancelledBy(cancelledBy);
        }
        
        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(request.getCancellationReason());
        booking.setCancelledAt(LocalDateTime.now());
        
        Booking cancelledBooking = bookingRepository.save(booking);
        log.info("Booking cancelled successfully");
        
        // Send cancellation email
        try {
            emailService.sendCancellationEmail(cancelledBooking);
        } catch (Exception e) {
            log.error("Failed to send cancellation email", e);
        }
        
        return mapToBookingResponse(cancelledBooking);
    }
    
    /**
     * Validate if booking can be cancelled based on time
     */
    private void validateCancellationTime(Booking booking, UserRole role) {
        LocalDateTime bookingStart = LocalDateTime.of(booking.getBookingDate(), booking.getStartTime());
        LocalDateTime now = LocalDateTime.now();
        
        int requiredHours = role == UserRole.STUDENT ? STUDENT_CANCEL_HOURS : FACULTY_CANCEL_HOURS;
        LocalDateTime cancelDeadline = bookingStart.minusHours(requiredHours);
        
        if (now.isAfter(cancelDeadline)) {
            throw new ValidationException("Must cancel at least " + requiredHours + " hours before start time");
        }
    }
    
    /**
     * Check booking availability
     */
    public boolean checkAvailability(Long resourceId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                resourceId, date, startTime, endTime);
        return conflicts.isEmpty();
    }
    
    /**
     * Get booking statistics
     */
    public BookingStatistics getBookingStatistics() {
        log.debug("Calculating booking statistics");
        
        Long totalBookings = bookingRepository.count();
        Long confirmedBookings = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        Long completedBookings = bookingRepository.countByStatus(BookingStatus.COMPLETED);
        Long cancelledBookings = bookingRepository.countByStatus(BookingStatus.CANCELLED);
        Long noShowBookings = bookingRepository.countByStatus(BookingStatus.NO_SHOW);
        
        // Calculate average booking duration
        List<Booking> allBookings = bookingRepository.findAll();
        Double avgDuration = allBookings.stream()
                .filter(b -> b.getDuration() != null)
                .mapToInt(Booking::getDuration)
                .average()
                .orElse(0.0);
        
        BookingStatistics stats = new BookingStatistics();
        stats.setTotalBookings(totalBookings);
        stats.setConfirmedBookings(confirmedBookings);
        stats.setCompletedBookings(completedBookings);
        stats.setCancelledBookings(cancelledBookings);
        stats.setNoShowBookings(noShowBookings);
        stats.setAverageBookingDuration(avgDuration);
        
        return stats;
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Get max bookings allowed by role
     */
    private int getMaxBookingsByRole(UserRole role) {
        return switch (role) {
            case STUDENT -> STUDENT_MAX_BOOKINGS;
            case FACULTY -> FACULTY_MAX_BOOKINGS;
            case ADMIN -> Integer.MAX_VALUE; // No limit for admins
        };
    }
    
    /**
     * Get advance booking days by role
     */
    private int getAdvanceBookingDays(UserRole role) {
        return switch (role) {
            case STUDENT -> STUDENT_ADVANCE_DAYS;
            case FACULTY -> FACULTY_ADVANCE_DAYS;
            case ADMIN -> Integer.MAX_VALUE; // No limit for admins
        };
    }
    
    /**
     * Get max booking duration by role
     */
    private int getMaxBookingDuration(UserRole role, Resource resource) {
        int roleDuration = switch (role) {
            case STUDENT -> STUDENT_MAX_DURATION;
            case FACULTY -> FACULTY_MAX_DURATION;
            case ADMIN -> Integer.MAX_VALUE;
        };
        
        // Take minimum of role duration and resource max duration
        return Math.min(roleDuration, resource.getMaxBookingDuration());
    }
    
    /**
     * Map Booking entity to BookingResponse DTO
     */
    private BookingResponse mapToBookingResponse(Booking booking) {
        UserResponse userResponse = userService.getUserById(booking.getUser().getId());
        ResourceResponse resourceResponse = resourceService.getResourceById(booking.getResource().getId());
        
        UserResponse cancelledByResponse = null;
        if (booking.getCancelledBy() != null) {
            cancelledByResponse = userService.getUserById(booking.getCancelledBy().getId());
        }
        
        return new BookingResponse(
                booking.getId(),
                userResponse,
                resourceResponse,
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getDuration(),
                booking.getPurpose(),
                booking.getAttendeesCount(),
                booking.getStatus(),
                booking.getCancellationReason(),
                booking.getCancelledAt(),
                cancelledByResponse,
                booking.getCreatedAt()
        );
    }
}