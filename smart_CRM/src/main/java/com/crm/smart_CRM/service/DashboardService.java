package com.crm.smart_CRM.service;



import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.crm.smart_CRM.Enum.AccountStatus;
import com.crm.smart_CRM.Enum.BookingStatus;
import com.crm.smart_CRM.Enum.EmailStatus;
import com.crm.smart_CRM.Enum.ResourceStatus;
import com.crm.smart_CRM.Enum.UserRole;
import com.crm.smart_CRM.dto.response.BookingResponse;
import com.crm.smart_CRM.dto.response.DashboardResponse;
import com.crm.smart_CRM.dto.response.ResourceUtilization;
import com.crm.smart_CRM.model.Booking;
import com.crm.smart_CRM.model.Resource;
import com.crm.smart_CRM.model.User;
import com.crm.smart_CRM.repository.BookingRepository;
import com.crm.smart_CRM.repository.EmailLogRepository;
import com.crm.smart_CRM.repository.ResourceRepository;
import com.crm.smart_CRM.repository.ReviewRepository;
import com.crm.smart_CRM.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {
    
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final EmailLogRepository emailLogRepository;
    private final BookingService bookingService;
    
    /**
     * Get student dashboard data
     */
    public DashboardResponse getStudentDashboard(Long userId) {
        log.debug("Generating student dashboard for user: {}", userId);
        
        DashboardResponse dashboard = new DashboardResponse();
        
        // Get booking statistics
//        Long totalBookings = bookingRepository.countByUserId(userId);
        Long activeBookings = bookingRepository.countByUserIdAndStatus(userId, BookingStatus.CONFIRMED);
        Long completedBookings = bookingRepository.countByUserIdAndStatus(userId, BookingStatus.COMPLETED);
        Long cancelledBookings = bookingRepository.countByUserIdAndStatus(userId, BookingStatus.CANCELLED);
        
//        dashboard.setTotalBookings(totalBookings);
        dashboard.setActiveBookings(activeBookings);
        dashboard.setCompletedBookings(completedBookings);
        dashboard.setCancelledBookings(cancelledBookings);
        
        // Get upcoming bookings (next 3)
        List<BookingResponse> upcomingBookings = getUpcomingBookingsForUser(userId, 3);
        dashboard.setUpcomingBookings(upcomingBookings);
        
        return dashboard;
    }
    
    /**
     * Get faculty dashboard data
     */
    public DashboardResponse getFacultyDashboard(Long userId) {
        log.debug("Generating faculty dashboard for user: {}", userId);
        
        // Faculty dashboard has same data as student + additional features
        DashboardResponse dashboard = getStudentDashboard(userId);
        
        // Additional faculty-specific data can be added here
        // For now, it's the same as student dashboard
        
        return dashboard;
    }
    
    /**
     * Get admin dashboard data
     */
    public DashboardResponse getAdminDashboard() {
        log.debug("Generating admin dashboard");
        
        DashboardResponse dashboard = new DashboardResponse();
        
        // ========== USER STATISTICS ==========
        Long totalUsers = userRepository.count();
        Long studentCount = userRepository.countByRole(UserRole.STUDENT);
        Long facultyCount = userRepository.countByRole(UserRole.FACULTY);
        Long adminCount = userRepository.countByRole(UserRole.ADMIN);
        
        dashboard.setTotalUsers(totalUsers);
        dashboard.setStudentCount(studentCount);
        dashboard.setFacultyCount(facultyCount);
        dashboard.setAdminCount(adminCount);
        
        // ========== RESOURCE STATISTICS ==========
        Long totalResources = resourceRepository.count();
        Long resourcesUnderMaintenance = resourceRepository.countByStatus(ResourceStatus.UNDER_MAINTENANCE);
        
        dashboard.setTotalResources(totalResources);
        dashboard.setResourcesUnderMaintenance(resourcesUnderMaintenance);
        
        // ========== BOOKING STATISTICS ==========
        Long totalBookings = bookingRepository.count();
        Long activeBookings = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        Long completedBookings = bookingRepository.countByStatus(BookingStatus.COMPLETED);
        Long cancelledBookings = bookingRepository.countByStatus(BookingStatus.CANCELLED);
        
        dashboard.setTotalBookings(totalBookings);
        dashboard.setActiveBookings(activeBookings);
        dashboard.setCompletedBookings(completedBookings);
        dashboard.setCancelledBookings(cancelledBookings);
        
        // Today's bookings
        Long todayBookings = bookingRepository.countByBookingDate(LocalDate.now());
        dashboard.setTodayBookings(todayBookings);
        
        // This week's bookings
        List<Booking> weekBookings = bookingRepository.findThisWeeksBookings();
        dashboard.setThisWeekBookings((long) weekBookings.size());
        
        // This month's bookings
        List<Booking> monthBookings = bookingRepository.findThisMonthsBookings();
        dashboard.setThisMonthBookings((long) monthBookings.size());
        
        // ========== CATEGORY-WISE BOOKINGS ==========
        Map<String, Long> categoryWiseBookings = getCategoryWiseBookings();
        dashboard.setCategoryWiseBookings(categoryWiseBookings);
        
        // ========== STATUS-WISE BOOKINGS ==========
        Map<String, Long> statusWiseBookings = getStatusWiseBookings();
        dashboard.setStatusWiseBookings(statusWiseBookings);
        
        // ========== TOP RESOURCES ==========
        List<ResourceUtilization> topResources = getTopResources(10);
        dashboard.setTopResources(topResources);
        
        // ========== UPCOMING BOOKINGS ==========
        List<BookingResponse> upcomingBookings = getUpcomingBookings(5);
        dashboard.setUpcomingBookings(upcomingBookings);
        
        return dashboard;
    }
    
    /**
     * Get upcoming bookings for a specific user
     */
    private List<BookingResponse> getUpcomingBookingsForUser(Long userId, int limit) {
        log.debug("Fetching upcoming bookings for user: {}", userId);
        
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        
        List<Booking> allUserBookings = bookingRepository.findByUserIdAndStatus(userId, BookingStatus.CONFIRMED);
        
        return allUserBookings.stream()
                .filter(booking -> {
                    // Filter only future bookings
                    if (booking.getBookingDate().isAfter(today)) {
                        return true;
                    }
                    if (booking.getBookingDate().equals(today) && booking.getStartTime().isAfter(now)) {
                        return true;
                    }
                    return false;
                })
                .sorted(Comparator.comparing(Booking::getBookingDate)
                        .thenComparing(Booking::getStartTime))
                .limit(limit)
                .map(booking -> bookingService.getBookingById(booking.getId()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get upcoming bookings (for admin dashboard)
     */
    private List<BookingResponse> getUpcomingBookings(int limit) {
        log.debug("Fetching upcoming bookings");
        
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        
        List<Booking> allBookings = bookingRepository.findByStatus(BookingStatus.CONFIRMED);
        
        return allBookings.stream()
                .filter(booking -> {
                    if (booking.getBookingDate().isAfter(today)) {
                        return true;
                    }
                    if (booking.getBookingDate().equals(today) && booking.getStartTime().isAfter(now)) {
                        return true;
                    }
                    return false;
                })
                .sorted(Comparator.comparing(Booking::getBookingDate)
                        .thenComparing(Booking::getStartTime))
                .limit(limit)
                .map(booking -> bookingService.getBookingById(booking.getId()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get category-wise booking distribution
     */
    private Map<String, Long> getCategoryWiseBookings() {
        log.debug("Calculating category-wise bookings");
        
        Map<String, Long> categoryBookings = new HashMap<>();
        
        List<Booking> allBookings = bookingRepository.findAll();
        
        for (Booking booking : allBookings) {
            String categoryName = booking.getResource().getCategory().getName();
            categoryBookings.put(categoryName, categoryBookings.getOrDefault(categoryName, 0L) + 1);
        }
        
        return categoryBookings;
    }
    
    /**
     * Get status-wise booking distribution
     */
    private Map<String, Long> getStatusWiseBookings() {
        log.debug("Calculating status-wise bookings");
        
        Map<String, Long> statusBookings = new HashMap<>();
        
        statusBookings.put("CONFIRMED", bookingRepository.countByStatus(BookingStatus.CONFIRMED));
        statusBookings.put("COMPLETED", bookingRepository.countByStatus(BookingStatus.COMPLETED));
        statusBookings.put("CANCELLED", bookingRepository.countByStatus(BookingStatus.CANCELLED));
        statusBookings.put("NO_SHOW", bookingRepository.countByStatus(BookingStatus.NO_SHOW));
        
        return statusBookings;
    }
    
    /**
     * Get top resources by booking count
     */
    private List<ResourceUtilization> getTopResources(int limit) {
        log.debug("Fetching top {} resources", limit);
        
        List<Resource> allResources = resourceRepository.findAll();
        List<ResourceUtilization> utilizationList = new ArrayList<>();
        
        for (Resource resource : allResources) {
            Long totalBookings = bookingRepository.countCompletedBookingsByResource(resource.getId());
            
            // Calculate utilization percentage (simplified)
            // In real scenario: (booked hours / total available hours) * 100
            Double utilization = Math.min(totalBookings * 5.0, 100.0);
            
            utilizationList.add(new ResourceUtilization(
                    resource.getId(),
                    resource.getName(),
                    totalBookings,
                    utilization
            ));
        }
        
        return utilizationList.stream()
                .sorted((a, b) -> Long.compare(b.getTotalBookings(), a.getTotalBookings()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Get booking trend data (for charts)
     */
    public Map<String, Long> getBookingTrend(int days) {
        log.debug("Calculating booking trend for last {} days", days);
        
        Map<String, Long> trendData = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Long count = bookingRepository.countByBookingDate(date);
            trendData.put(date.toString(), count);
        }
        
        return trendData;
    }
    
    /**
     * Get peak hours analysis
     */
    public Map<Integer, Long> getPeakHoursAnalysis() {
        log.debug("Analyzing peak booking hours");
        
        Map<Integer, Long> hourlyBookings = new HashMap<>();
        
        // Initialize all hours (8 AM to 8 PM)
        for (int hour = 8; hour < 20; hour++) {
            hourlyBookings.put(hour, 0L);
        }
        
        List<Booking> allBookings = bookingRepository.findAll();
        
        for (Booking booking : allBookings) {
            int startHour = booking.getStartTime().getHour();
            hourlyBookings.put(startHour, hourlyBookings.getOrDefault(startHour, 0L) + 1);
        }
        
        return hourlyBookings;
    }
    
    /**
     * Get day-wise booking distribution
     */
    public Map<String, Long> getDayWiseBookings() {
        log.debug("Analyzing day-wise bookings");
        
        Map<String, Long> dayWiseBookings = new LinkedHashMap<>();
        
        // Initialize all days
        dayWiseBookings.put("Monday", 0L);
        dayWiseBookings.put("Tuesday", 0L);
        dayWiseBookings.put("Wednesday", 0L);
        dayWiseBookings.put("Thursday", 0L);
        dayWiseBookings.put("Friday", 0L);
        dayWiseBookings.put("Saturday", 0L);
        dayWiseBookings.put("Sunday", 0L);
        
        List<Booking> allBookings = bookingRepository.findAll();
        
        for (Booking booking : allBookings) {
            String dayName = booking.getBookingDate().getDayOfWeek().toString();
            dayName = dayName.charAt(0) + dayName.substring(1).toLowerCase();
            dayWiseBookings.put(dayName, dayWiseBookings.getOrDefault(dayName, 0L) + 1);
        }
        
        return dayWiseBookings;
    }
    
    /**
     * Get resource utilization by category
     */
    public Map<String, Double> getCategoryUtilization() {
        log.debug("Calculating category utilization");
        
        Map<String, Double> categoryUtilization = new HashMap<>();
        
        List<Resource> allResources = resourceRepository.findAll();
        
        // Group resources by category
        Map<String, List<Resource>> resourcesByCategory = allResources.stream()
                .collect(Collectors.groupingBy(r -> r.getCategory().getName()));
        
        for (Map.Entry<String, List<Resource>> entry : resourcesByCategory.entrySet()) {
            String category = entry.getKey();
            List<Resource> resources = entry.getValue();
            
            // Calculate average utilization for category
            double avgUtilization = resources.stream()
                    .mapToDouble(resource -> {
                        Long bookings = bookingRepository.countCompletedBookingsByResource(resource.getId());
                        return Math.min(bookings * 5.0, 100.0); // Simplified calculation
                    })
                    .average()
                    .orElse(0.0);
            
            categoryUtilization.put(category, avgUtilization);
        }
        
        return categoryUtilization;
    }
    
    /**
     * Get most active users
     */
    public List<Map<String, Object>> getMostActiveUsers(int limit) {
        log.debug("Fetching top {} active users", limit);
        
        List<User> allUsers = userRepository.findAll();
        
        return allUsers.stream()
                .map(user -> {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("userId", user.getId());
                    userData.put("userName", user.getName());
                    userData.put("role", user.getRole());
//                    userData.put("totalBookings", bookingRepository.countByUserId(user.getId()));
                    return userData;
                })
                .sorted((a, b) -> Long.compare(
                        (Long) b.get("totalBookings"),
                        (Long) a.get("totalBookings")
                ))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Get system health metrics
     */
    public Map<String, Object> getSystemHealthMetrics() {
        log.debug("Calculating system health metrics");
        
        Map<String, Object> metrics = new HashMap<>();
        
        // Email statistics
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        Long emailsSent = emailLogRepository.countEmailsSince(last24Hours);
        Long emailsFailed = emailLogRepository.countByStatus(EmailStatus.FAILED);
        
        metrics.put("emailsSentLast24h", emailsSent);
        metrics.put("emailsFailedTotal", emailsFailed);
        
        // Booking statistics
        Long activeBookings = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        Long noShowCount = bookingRepository.countByStatus(BookingStatus.NO_SHOW);
        
        metrics.put("currentActiveBookings", activeBookings);
        metrics.put("totalNoShows", noShowCount);
        
        // Resource statistics
        Long availableResources = resourceRepository.countByStatus(ResourceStatus.AVAILABLE);
        Long maintenanceResources = resourceRepository.countByStatus(ResourceStatus.UNDER_MAINTENANCE);
        
        metrics.put("availableResources", availableResources);
        metrics.put("resourcesInMaintenance", maintenanceResources);
        
        // User statistics
        Long activeUsers = userRepository.countByStatus(AccountStatus.ACTIVE);
        Long inactiveUsers = userRepository.countByStatus(AccountStatus.INACTIVE);
        
        metrics.put("activeUsers", activeUsers);
        metrics.put("inactiveUsers", inactiveUsers);
        
        return metrics;
    }
    
    /**
     * Get cancellation rate by user role
     */
    public Map<String, Double> getCancellationRateByRole() {
        log.debug("Calculating cancellation rate by role");
        
        Map<String, Double> cancellationRates = new HashMap<>();
        
        for (UserRole role : UserRole.values()) {
            List<User> usersWithRole = userRepository.findByRole(role);
            
            long totalBookings = 0;
            long cancelledBookings = 0;
            
            for (User user : usersWithRole) {
//                totalBookings += bookingRepository.countByUserId(user.getId());
                cancelledBookings += bookingRepository.countByUserIdAndStatus(user.getId(), BookingStatus.CANCELLED);
            }
            
            double rate = totalBookings > 0 ? (cancelledBookings * 100.0) / totalBookings : 0.0;
            cancellationRates.put(role.toString(), rate);
        }
        
        return cancellationRates;
    }
    
    /**
     * Get average booking duration by category
     */
    public Map<String, Double> getAverageBookingDurationByCategory() {
        log.debug("Calculating average booking duration by category");
        
        Map<String, Double> avgDurations = new HashMap<>();
        
        List<Booking> allBookings = bookingRepository.findAll();
        
        Map<String, List<Booking>> bookingsByCategory = allBookings.stream()
                .collect(Collectors.groupingBy(b -> b.getResource().getCategory().getName()));
        
        for (Map.Entry<String, List<Booking>> entry : bookingsByCategory.entrySet()) {
            String category = entry.getKey();
            List<Booking> bookings = entry.getValue();
            
            double avgDuration = bookings.stream()
                    .filter(b -> b.getDuration() != null)
                    .mapToInt(Booking::getDuration)
                    .average()
                    .orElse(0.0);
            
            avgDurations.put(category, avgDuration);
        }
        
        return avgDurations;
    }
}
