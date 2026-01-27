package com.crm.smart_CRM.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlotsResponse {
    
    private LocalDate date;
    private Long resourceId;
    private String resourceName;
    private List<TimeSlot> slots;
}
