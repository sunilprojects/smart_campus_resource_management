package com.crm.smart_CRM.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatistics {
    
    private Long totalBookings;
    private Long confirmedBookings;
    private Long completedBookings;
    private Long cancelledBookings;
    private Long noShowBookings;
    private Double averageBookingDuration; // in minutes
    private Map<String, Long> bookingsByDay; // day of week -> count
    private Map<String, Long> bookingsByHour; // hour -> count
}
