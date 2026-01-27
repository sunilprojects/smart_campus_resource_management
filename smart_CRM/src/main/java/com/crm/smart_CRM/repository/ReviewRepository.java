package com.crm.smart_CRM.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.crm.smart_CRM.model.Booking;
import com.crm.smart_CRM.model.Resource;
import com.crm.smart_CRM.model.Review;
import com.crm.smart_CRM.model.User;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // Find by resource
    List<Review> findByResource(Resource resource);
    
    // Find by resource ID
    List<Review> findByResourceId(Long resourceId);
    
    // Find by user
    List<Review> findByUser(User user);
    
    // Find by user ID
    List<Review> findByUserId(Long userId);
    
    // Find by booking
    Optional<Review> findByBooking(Booking booking);
    
    // Find by booking ID
    Optional<Review> findByBookingId(Long bookingId);
    
    // Check if review exists for booking
    boolean existsByBooking(Booking booking);
    
    // Check if review exists by booking ID
    boolean existsByBookingId(Long bookingId);
    
    // Get average rating by resource
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.resource.id = :resourceId")
    Double getAverageRatingByResource(@Param("resourceId") Long resourceId);
    
    // Count reviews by resource
    Long countByResourceId(Long resourceId);
    
    // Find recent reviews by resource (ordered by date)
    @Query("SELECT r FROM Review r WHERE r.resource.id = :resourceId ORDER BY r.createdAt DESC")
    List<Review> findRecentReviewsByResource(@Param("resourceId") Long resourceId);
    
    // Count reviews by resource and rating
    @Query("SELECT COUNT(r) FROM Review r WHERE r.resource.id = :resourceId AND r.rating = :rating")
    Long countByResourceIdAndRating(@Param("resourceId") Long resourceId, @Param("rating") Integer rating);
    
    // Find top rated resources
    @Query("SELECT r.resource.id, AVG(r.rating) as avgRating FROM Review r " +
           "GROUP BY r.resource.id ORDER BY avgRating DESC")
    List<Object[]> findTopRatedResources();
}
