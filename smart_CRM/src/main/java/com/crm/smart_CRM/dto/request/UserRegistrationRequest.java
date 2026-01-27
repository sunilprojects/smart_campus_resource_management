package com.crm.smart_CRM.dto.request;


import com.crm.smart_CRM.Enum.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
    private String password;
    
    @Size(max = 15, message = "Phone number must not exceed 15 characters")
    private String phone;
    
    private String studentId;
    
    private String employeeId;
    
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;
    
    private UserRole role = UserRole.STUDENT;
}