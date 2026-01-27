package com.crm.smart_CRM.service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crm.smart_CRM.Enum.BookingStatus;
import com.crm.smart_CRM.dto.request.ReviewRequest;
import com.crm.smart_CRM.dto.request.ReviewUpdateRequest;
import com.crm.smart_CRM.dto.response.ReviewResponse;
import com.crm.smart_CRM.exception.ResourceNotFoundException;
import com.crm.smart_CRM.model.Booking;
import com.crm.smart_CRM.model.Resource;
import com.crm.smart_CRM.model.Review;
import com.crm.smart_CRM.model.User;
import com.crm.smart_CRM.repository.BookingRepository;
import com.crm.smart_CRM.repository.ResourceRepository;
import com.crm.smart_CRM.repository.ReviewRepository;
import com.crm.smart_CRM.repository.UserRepository;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    
    
    /**
     * Submit a new review
     */
    @Transactional
    public ReviewResponse submitReview(ReviewRequest request) {
        log.info("Submitting review for resource: {} by user: {}", 
                request.getResourceId(), request.getUserId());
        
        // Fetch entities
        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        
        // Validate review eligibility
        validateReviewEligibility(booking, user, resource);
        
        // Create review
        Review review = new Review();
        review.setResource(resource);
        review.setUser(user);
        review.setBooking(booking);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        
        Review savedReview = reviewRepository.save(review);
        log.info("Review submitted successfully with ID: {}", savedReview.getId());
        
        return mapToReviewResponse(savedReview);
    }
    
    /**
     * Validate if user can submit review for this booking
     */
    private void validateReviewEligibility(Booking booking, User user, Resource resource) {
        log.debug("Validating review eligibility for booking: {}", booking.getId());
        
        // 1. Check if booking belongs to the user
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ValidationException("You can only review your own bookings");
        }
        
        // 2. Check if booking is for the same resource
        if (!booking.getResource().getId().equals(resource.getId())) {
            throw new ValidationException("Booking is not for this resource");
        }
        
        // 3. Check if booking is completed
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ValidationException("Can only review completed bookings");
        }
        
        // 4. Check if review already exists for this booking
        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new ValidationException("Review already exists for this booking");
        }
        
        log.debug("Review eligibility validation passed");
    }
    
    /**
     * Get all reviews for a resource
     */
    public List<ReviewResponse> getReviewsByResource(Long resourceId) {
        log.debug("Fetching reviews for resource: {}", resourceId);
        
        // Verify resource exists
        if (!resourceRepository.existsById(resourceId)) {
            throw new ResourceNotFoundException("Resource not found");
        }
        
        List<Review> reviews = reviewRepository.findByResourceId(resourceId);
        return reviews.stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get recent reviews for a resource (sorted by date)
     */
    public List<ReviewResponse> getRecentReviewsByResource(Long resourceId, int limit) {
        log.debug("Fetching recent {} reviews for resource: {}", limit, resourceId);
        
        List<Review> reviews = reviewRepository.findRecentReviewsByResource(resourceId);
        return reviews.stream()
                .limit(limit)
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all reviews by a user
     */
    public List<ReviewResponse> getReviewsByUser(Long userId) {
        log.debug("Fetching reviews by user: {}", userId);
        
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
        
        List<Review> reviews = reviewRepository.findByUserId(userId);
        return reviews.stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get review by ID
     */
    public ReviewResponse getReviewById(Long id) {
        log.debug("Fetching review with ID: {}", id);
        
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        return mapToReviewResponse(review);
    }
    
    /**
     * Update review (user can edit their own review)
     */
    @Transactional
    public ReviewResponse updateReview(Long reviewId, Long userId, ReviewUpdateRequest request) {
        log.info("Updating review ID: {} by user: {}", reviewId, userId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        // Check if user owns this review
        if (!review.getUser().getId().equals(userId)) {
            throw new ValidationException("You can only edit your own reviews");
        }
        
        // Update fields
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }
        
        Review updatedReview = reviewRepository.save(review);
        log.info("Review updated successfully");
        
        return mapToReviewResponse(updatedReview);
    }
    
    /**
     * Delete review (Admin only or user's own review)
     */
    @Transactional
    public void deleteReview(Long reviewId, Long userId, boolean isAdmin) {
        log.info("Deleting review ID: {}", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        // Check permissions
        if (!isAdmin && !review.getUser().getId().equals(userId)) {
            throw new ValidationException("You can only delete your own reviews");
        }
        
        reviewRepository.delete(review);
        log.info("Review deleted successfully");
    }
    
    /**
     * Get average rating for a resource
     */
    public Double getAverageRating(Long resourceId) {
        log.debug("Calculating average rating for resource: {}", resourceId);
        
        Double avgRating = reviewRepository.getAverageRatingByResource(resourceId);
        return avgRating != null ? avgRating : 0.0;
    }
    
    /**
     * Get rating distribution for a resource
     */
    public Map<Integer, Long> getRatingDistribution(Long resourceId) {
        log.debug("Getting rating distribution for resource: {}", resourceId);
        
        Map<Integer, Long> distribution = new HashMap<>();
        
        for (int rating = 1; rating <= 5; rating++) {
            Long count = reviewRepository.countByResourceIdAndRating(resourceId, rating);
            distribution.put(rating, count);
        }
        
        return distribution;
    }
    
    /**
     * Get total review count for a resource
     */
    public Long getReviewCount(Long resourceId) {
        return reviewRepository.countByResourceId(resourceId);
    }
    
    /**
     * Check if user has reviewed a booking
     */
    public boolean hasUserReviewedBooking(Long bookingId) {
        return reviewRepository.existsByBookingId(bookingId);
    }
    
    /**
     * Get review statistics for admin dashboard
     */
    public Map<String, Object> getReviewStatistics() {
        log.debug("Calculating review statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        // Total reviews
        Long totalReviews = reviewRepository.count();
        stats.put("totalReviews", totalReviews);
        
        // Get top rated resources (simplified)
        List<Object[]> topRated = reviewRepository.findTopRatedResources();
        stats.put("topRatedResources", topRated);
        
        // Average rating across all resources
        List<Review> allReviews = reviewRepository.findAll();
        double overallAvgRating = allReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        stats.put("overallAverageRating", overallAvgRating);
        
        // Rating distribution (all resources)
        Map<Integer, Long> globalDistribution = new HashMap<>();
        for (int rating = 1; rating <= 5; rating++) {
        	 final int currentRating=rating;
            long count = allReviews.stream()
                    .filter(r -> r.getRating() == currentRating)
                    .count();
            globalDistribution.put(rating, count);
        }
        stats.put("ratingDistribution", globalDistribution);
        
        return stats;
    }
    
    /**
     * Get most reviewed resources
     */
    public List<Map<String, Object>> getMostReviewedResources(int limit) {
        log.debug("Fetching top {} most reviewed resources", limit);
        
        List<Resource> allResources = resourceRepository.findAll();
        
        return allResources.stream()
                .map(resource -> {
                    Map<String, Object> resourceData = new HashMap<>();
                    resourceData.put("resourceId", resource.getId());
                    resourceData.put("resourceName", resource.getName());
                    resourceData.put("reviewCount", reviewRepository.countByResourceId(resource.getId()));
                    resourceData.put("averageRating", reviewRepository.getAverageRatingByResource(resource.getId()));
                    return resourceData;
                })
                .sorted((a, b) -> Long.compare(
                        (Long) b.get("reviewCount"), 
                        (Long) a.get("reviewCount")
                ))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Get lowest rated resources (for improvement)
     */
    public List<Map<String, Object>> getLowestRatedResources(int limit) {
        log.debug("Fetching lowest rated resources");
        
        List<Resource> allResources = resourceRepository.findAll();
        
        return allResources.stream()
                .map(resource -> {
                    Long reviewCount = reviewRepository.countByResourceId(resource.getId());
                    if (reviewCount == 0) return null; // Skip resources with no reviews
                    
                    Map<String, Object> resourceData = new HashMap<>();
                    resourceData.put("resourceId", resource.getId());
                    resourceData.put("resourceName", resource.getName());
                    resourceData.put("reviewCount", reviewCount);
                    resourceData.put("averageRating", reviewRepository.getAverageRatingByResource(resource.getId()));
                    return resourceData;
                })
                .filter(data -> data != null)
                .sorted((a, b) -> Double.compare(
                        (Double) a.get("averageRating"), 
                        (Double) b.get("averageRating")
                ))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Map Review entity to ReviewResponse DTO
     */
    private ReviewResponse mapToReviewResponse(Review review) {
        // Get only first name for privacy
        String userName = review.getUser().getName().split(" ")[0];
        
        // Check if review was edited
        boolean edited = !review.getCreatedAt().equals(review.getUpdatedAt());
        
        ReviewResponse response = new ReviewResponse(
                review.getId(),
                review.getResource().getId(),
                review.getResource().getName(),
                userName,
                review.getBooking().getId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                edited
        );
        
        return response;
    }
}