package com.crm.smart_CRM.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceUtilization {
    
    private Long resourceId;
    private String resourceName;
    private Long totalBookings;
    private Double utilizationPercentage;
}