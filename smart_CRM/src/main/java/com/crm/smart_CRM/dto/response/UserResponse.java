package com.crm.smart_CRM.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.crm.smart_CRM.Enum.AccountStatus;
import com.crm.smart_CRM.Enum.UserRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String studentId;
    private String employeeId;
    private String department;
    private UserRole role;
    private AccountStatus status;
    private String profileImage;
    private LocalDateTime createdAt;
}