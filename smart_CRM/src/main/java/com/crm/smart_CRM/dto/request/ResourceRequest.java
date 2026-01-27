package com.crm.smart_CRM.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceRequest {
    
    @NotBlank(message = "Resource name is required")
    private String name;
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    private String description;
    
    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    private String amenities; // JSON string or comma-separated
    
    private String imageUrl;
    
    private Integer maxBookingDuration = 180; // 3 hours default
    
    private Integer minBookingDuration = 60; // 1 hour default
    
    private Integer advanceBookingDays = 7;
}