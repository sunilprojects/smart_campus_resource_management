package com.crm.smart_CRM.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String message;
    private UserResponse user;
    private String token; // Simple token (UUID or JWT)
}