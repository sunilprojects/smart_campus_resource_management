package com.crm.smart_CRM.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    
    // Common for all users
    private Long totalBookings;
    private Long activeBookings;
    private Long completedBookings;
    private Long cancelledBookings;
    private List<BookingResponse> upcomingBookings;
    
    // Admin-specific fields
    private Long totalResources;
    private Long totalUsers;
    private Long studentCount;
    private Long facultyCount;
    private Long adminCount;
    private Long todayBookings;
    private Long thisWeekBookings;
    private Long thisMonthBookings;
    private Long resourcesUnderMaintenance;
    private Map<String, Long> categoryWiseBookings;
    private Map<String, Long> statusWiseBookings;
    private List<ResourceUtilization> topResources;
}
