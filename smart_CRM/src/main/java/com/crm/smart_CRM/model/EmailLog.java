package com.crm.smart_CRM.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import com.crm.smart_CRM.Enum.EmailStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "recipient_email", nullable = false, length = 100)
    private String recipientEmail;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(name = "email_type", length = 50)
    private String emailType;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EmailStatus status;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}