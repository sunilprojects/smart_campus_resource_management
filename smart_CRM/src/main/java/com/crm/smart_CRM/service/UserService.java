package com.crm.smart_CRM.service;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crm.smart_CRM.Enum.AccountStatus;
import com.crm.smart_CRM.Enum.UserRole;
import com.crm.smart_CRM.dto.request.LoginRequest;
import com.crm.smart_CRM.dto.request.PasswordChangeRequest;
import com.crm.smart_CRM.dto.request.UserRegistrationRequest;
import com.crm.smart_CRM.dto.request.UserUpdateRequest;
import com.crm.smart_CRM.dto.response.LoginResponse;
import com.crm.smart_CRM.dto.response.UserResponse;
import com.crm.smart_CRM.exception.ResourceNotFoundException;
import com.crm.smart_CRM.exception.UnauthorizedException;
import com.crm.smart_CRM.model.User;
import com.crm.smart_CRM.repository.UserRepository;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    /**
     * Register a new user
     */
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already registered: " + request.getEmail());
        }
        
        // Create user entity
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // In production: hash with BCrypt
        user.setPhone(request.getPhone());
        user.setStudentId(request.getStudentId());
        user.setEmployeeId(request.getEmployeeId());
        user.setDepartment(request.getDepartment());
        user.setRole(request.getRole() != null ? request.getRole() : UserRole.STUDENT);
        user.setStatus(AccountStatus.ACTIVE);
        
        // Save user
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());
        
        // Send welcome email asynchronously
        try {
            emailService.sendWelcomeEmail(savedUser);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", savedUser.getEmail(), e);
            // Don't fail registration if email fails
        }
        
        return mapToUserResponse(savedUser);
    }
    
    /**
     * User login
     */
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        
        // Check password (In production: use BCrypt.matches())
        if (!user.getPassword().equals(request.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        
        // Check if account is active
        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new UnauthorizedException("Account is inactive. Please contact administrator.");
        }
        
        log.info("User logged in successfully: {}", user.getEmail());
        
        // Generate simple token (In production: use JWT)
        String token = UUID.randomUUID().toString();
        
        return new LoginResponse("Login successful", mapToUserResponse(user), token);
    }
    
    /**
     * Get user by ID
     */
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return mapToUserResponse(user);
    }
    
    /**
     * Get user by email
     */
    public UserResponse getUserByEmail(String email) {
        log.debug("Fetching user with email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToUserResponse(user);
    }
    
    /**
     * Get all users
     */
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get users by role
     */
    public List<UserResponse> getUsersByRole(UserRole role) {
        log.debug("Fetching users with role: {}", role);
        return userRepository.findByRole(role).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Update user profile
     */
    @Transactional
    public UserResponse updateProfile(Long id, UserUpdateRequest request) {
        log.info("Updating profile for user ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        // Update allowed fields
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }
        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("Profile updated successfully for user ID: {}", id);
        
        return mapToUserResponse(updatedUser);
    }
    
    /**
     * Change password
     */
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        log.info("Changing password for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify current password
        if (!user.getPassword().equals(request.getCurrentPassword())) {
            throw new ValidationException("Current password is incorrect");
        }
        
        // Verify new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("New password and confirm password do not match");
        }
        
        // Update password (In production: hash with BCrypt)
        user.setPassword(request.getNewPassword());
        userRepository.save(user);
        
        log.info("Password changed successfully for user ID: {}", userId);
    }
    
    /**
     * Change user role (Admin only)
     */
    @Transactional
    public UserResponse changeUserRole(Long userId, UserRole newRole) {
        log.info("Changing role for user ID: {} to {}", userId, newRole);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Prevent changing role to ADMIN
        if (newRole == UserRole.ADMIN) {
            throw new ValidationException("Cannot assign ADMIN role through this endpoint");
        }
        
        user.setRole(newRole);
        User updatedUser = userRepository.save(user);
        
        log.info("Role changed successfully for user ID: {}", userId);
        return mapToUserResponse(updatedUser);
    }
    
    /**
     * Change account status (Admin only)
     */
    @Transactional
    public UserResponse changeAccountStatus(Long userId, AccountStatus status) {
        log.info("Changing account status for user ID: {} to {}", userId, status);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        user.setStatus(status);
        User updatedUser = userRepository.save(user);
        
        log.info("Account status changed successfully for user ID: {}", userId);
        return mapToUserResponse(updatedUser);
    }
    
    /**
     * Get total user count
     */
    public Long getTotalUserCount() {
        return userRepository.count();
    }
    
    /**
     * Get user count by role
     */
    public Long getUserCountByRole(UserRole role) {
        return userRepository.countByRole(role);
    }
    
    /**
     * Helper method to convert User entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getStudentId(),
                user.getEmployeeId(),
                user.getDepartment(),
                user.getRole(),
                user.getStatus(),
                user.getProfileImage(),
                user.getCreatedAt()
        );
    }
}
