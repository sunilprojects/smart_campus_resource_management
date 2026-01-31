package com.crm.smart_CRM.dto.request;


import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Resource ID is required")
    private Long resourceId;
    
    @NotNull(message = "Booking date is required")
    @FutureOrPresent(message = "Booking date must be today or in the future")
    private LocalDate bookingDate;
    
//    @NotNull(message = "Start time is required")
//    private LocalTime startTime;
    
    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string", example = "10:30")
    private LocalTime startTime;

    
//    @NotNull(message = "End time is required")
//    private LocalTime endTime;
    
    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string", example = "11:30")
    private LocalTime endTime;
    
    @NotBlank(message = "Purpose is required")
    @Size(min = 10, max = 255, message = "Purpose must be between 10 and 255 characters")
    private String purpose;
    
    @Min(value = 1, message = "Attendees count must be at least 1")
    private Integer attendeesCount;
}