package com.crm.smart_CRM.dto.request;


import com.crm.smart_CRM.Enum.ResourceStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceStatusUpdateRequest {
    
    @NotNull(message = "Status is required")
    private ResourceStatus status;
}