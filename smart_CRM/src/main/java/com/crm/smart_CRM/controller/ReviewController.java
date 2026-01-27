package com.crm.smart_CRM.controller;


import java.util.List;
import java.util.Map;

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

import com.crm.smart_CRM.dto.request.ReviewRequest;
import com.crm.smart_CRM.dto.request.ReviewUpdateRequest;
import com.crm.smart_CRM.dto.response.ApiResponse;
import com.crm.smart_CRM.dto.response.ReviewResponse;
import com.crm.smart_CRM.service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReviewController {
    
    private final ReviewService reviewService;
    
    /**
     * Submit a new review
     * POST /api/reviews
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> submitReview(
            @Valid @RequestBody ReviewRequest request) {
        
        log.info("Submit review request for resource: {} by user: {}", 
                request.getResourceId(), request.getUserId());
        
        ReviewResponse review = reviewService.submitReview(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted successfully", review));
    }
    
    /**
     * Get all reviews for a resource
     * GET /api/reviews/resource/{resourceId}
     */
    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByResource(
            @PathVariable Long resourceId) {
        
        log.info("Get reviews for resource ID: {}", resourceId);
        
        List<ReviewResponse> reviews = reviewService.getReviewsByResource(resourceId);
        
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved successfully", reviews));
    }
    
    /**
     * Get recent reviews for a resource
     * GET /api/reviews/resource/{resourceId}/recent?limit={limit}
     */
    @GetMapping("/resource/{resourceId}/recent")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getRecentReviewsByResource(
            @PathVariable Long resourceId,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Get recent {} reviews for resource ID: {}", limit, resourceId);
        
        List<ReviewResponse> reviews = reviewService.getRecentReviewsByResource(resourceId, limit);
        
        return ResponseEntity.ok(ApiResponse.success("Recent reviews retrieved successfully", reviews));
    }
    
    /**
     * Get all reviews by a user
     * GET /api/reviews/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByUser(
            @PathVariable Long userId) {
        
        log.info("Get reviews by user ID: {}", userId);
        
        List<ReviewResponse> reviews = reviewService.getReviewsByUser(userId);
        
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved successfully", reviews));
    }
    
    /**
     * Get my reviews
     * GET /api/reviews/my-reviews?userId={userId}
     */
    @GetMapping("/my-reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviews(
            @RequestParam Long userId) {
        
        log.info("Get my reviews request for user ID: {}", userId);
        
        List<ReviewResponse> reviews = reviewService.getReviewsByUser(userId);
        
        return ResponseEntity.ok(ApiResponse.success("Your reviews retrieved successfully", reviews));
    }
    
    /**
     * Get review by ID
     * GET /api/reviews/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(@PathVariable Long id) {
        log.info("Get review by ID request: {}", id);
        
        ReviewResponse review = reviewService.getReviewById(id);
        
        return ResponseEntity.ok(ApiResponse.success("Review retrieved successfully", review));
    }
    
    /**
     * Update review (user's own review)
     * PUT /api/reviews/{id}?userId={userId}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long id,
            @RequestParam Long userId,
            @Valid @RequestBody ReviewUpdateRequest request) {
        
        log.info("Update review request for ID: {} by user: {}", id, userId);
        
        ReviewResponse review = reviewService.updateReview(id, userId, request);
        
        return ResponseEntity.ok(ApiResponse.success("Review updated successfully", review));
    }
    
    /**
     * Delete review
     * DELETE /api/reviews/{id}?userId={userId}&isAdmin={true/false}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteReview(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "false") boolean isAdmin) {
        
        log.info("Delete review request for ID: {} by user: {}, isAdmin: {}", id, userId, isAdmin);
        
        reviewService.deleteReview(id, userId, isAdmin);
        
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
    }
    
    /**
     * Get average rating for a resource
     * GET /api/reviews/resource/{resourceId}/average-rating
     */
    @GetMapping("/resource/{resourceId}/average-rating")
    public ResponseEntity<ApiResponse<Double>> getAverageRating(@PathVariable Long resourceId) {
        log.info("Get average rating for resource ID: {}", resourceId);
        
        Double avgRating = reviewService.getAverageRating(resourceId);
        
        return ResponseEntity.ok(ApiResponse.success("Average rating retrieved successfully", avgRating));
    }
    
    /**
     * Get rating distribution for a resource
     * GET /api/reviews/resource/{resourceId}/rating-distribution
     */
    @GetMapping("/resource/{resourceId}/rating-distribution")
    public ResponseEntity<ApiResponse<Map<Integer, Long>>> getRatingDistribution(
            @PathVariable Long resourceId) {
        
        log.info("Get rating distribution for resource ID: {}", resourceId);
        
        Map<Integer, Long> distribution = reviewService.getRatingDistribution(resourceId);
        
        return ResponseEntity.ok(ApiResponse.success("Rating distribution retrieved successfully", distribution));
    }
    
    /**
     * Get review count for a resource
     * GET /api/reviews/resource/{resourceId}/count
     */
    @GetMapping("/resource/{resourceId}/count")
    public ResponseEntity<ApiResponse<Long>> getReviewCount(@PathVariable Long resourceId) {
        log.info("Get review count for resource ID: {}", resourceId);
        
        Long count = reviewService.getReviewCount(resourceId);
        
        return ResponseEntity.ok(ApiResponse.success("Review count retrieved successfully", count));
    }
    
    /**
     * Check if user has reviewed a booking
     * GET /api/reviews/booking/{bookingId}/has-review
     */
    @GetMapping("/booking/{bookingId}/has-review")
    public ResponseEntity<ApiResponse<Boolean>> hasUserReviewedBooking(@PathVariable Long bookingId) {
        log.info("Check if booking {} has been reviewed", bookingId);
        
        boolean hasReviewed = reviewService.hasUserReviewedBooking(bookingId);
        
        return ResponseEntity.ok(ApiResponse.success(
                hasReviewed ? "Booking has been reviewed" : "Booking not reviewed yet", 
                hasReviewed));
    }
    
    /**
     * Get review statistics (Admin only)
     * GET /api/reviews/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReviewStatistics() {
        log.info("Get review statistics request");
        
        Map<String, Object> statistics = reviewService.getReviewStatistics();
        
        return ResponseEntity.ok(ApiResponse.success("Review statistics retrieved successfully", statistics));
    }
    
    /**
     * Get most reviewed resources
     * GET /api/reviews/most-reviewed?limit={limit}
     */
    @GetMapping("/most-reviewed")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMostReviewedResources(
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Get most reviewed resources request with limit: {}", limit);
        
        List<Map<String, Object>> resources = reviewService.getMostReviewedResources(limit);
        
        return ResponseEntity.ok(ApiResponse.success("Most reviewed resources retrieved successfully", resources));
    }
    
    /**
     * Get lowest rated resources (Admin only)
     * GET /api/reviews/lowest-rated?limit={limit}
     */
    @GetMapping("/lowest-rated")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getLowestRatedResources(
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Get lowest rated resources request with limit: {}", limit);
        
        List<Map<String, Object>> resources = reviewService.getLowestRatedResources(limit);
        
        return ResponseEntity.ok(ApiResponse.success("Lowest rated resources retrieved successfully", resources));
    }
}
