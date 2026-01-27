package com.crm.smart_CRM.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crm.smart_CRM.dto.request.LoginRequest;
import com.crm.smart_CRM.dto.request.PasswordChangeRequest;
import com.crm.smart_CRM.dto.request.UserRegistrationRequest;
import com.crm.smart_CRM.dto.response.ApiResponse;
import com.crm.smart_CRM.dto.response.LoginResponse;
import com.crm.smart_CRM.dto.response.UserResponse;
import com.crm.smart_CRM.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final UserService userService;
    
    /**
     * User Registration
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody UserRegistrationRequest request) {
        
        log.info("Registration request received for email: {}", request.getEmail());
        
        UserResponse userResponse = userService.registerUser(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", userResponse));
    }
    
    /**
     * User Login
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        
        log.info("Login request received for email: {}", request.getEmail());
        
        LoginResponse loginResponse = userService.login(request);
        
        return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
    }
    
    /**
     * Change Password
     * PUT /api/auth/change-password/{userId}
     */
    @PutMapping("/change-password/{userId}")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @PathVariable Long userId,
            @Valid @RequestBody PasswordChangeRequest request) {
        
        log.info("Password change request for user ID: {}", userId);
        
        userService.changePassword(userId, request);
        
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }
    
    /**
     * Logout (Client-side implementation)
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        log.info("Logout request received");
        
        // In a stateless JWT implementation, logout is handled client-side
        // by removing the token from storage
        
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}