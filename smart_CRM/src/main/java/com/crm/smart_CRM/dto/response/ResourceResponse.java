package com.crm.smart_CRM.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.crm.smart_CRM.Enum.ResourceStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceResponse {
    
    private Long id;
    private String name;
    private ResourceCategoryResponse category;
    private String description;
    private Integer capacity;
    private String location;
    private String amenities;
    private String imageUrl;
    private ResourceStatus status;
    private Integer maxBookingDuration;
    private Integer minBookingDuration;
    private Integer advanceBookingDays;
    private LocalDateTime maintenanceStart;
    private LocalDateTime maintenanceEnd;
    private String maintenanceReason;
    private Double averageRating;
    private Integer totalReviews;
    private LocalDateTime createdAt;
}