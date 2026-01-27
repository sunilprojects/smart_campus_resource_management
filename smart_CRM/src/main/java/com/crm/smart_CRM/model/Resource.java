package com.crm.smart_CRM.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.crm.smart_CRM.Enum.ResourceStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "resources")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resource {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private ResourceCategory category;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Integer capacity;
    
    @Column(nullable = false, length = 100)
    private String location;
    
    @Column(columnDefinition = "TEXT")
    private String amenities; // Stored as JSON string or comma-separated
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private ResourceStatus status = ResourceStatus.AVAILABLE;
    
    @Column(name = "max_booking_duration")
    private Integer maxBookingDuration = 180; // in minutes (3 hours)
    
    @Column(name = "min_booking_duration")
    private Integer minBookingDuration = 60; // in minutes (1 hour)
    
    @Column(name = "advance_booking_days")
    private Integer advanceBookingDays = 7;
    
    @Column(name = "maintenance_start")
    private LocalDateTime maintenanceStart;
    
    @Column(name = "maintenance_end")
    private LocalDateTime maintenanceEnd;
    
    @Column(name = "maintenance_reason", columnDefinition = "TEXT")
    private String maintenanceReason;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}