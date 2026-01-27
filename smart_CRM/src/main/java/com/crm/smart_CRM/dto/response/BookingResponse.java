package com.crm.smart_CRM.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.crm.smart_CRM.Enum.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
private Long id;
private UserResponse user;
private ResourceResponse resource;
private LocalDate bookingDate;
private LocalTime startTime;
private LocalTime endTime;
private Integer duration;
private String purpose;
private Integer attendeesCount;
private BookingStatus status;
private String cancellationReason;
private LocalDateTime cancelledAt;
private UserResponse cancelledBy;
private LocalDateTime createdAt;
}