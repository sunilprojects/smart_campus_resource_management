package com.crm.smart_CRM.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRequest {
    
    @NotNull(message = "Maintenance start time is required")
    private LocalDateTime maintenanceStart;
    
    @NotNull(message = "Maintenance end time is required")
    private LocalDateTime maintenanceEnd;
    
    @NotNull(message = "Maintenance reason is required")
    private String maintenanceReason;
}