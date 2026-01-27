package com.crm.smart_CRM.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

import com.crm.smart_CRM.Enum.SlotStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlot {
    
    private LocalTime time;
    private SlotStatus status;
}

