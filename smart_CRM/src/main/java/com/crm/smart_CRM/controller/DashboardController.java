package com.crm.smart_CRM.controller;


import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crm.smart_CRM.dto.response.ApiResponse;
import com.crm.smart_CRM.dto.response.DashboardResponse;
import com.crm.smart_CRM.service.DashboardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    /**
     * Get student dashboard
     * GET /api/dashboard/student/{userId}
     */
    @GetMapping("/student/{userId}")
    public ResponseEntity<ApiResponse<DashboardResponse>> getStudentDashboard(
            @PathVariable Long userId) {
        
        log.info("Get student dashboard request for user ID: {}", userId);
        
        DashboardResponse dashboard = dashboardService.getStudentDashboard(userId);
        
        return ResponseEntity.ok(ApiResponse.success("Dashboard data retrieved successfully", dashboard));
    }
    
    /**
     * Get faculty dashboard
     * GET /api/dashboard/faculty/{userId}
     */
    @GetMapping("/faculty/{userId}")
    public ResponseEntity<ApiResponse<DashboardResponse>> getFacultyDashboard(
            @PathVariable Long userId) {
        
        log.info("Get faculty dashboard request for user ID: {}", userId);
        
        DashboardResponse dashboard = dashboardService.getFacultyDashboard(userId);
        
        return ResponseEntity.ok(ApiResponse.success("Dashboard data retrieved successfully", dashboard));
    }
    
    /**
     * Get admin dashboard
     * GET /api/dashboard/admin
     */
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<DashboardResponse>> getAdminDashboard() {
        log.info("Get admin dashboard request");
        
        DashboardResponse dashboard = dashboardService.getAdminDashboard();
        
        return ResponseEntity.ok(ApiResponse.success("Dashboard data retrieved successfully", dashboard));
    }
    
    /**
     * Get booking trend data
     * GET /api/dashboard/booking-trend?days={days}
     */
    @GetMapping("/booking-trend")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getBookingTrend(
            @RequestParam(defaultValue = "30") int days) {
        
        log.info("Get booking trend request for last {} days", days);
        
        Map<String, Long> trendData = dashboardService.getBookingTrend(days);
        
        return ResponseEntity.ok(ApiResponse.success("Booking trend data retrieved successfully", trendData));
    }
    
    /**
     * Get peak hours analysis
     * GET /api/dashboard/peak-hours
     */
    @GetMapping("/peak-hours")
    public ResponseEntity<ApiResponse<Map<Integer, Long>>> getPeakHoursAnalysis() {
        log.info("Get peak hours analysis request");
        
        Map<Integer, Long> peakHours = dashboardService.getPeakHoursAnalysis();
        
        return ResponseEntity.ok(ApiResponse.success("Peak hours data retrieved successfully", peakHours));
    }
    
    /**
     * Get day-wise booking distribution
     * GET /api/dashboard/day-wise-bookings
     */
    @GetMapping("/day-wise-bookings")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getDayWiseBookings() {
        log.info("Get day-wise bookings request");
        
        Map<String, Long> dayWiseData = dashboardService.getDayWiseBookings();
        
        return ResponseEntity.ok(ApiResponse.success("Day-wise booking data retrieved successfully", dayWiseData));
    }
    
    /**
     * Get category utilization
     * GET /api/dashboard/category-utilization
     */
    @GetMapping("/category-utilization")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getCategoryUtilization() {
        log.info("Get category utilization request");
        
        Map<String, Double> utilization = dashboardService.getCategoryUtilization();
        
        return ResponseEntity.ok(ApiResponse.success("Category utilization data retrieved successfully", utilization));
    }
    
    /**
     * Get most active users
     * GET /api/dashboard/active-users?limit={limit}
     */
    @GetMapping("/active-users")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMostActiveUsers(
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Get most active users request with limit: {}", limit);
        
        List<Map<String, Object>> activeUsers = dashboardService.getMostActiveUsers(limit);
        
        return ResponseEntity.ok(ApiResponse.success("Active users data retrieved successfully", activeUsers));
    }
    
    /**
     * Get system health metrics
     * GET /api/dashboard/system-health
     */
    @GetMapping("/system-health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemHealthMetrics() {
        log.info("Get system health metrics request");
        
        Map<String, Object> healthMetrics = dashboardService.getSystemHealthMetrics();
        
        return ResponseEntity.ok(ApiResponse.success("System health metrics retrieved successfully", healthMetrics));
    }
    
    /**
     * Get cancellation rate by role
     * GET /api/dashboard/cancellation-rate
     */
    @GetMapping("/cancellation-rate")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getCancellationRateByRole() {
        log.info("Get cancellation rate by role request");
        
        Map<String, Double> cancellationRates = dashboardService.getCancellationRateByRole();
        
        return ResponseEntity.ok(ApiResponse.success("Cancellation rates retrieved successfully", cancellationRates));
    }
    
    /**
     * Get average booking duration by category
     * GET /api/dashboard/avg-duration-by-category
     */
    @GetMapping("/avg-duration-by-category")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getAverageBookingDurationByCategory() {
        log.info("Get average booking duration by category request");
        
        Map<String, Double> avgDurations = dashboardService.getAverageBookingDurationByCategory();
        
        return ResponseEntity.ok(ApiResponse.success("Average durations retrieved successfully", avgDurations));
    }
}