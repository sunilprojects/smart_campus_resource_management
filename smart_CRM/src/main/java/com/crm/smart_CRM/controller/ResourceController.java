package com.crm.smart_CRM.controller;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crm.smart_CRM.Enum.ResourceStatus;
import com.crm.smart_CRM.dto.request.MaintenanceRequest;
import com.crm.smart_CRM.dto.request.ResourceCategoryRequest;
import com.crm.smart_CRM.dto.request.ResourceRequest;
import com.crm.smart_CRM.dto.response.ApiResponse;
import com.crm.smart_CRM.dto.response.ResourceCategoryResponse;
import com.crm.smart_CRM.dto.response.ResourceResponse;
import com.crm.smart_CRM.dto.response.ResourceUtilization;
import com.crm.smart_CRM.service.ResourceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ResourceController {
    
    private final ResourceService resourceService;
    
    // ========== CATEGORY ENDPOINTS ==========
    
    /**
     * Get all categories
     * GET /api/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<ResourceCategoryResponse>>> getAllCategories() {
        log.info("Get all categories request received");
        
        List<ResourceCategoryResponse> categories = resourceService.getAllCategories();
        
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
    }
    
    /**
     * Get category by ID
     * GET /api/categories/{id}
     */
    @GetMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<ResourceCategoryResponse>> getCategoryById(@PathVariable Long id) {
        log.info("Get category by ID request: {}", id);
        
        ResourceCategoryResponse category = resourceService.getCategoryById(id);
        
        return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", category));
    }
    
    /**
     * Create category (Admin only)
     * POST /api/categories
     */
    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<ResourceCategoryResponse>> createCategory(
            @Valid @RequestBody ResourceCategoryRequest request) {
        
        log.info("Create category request: {}", request.getName());
        
        ResourceCategoryResponse category = resourceService.createCategory(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", category));
    }
    
    /**
     * Update category (Admin only)
     * PUT /api/categories/{id}
     */
    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<ResourceCategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody ResourceCategoryRequest request) {
        
        log.info("Update category request for ID: {}", id);
        
        ResourceCategoryResponse category = resourceService.updateCategory(id, request);
        
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", category));
    }
    
    /**
     * Delete category (Admin only)
     * DELETE /api/categories/{id}
     */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable Long id) {
        log.info("Delete category request for ID: {}", id);
        
        resourceService.deleteCategory(id);
        
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }
    
    // ========== RESOURCE ENDPOINTS ==========
    
    /**
     * Get all resources
     * GET /api/resources
     */
    @GetMapping("/resources")
    public ResponseEntity<ApiResponse<List<ResourceResponse>>> getAllResources() {
        log.info("Get all resources request received");
        
        List<ResourceResponse> resources = resourceService.getAllResources();
        
        return ResponseEntity.ok(ApiResponse.success("Resources retrieved successfully", resources));
    }
    
    /**
     * Get resource by ID
     * GET /api/resources/{id}
     */
    @GetMapping("/resources/{id}")
    public ResponseEntity<ApiResponse<ResourceResponse>> getResourceById(@PathVariable Long id) {
        log.info("Get resource by ID request: {}", id);
        
        ResourceResponse resource = resourceService.getResourceById(id);
        
        return ResponseEntity.ok(ApiResponse.success("Resource retrieved successfully", resource));
    }
    
    /**
     * Get resources by category
     * GET /api/resources/category/{categoryId}
     */
    @GetMapping("/resources/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ResourceResponse>>> getResourcesByCategory(
            @PathVariable Long categoryId) {
        
        log.info("Get resources by category request: {}", categoryId);
        
        List<ResourceResponse> resources = resourceService.getResourcesByCategory(categoryId);
        
        return ResponseEntity.ok(ApiResponse.success("Resources retrieved successfully", resources));
    }
    
    /**
     * Get resources by status
     * GET /api/resources/status/{status}
     */
    @GetMapping("/resources/status/{status}")
    public ResponseEntity<ApiResponse<List<ResourceResponse>>> getResourcesByStatus(
            @PathVariable ResourceStatus status) {
        
        log.info("Get resources by status request: {}", status);
        
        List<ResourceResponse> resources = resourceService.getResourcesByStatus(status);
        
        return ResponseEntity.ok(ApiResponse.success("Resources retrieved successfully", resources));
    }
    
    /**
     * Search resources by keyword
     * GET /api/resources/search?keyword={keyword}
     */
    @GetMapping("/resources/search")
    public ResponseEntity<ApiResponse<List<ResourceResponse>>> searchResources(
            @RequestParam String keyword) {
        
        log.info("Search resources request with keyword: {}", keyword);
        
        List<ResourceResponse> resources = resourceService.searchResources(keyword);
        
        return ResponseEntity.ok(ApiResponse.success("Resources retrieved successfully", resources));
    }
    
    /**
     * Create resource (Admin only)
     * POST /api/resources
     */
    @PostMapping("/resources")
    public ResponseEntity<ApiResponse<ResourceResponse>> createResource(
            @Valid @RequestBody ResourceRequest request) {
        
        log.info("Create resource request: {}", request.getName());
        
        ResourceResponse resource = resourceService.createResource(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resource created successfully", resource));
    }
    
    /**
     * Update resource (Admin only)
     * PUT /api/resources/{id}
     */
    @PutMapping("/resources/{id}")
    public ResponseEntity<ApiResponse<ResourceResponse>> updateResource(
            @PathVariable Long id,
            @Valid @RequestBody ResourceRequest request) {
        
        log.info("Update resource request for ID: {}", id);
        
        ResourceResponse resource = resourceService.updateResource(id, request);
        
        return ResponseEntity.ok(ApiResponse.success("Resource updated successfully", resource));
    }
    
    /**
     * Delete resource (Admin only)
     * DELETE /api/resources/{id}
     */
    @DeleteMapping("/resources/{id}")
    public ResponseEntity<ApiResponse<String>> deleteResource(@PathVariable Long id) {
        log.info("Delete resource request for ID: {}", id);
        
        resourceService.deleteResource(id);
        
        return ResponseEntity.ok(ApiResponse.success("Resource deleted successfully", null));
    }
    
    /**
     * Update resource status (Admin only)
     * PUT /api/resources/{id}/status
     */
    @PutMapping("/resources/{id}/status")
    public ResponseEntity<ApiResponse<ResourceResponse>> updateResourceStatus(
            @PathVariable Long id,
            @RequestParam ResourceStatus status) {
        
        log.info("Update resource status request for ID: {} to status: {}", id, status);
        
        ResourceResponse resource = resourceService.updateResourceStatus(id, status);
        
        return ResponseEntity.ok(ApiResponse.success("Resource status updated successfully", resource));
    }
    
    /**
     * Schedule maintenance (Admin only)
     * POST /api/resources/{id}/maintenance
     */
    @PostMapping("/resources/{id}/maintenance")
    public ResponseEntity<ApiResponse<ResourceResponse>> scheduleMaintenance(
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceRequest request) {
        
        log.info("Schedule maintenance request for resource ID: {}", id);
        
        ResourceResponse resource = resourceService.scheduleMaintenance(id, request);
        
        return ResponseEntity.ok(ApiResponse.success("Maintenance scheduled successfully", resource));
    }
    
    /**
     * Get resource count
     * GET /api/resources/count
     */
    @GetMapping("/resources/count")
    public ResponseEntity<ApiResponse<Long>> getResourceCount() {
        log.info("Get resource count request");
        
        Long count = resourceService.getResourceCount();
        
        return ResponseEntity.ok(ApiResponse.success("Resource count retrieved successfully", count));
    }
    
    /**
     * Get resource count by status
     * GET /api/resources/count/status/{status}
     */
    @GetMapping("/resources/count/status/{status}")
    public ResponseEntity<ApiResponse<Long>> getResourceCountByStatus(@PathVariable ResourceStatus status) {
        log.info("Get resource count by status request: {}", status);
        
        Long count = resourceService.getResourceCountByStatus(status);
        
        return ResponseEntity.ok(ApiResponse.success("Resource count retrieved successfully", count));
    }
    
    /**
     * Get top resources by bookings (Admin only)
     * GET /api/resources/top?limit={limit}
     */
    @GetMapping("/resources/top")
    public ResponseEntity<ApiResponse<List<ResourceUtilization>>> getTopResources(
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Get top resources request with limit: {}", limit);
        
        List<ResourceUtilization> topResources = resourceService.getTopResourcesByBookings(limit);
        
        return ResponseEntity.ok(ApiResponse.success("Top resources retrieved successfully", topResources));
    }
}