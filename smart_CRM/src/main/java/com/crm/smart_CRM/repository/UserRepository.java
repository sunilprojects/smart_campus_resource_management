package com.crm.smart_CRM.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.crm.smart_CRM.Enum.AccountStatus;
import com.crm.smart_CRM.Enum.UserRole;
import com.crm.smart_CRM.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Find by email
    Optional<User> findByEmail(String email);
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    // Find by role
    List<User> findByRole(UserRole role);
    
    // Find by status
    List<User> findByStatus(AccountStatus status);
    
    // Find by department
    List<User> findByDepartment(String department);
    
    // Find by role and status
    List<User> findByRoleAndStatus(UserRole role, AccountStatus status);
    
    // Count by role
    Long countByRole(UserRole role);
    
    // Count by status
    Long countByStatus(AccountStatus status);
}

