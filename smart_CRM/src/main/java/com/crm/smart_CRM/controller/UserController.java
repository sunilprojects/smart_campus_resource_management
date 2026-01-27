package com.crm.smart_CRM.controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crm.smart_CRM.Enum.AccountStatus;
import com.crm.smart_CRM.Enum.UserRole;
import com.crm.smart_CRM.dto.request.UserUpdateRequest;
import com.crm.smart_CRM.dto.response.ApiResponse;
import com.crm.smart_CRM.dto.response.UserResponse;
import com.crm.smart_CRM.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    
    /**
     * Get all users (Admin only)
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        log.info("Get all users request received");
        
        List<UserResponse> users = userService.getAllUsers();
        
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }
    
    /**
     * Get user by ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        log.info("Get user by ID request: {}", id);
        
        UserResponse user = userService.getUserById(id);
        
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }
    
    /**
     * Get user by email
     * GET /api/users/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        log.info("Get user by email request: {}", email);
        
        UserResponse user = userService.getUserByEmail(email);
        
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }
    
    /**
     * Get users by role (Admin only)
     * GET /api/users/role/{role}
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@PathVariable UserRole role) {
        log.info("Get users by role request: {}", role);
        
        List<UserResponse> users = userService.getUsersByRole(role);
        
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }
    
    /**
     * Update user profile
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        
        log.info("Update profile request for user ID: {}", id);
        
        UserResponse updatedUser = userService.updateProfile(id, request);
        
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedUser));
    }
    
    /**
     * Change user role (Admin only)
     * PUT /api/users/{id}/role
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> changeUserRole(
            @PathVariable Long id,
            @RequestParam UserRole role) {
        
        log.info("Change role request for user ID: {} to role: {}", id, role);
        
        UserResponse updatedUser = userService.changeUserRole(id, role);
        
        return ResponseEntity.ok(ApiResponse.success("Role changed successfully", updatedUser));
    }
    
    /**
     * Change account status (Admin only)
     * PUT /api/users/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> changeAccountStatus(
            @PathVariable Long id,
            @RequestParam AccountStatus status) {
        
        log.info("Change account status request for user ID: {} to status: {}", id, status);
        
        UserResponse updatedUser = userService.changeAccountStatus(id, status);
        
        return ResponseEntity.ok(ApiResponse.success("Account status changed successfully", updatedUser));
    }
    
    /**
     * Get total user count
     * GET /api/users/count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getTotalUserCount() {
        log.info("Get total user count request");
        
        Long count = userService.getTotalUserCount();
        
        return ResponseEntity.ok(ApiResponse.success("User count retrieved successfully", count));
    }
    
    /**
     * Get user count by role
     * GET /api/users/count/role/{role}
     */
    @GetMapping("/count/role/{role}")
    public ResponseEntity<ApiResponse<Long>> getUserCountByRole(@PathVariable UserRole role) {
        log.info("Get user count by role request: {}", role);
        
        Long count = userService.getUserCountByRole(role);
        
        return ResponseEntity.ok(ApiResponse.success("User count retrieved successfully", count));
    }
}