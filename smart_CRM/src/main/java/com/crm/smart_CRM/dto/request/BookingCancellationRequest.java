package com.crm.smart_CRM.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCancellationRequest {
    
    private String cancellationReason;
    
    private Long cancelledByUserId;
}