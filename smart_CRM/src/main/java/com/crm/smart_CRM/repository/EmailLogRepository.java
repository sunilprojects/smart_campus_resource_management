package com.crm.smart_CRM.repository;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.crm.smart_CRM.Enum.EmailStatus;
import com.crm.smart_CRM.model.EmailLog;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    
    // Find by recipient email
    List<EmailLog> findByRecipientEmail(String email);
    
    // Find by status
    List<EmailLog> findByStatus(EmailStatus status);
    
    // Find by email type
    List<EmailLog> findByEmailType(String emailType);
    
    // Find by date range
    @Query("SELECT e FROM EmailLog e WHERE e.createdAt BETWEEN :start AND :end")
    List<EmailLog> findByDateRange(@Param("start") LocalDateTime start, 
                                    @Param("end") LocalDateTime end);
    
    // Count by status
    Long countByStatus(EmailStatus status);
    
    // Count emails sent since date
    @Query("SELECT COUNT(e) FROM EmailLog e WHERE e.createdAt >= :date")
    Long countEmailsSince(@Param("date") LocalDateTime date);
    
    // Count by email type and status
    Long countByEmailTypeAndStatus(String emailType, EmailStatus status);
    
    // Find failed emails
    List<EmailLog> findByStatusOrderByCreatedAtDesc(EmailStatus status);
}