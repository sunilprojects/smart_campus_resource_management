package com.crm.smart_CRM.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceCategoryResponse {
    
    private Long id;
    private String name;
    private String description;
    private String icon;
    private LocalDateTime createdAt;
}