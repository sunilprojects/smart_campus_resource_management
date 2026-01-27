package com.crm.smart_CRM.service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crm.smart_CRM.Enum.BookingStatus;
import com.crm.smart_CRM.Enum.ResourceStatus;
import com.crm.smart_CRM.dto.request.MaintenanceRequest;
import com.crm.smart_CRM.dto.request.ResourceCategoryRequest;
import com.crm.smart_CRM.dto.request.ResourceRequest;
import com.crm.smart_CRM.dto.response.ResourceCategoryResponse;
import com.crm.smart_CRM.dto.response.ResourceResponse;
import com.crm.smart_CRM.dto.response.ResourceUtilization;
import com.crm.smart_CRM.exception.ResourceNotFoundException;
import com.crm.smart_CRM.model.Booking;
import com.crm.smart_CRM.model.Resource;
import com.crm.smart_CRM.model.ResourceCategory;
import com.crm.smart_CRM.repository.BookingRepository;
import com.crm.smart_CRM.repository.ResourceCategoryRepository;
import com.crm.smart_CRM.repository.ResourceRepository;
import com.crm.smart_CRM.repository.ReviewRepository;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceService {
    
    private final ResourceRepository resourceRepository;
    private final ResourceCategoryRepository categoryRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final EmailService emailService;
    
    // ========== CATEGORY METHODS ==========
    
    /**
     * Create a new resource category
     */
    @Transactional
    public ResourceCategoryResponse createCategory(ResourceCategoryRequest request) {
        log.info("Creating new category: {}", request.getName());
        
        if (categoryRepository.existsByName(request.getName())) {
            throw new ValidationException("Category already exists with name: " + request.getName());
        }
        
        ResourceCategory category = new ResourceCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        
        ResourceCategory savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());
        
        return mapToCategoryResponse(savedCategory);
    }
    
    /**
     * Get all categories
     */
    public List<ResourceCategoryResponse> getAllCategories() {
        log.debug("Fetching all categories");
        return categoryRepository.findAll().stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get category by ID
     */
    public ResourceCategoryResponse getCategoryById(Long id) {
        log.debug("Fetching category with ID: {}", id);
        ResourceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        return mapToCategoryResponse(category);
    }
    
    /**
     * Update category
     */
    @Transactional
    public ResourceCategoryResponse updateCategory(Long id, ResourceCategoryRequest request) {
        log.info("Updating category ID: {}", id);
        
        ResourceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        
        ResourceCategory updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully");
        
        return mapToCategoryResponse(updatedCategory);
    }
    
    /**
     * Delete category
     */
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category ID: {}", id);
        
        ResourceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        
        // Check if any resources use this category
        List<Resource> resources = resourceRepository.findByCategory(category);
        if (!resources.isEmpty()) {
            throw new ValidationException("Cannot delete category. " + resources.size() + " resources are using this category.");
        }
        
        categoryRepository.delete(category);
        log.info("Category deleted successfully");
    }
    
    // ========== RESOURCE METHODS ==========
    
    /**
     * Create a new resource
     */
    @Transactional
    public ResourceResponse createResource(ResourceRequest request) {
        log.info("Creating new resource: {}", request.getName());
        
        ResourceCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));
        
        Resource resource = new Resource();
        resource.setName(request.getName());
        resource.setCategory(category);
        resource.setDescription(request.getDescription());
        resource.setCapacity(request.getCapacity());
        resource.setLocation(request.getLocation());
        resource.setAmenities(request.getAmenities());
        resource.setImageUrl(request.getImageUrl());
        resource.setStatus(ResourceStatus.AVAILABLE);
        resource.setMaxBookingDuration(request.getMaxBookingDuration());
        resource.setMinBookingDuration(request.getMinBookingDuration());
        resource.setAdvanceBookingDays(request.getAdvanceBookingDays());
        
        Resource savedResource = resourceRepository.save(resource);
        log.info("Resource created successfully with ID: {}", savedResource.getId());
        
        return mapToResourceResponse(savedResource);
    }
    
    /**
     * Get all resources
     */
    public List<ResourceResponse> getAllResources() {
        log.debug("Fetching all resources");
        return resourceRepository.findAll().stream()
                .map(this::mapToResourceResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get resource by ID
     */
    public ResourceResponse getResourceById(Long id) {
        log.debug("Fetching resource with ID: {}", id);
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + id));
        return mapToResourceResponse(resource);
    }
    
    /**
     * Get resources by category
     */
    public List<ResourceResponse> getResourcesByCategory(Long categoryId) {
        log.debug("Fetching resources for category ID: {}", categoryId);
        
        ResourceCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        
        return resourceRepository.findByCategory(category).stream()
                .map(this::mapToResourceResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get resources by status
     */
    public List<ResourceResponse> getResourcesByStatus(ResourceStatus status) {
        log.debug("Fetching resources with status: {}", status);
        return resourceRepository.findByStatus(status).stream()
                .map(this::mapToResourceResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Search resources by keyword
     */
    public List<ResourceResponse> searchResources(String keyword) {
        log.debug("Searching resources with keyword: {}", keyword);
        return resourceRepository.findByNameContainingIgnoreCase(keyword).stream()
                .map(this::mapToResourceResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Update resource
     */
    @Transactional
    public ResourceResponse updateResource(Long id, ResourceRequest request) {
        log.info("Updating resource ID: {}", id);
        
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        
        ResourceCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        
        resource.setName(request.getName());
        resource.setCategory(category);
        resource.setDescription(request.getDescription());
        resource.setCapacity(request.getCapacity());
        resource.setLocation(request.getLocation());
        resource.setAmenities(request.getAmenities());
        resource.setImageUrl(request.getImageUrl());
        resource.setMaxBookingDuration(request.getMaxBookingDuration());
        resource.setMinBookingDuration(request.getMinBookingDuration());
        resource.setAdvanceBookingDays(request.getAdvanceBookingDays());
        
        Resource updatedResource = resourceRepository.save(resource);
        log.info("Resource updated successfully");
        
        return mapToResourceResponse(updatedResource);
    }
    
    /**
     * Delete resource (soft delete)
     */
    @Transactional
    public void deleteResource(Long id) {
        log.info("Deleting resource ID: {}", id);
        
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        
        // Check if there are any active bookings
        List<Booking> activeBookings = bookingRepository.findByResourceIdAndStatus(id, BookingStatus.CONFIRMED);
        if (!activeBookings.isEmpty()) {
            throw new ValidationException("Cannot delete resource. " + activeBookings.size() + " active bookings exist.");
        }
        
        // Soft delete - change status to UNAVAILABLE
        resource.setStatus(ResourceStatus.UNAVAILABLE);
        resourceRepository.save(resource);
        
        log.info("Resource deleted (status changed to UNAVAILABLE)");
    }
    
    /**
     * Update resource status
     */
    @Transactional
    public ResourceResponse updateResourceStatus(Long id, ResourceStatus status) {
        log.info("Updating resource ID: {} status to {}", id, status);
        
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        
        resource.setStatus(status);
        Resource updatedResource = resourceRepository.save(resource);
        
        log.info("Resource status updated successfully");
        return mapToResourceResponse(updatedResource);
    }
    
    /**
     * Schedule maintenance for resource
     */
    @Transactional
    public ResourceResponse scheduleMaintenance(Long id, MaintenanceRequest request) {
        log.info("Scheduling maintenance for resource ID: {}", id);
        
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        
        // Validate maintenance dates
        if (request.getMaintenanceEnd().isBefore(request.getMaintenanceStart())) {
            throw new ValidationException("Maintenance end time must be after start time");
        }
        
        resource.setMaintenanceStart(request.getMaintenanceStart());
        resource.setMaintenanceEnd(request.getMaintenanceEnd());
        resource.setMaintenanceReason(request.getMaintenanceReason());
        resource.setStatus(ResourceStatus.UNDER_MAINTENANCE);
        
        Resource updatedResource = resourceRepository.save(resource);
        
        // Cancel conflicting bookings and notify users
        cancelConflictingBookingsForMaintenance(resource, request);
        
        log.info("Maintenance scheduled successfully");
        return mapToResourceResponse(updatedResource);
    }
    
    /**
     * Cancel bookings that conflict with maintenance period
     */
    private void cancelConflictingBookingsForMaintenance(Resource resource, MaintenanceRequest request) {
        log.info("Cancelling conflicting bookings for maintenance");
        
        List<Booking> bookings = bookingRepository.findByResourceIdAndStatus(resource.getId(), BookingStatus.CONFIRMED);
        
        int cancelledCount = 0;
        for (Booking booking : bookings) {
            LocalDateTime bookingStart = LocalDateTime.of(booking.getBookingDate(), booking.getStartTime());
            LocalDateTime bookingEnd = LocalDateTime.of(booking.getBookingDate(), booking.getEndTime());
            
            // Check if booking overlaps with maintenance
            if (bookingStart.isBefore(request.getMaintenanceEnd()) && 
                bookingEnd.isAfter(request.getMaintenanceStart())) {
                
                booking.setStatus(BookingStatus.CANCELLED);
                booking.setCancellationReason("Resource scheduled for maintenance: " + request.getMaintenanceReason());
                booking.setCancelledAt(LocalDateTime.now());
                bookingRepository.save(booking);
                
                // Send maintenance notification email
                try {
                    emailService.sendMaintenanceNotification(booking, resource);
                } catch (Exception e) {
                    log.error("Failed to send maintenance notification for booking ID: {}", booking.getId(), e);
                }
                
                cancelledCount++;
            }
        }
        
        log.info("Cancelled {} bookings due to maintenance", cancelledCount);
    }
    
    /**
     * Get resource count
     */
    public Long getResourceCount() {
        return resourceRepository.count();
    }
    
    /**
     * Get resource count by status
     */
    public Long getResourceCountByStatus(ResourceStatus status) {
        return resourceRepository.countByStatus(status);
    }
    
    /**
     * Get top resources by bookings
     */
    public List<ResourceUtilization> getTopResourcesByBookings(int limit) {
        log.debug("Fetching top {} resources by bookings", limit);
        
        List<Resource> allResources = resourceRepository.findAll();
        List<ResourceUtilization> utilizationList = new ArrayList<>();
        
        for (Resource resource : allResources) {
            Long totalBookings = bookingRepository.countCompletedBookingsByResource(resource.getId());
            
            // Calculate utilization percentage (simplified)
            Double utilization = totalBookings * 5.0; // Simplified calculation
            
            utilizationList.add(new ResourceUtilization(
                    resource.getId(),
                    resource.getName(),
                    totalBookings,
                    Math.min(utilization, 100.0)
            ));
        }
        
        // Sort by total bookings and limit
        return utilizationList.stream()
                .sorted((a, b) -> Long.compare(b.getTotalBookings(), a.getTotalBookings()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Map ResourceCategory entity to response DTO
     */
    private ResourceCategoryResponse mapToCategoryResponse(ResourceCategory category) {
        return new ResourceCategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getIcon(),
                category.getCreatedAt()
        );
    }
    
    /**
     * Map Resource entity to response DTO
     */
    private ResourceResponse mapToResourceResponse(Resource resource) {
        Double avgRating = reviewRepository.getAverageRatingByResource(resource.getId());
        Long totalReviews = reviewRepository.countByResourceId(resource.getId());
        
        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                mapToCategoryResponse(resource.getCategory()),
                resource.getDescription(),
                resource.getCapacity(),
                resource.getLocation(),
                resource.getAmenities(),
                resource.getImageUrl(),
                resource.getStatus(),
                resource.getMaxBookingDuration(),
                resource.getMinBookingDuration(),
                resource.getAdvanceBookingDays(),
                resource.getMaintenanceStart(),
                resource.getMaintenanceEnd(),
                resource.getMaintenanceReason(),
                avgRating != null ? avgRating : 0.0,
                totalReviews.intValue(),
                resource.getCreatedAt()
        );
    }
}
