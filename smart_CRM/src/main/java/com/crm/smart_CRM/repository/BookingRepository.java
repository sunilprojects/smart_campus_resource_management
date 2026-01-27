package com.crm.smart_CRM.repository;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.crm.smart_CRM.Enum.BookingStatus;
import com.crm.smart_CRM.model.Booking;
import com.crm.smart_CRM.model.Resource;
import com.crm.smart_CRM.model.User;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // Find by user
    List<Booking> findByUser(User user);
    
    // Find by user ID
    List<Booking> findByUserId(Long userId);
    
    // Find by user and status
    List<Booking> findByUserAndStatus(User user, BookingStatus status);
    
    // Find by user ID and status
    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);
    
    // Find by resource
    List<Booking> findByResource(Resource resource);
    
    // Find by resource ID
    List<Booking> findByResourceId(Long resourceId);
    
    // Find by resource and booking date and status
    List<Booking> findByResourceAndBookingDateAndStatus(Resource resource, 
                                                         LocalDate bookingDate, 
                                                         BookingStatus status);
    
    // Find by resource ID, date and status
    @Query("SELECT b FROM Booking b WHERE b.resource.id = :resourceId " +
           "AND b.bookingDate = :date AND b.status = :status")
    List<Booking> findByResourceIdAndDateAndStatus(@Param("resourceId") Long resourceId,
                                                     @Param("date") LocalDate date,
                                                     @Param("status") BookingStatus status);
 // Find by resource ID and status [sunil s]
    @Query("SELECT b FROM Booking b WHERE b.resource.id = :resourceId " +
           "AND b.status = :status")
    List<Booking> findByResourceIdAndStatus(@Param("resourceId") Long resourceId,@Param("status") BookingStatus status);
    
    // Find by status
    List<Booking> findByStatus(BookingStatus status);
    
    // Find by booking date
    List<Booking> findByBookingDate(LocalDate bookingDate);
    
    // Find by booking date range
    @Query("SELECT b FROM Booking b WHERE b.bookingDate BETWEEN :startDate AND :endDate")
    List<Booking> findByDateRange(@Param("startDate") LocalDate startDate, 
                                   @Param("endDate") LocalDate endDate);
    
    // Find conflicting bookings
    @Query("SELECT b FROM Booking b WHERE b.resource.id = :resourceId " +
           "AND b.bookingDate = :date " +
           "AND b.status = 'CONFIRMED' " +
           "AND ((b.startTime < :endTime AND b.endTime > :startTime))")
    List<Booking> findConflictingBookings(@Param("resourceId") Long resourceId,
                                          @Param("date") LocalDate date,
                                          @Param("startTime") LocalTime startTime,
                                          @Param("endTime") LocalTime endTime);
    
    // Count active bookings by user
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId " +
           "AND b.status IN :statuses")
    Long countActiveBookingsByUser(@Param("userId") Long userId, 
                                    @Param("statuses") List<BookingStatus> statuses);
    
    // Find completed bookings (for scheduler)
    @Query("SELECT b FROM Booking b WHERE b.status = 'CONFIRMED' " +
           "AND FUNCTION('CONCAT', b.bookingDate, ' ', b.endTime) < :currentDateTime")
    List<Booking> findCompletedBookings(@Param("currentDateTime") String currentDateTime);
    
    // Find upcoming bookings (for reminders)
    @Query("SELECT b FROM Booking b WHERE b.status = 'CONFIRMED' " +
           "AND b.bookingDate = :date " +
           "AND b.startTime BETWEEN :startTime AND :endTime")
    List<Booking> findUpcomingBookings(@Param("date") LocalDate date,
                                       @Param("startTime") LocalTime startTime,
                                       @Param("endTime") LocalTime endTime);
    
    // Count completed bookings by resource
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.resource.id = :resourceId " +
           "AND b.status = 'COMPLETED'")
    Long countCompletedBookingsByResource(@Param("resourceId") Long resourceId);
    
    // Count bookings by date
    Long countByBookingDate(LocalDate date);
    
    // Count by status
    Long countByStatus(BookingStatus status);
    
    // Count by user ID and status
    Long countByUserIdAndStatus(Long userId, BookingStatus status);
    
    // Find today's bookings
    @Query("SELECT b FROM Booking b WHERE b.bookingDate = CURRENT_DATE")
    List<Booking> findTodaysBookings();
    
    // Find this week's bookings
    @Query("SELECT b FROM Booking b WHERE FUNCTION('WEEK', b.bookingDate) = FUNCTION('WEEK', CURRENT_DATE) " +
           "AND FUNCTION('YEAR', b.bookingDate) = FUNCTION('YEAR', CURRENT_DATE)")
    List<Booking> findThisWeeksBookings();
    
    // Find this month's bookings
    @Query("SELECT b FROM Booking b WHERE FUNCTION('MONTH', b.bookingDate) = FUNCTION('MONTH', CURRENT_DATE) " +
           "AND FUNCTION('YEAR', b.bookingDate) = FUNCTION('YEAR', CURRENT_DATE)")
    List<Booking> findThisMonthsBookings();
}
