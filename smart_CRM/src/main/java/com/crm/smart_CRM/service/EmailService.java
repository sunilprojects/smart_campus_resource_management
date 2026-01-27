package com.crm.smart_CRM.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.crm.smart_CRM.Enum.EmailStatus;
import com.crm.smart_CRM.model.Booking;
import com.crm.smart_CRM.model.EmailLog;
import com.crm.smart_CRM.model.Resource;
import com.crm.smart_CRM.model.User;
import com.crm.smart_CRM.repository.EmailLogRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;
    
    // Date formatters
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
    
    /**
     * Send welcome email to new user
     */
    @Async
    public void sendWelcomeEmail(User user) {
        log.info("Sending welcome email to: {}", user.getEmail());
        
        String subject = "Welcome to Campus Resource Management System!";
        String content = buildWelcomeEmailContent(user);
        
        sendEmail(user.getEmail(), subject, content, "WELCOME");
    }
    
    /**
     * Send booking confirmation email
     */
    @Async
    public void sendBookingConfirmation(Booking booking) {
        log.info("Sending booking confirmation email to: {}", booking.getUser().getEmail());
        
        String subject = "Booking Confirmed - " + booking.getResource().getName();
        String content = buildBookingConfirmationContent(booking);
        
        sendEmail(booking.getUser().getEmail(), subject, content, "BOOKING_CONFIRMATION");
    }
    
    /**
     * Send booking reminder email (24 hours before)
     */
    @Async
    public void sendBookingReminder(Booking booking) {
        log.info("Sending booking reminder email to: {}", booking.getUser().getEmail());
        
        String subject = "Reminder: Your booking tomorrow at " + booking.getStartTime().format(TIME_FORMATTER);
        String content = buildBookingReminderContent(booking);
        
        sendEmail(booking.getUser().getEmail(), subject, content, "BOOKING_REMINDER");
    }
    
    /**
     * Send booking cancellation email
     */
    @Async
    public void sendCancellationEmail(Booking booking) {
        log.info("Sending cancellation email to: {}", booking.getUser().getEmail());
        
        String subject = "Booking Cancelled - " + booking.getResource().getName();
        String content = buildCancellationEmailContent(booking);
        
        sendEmail(booking.getUser().getEmail(), subject, content, "BOOKING_CANCELLATION");
        
        // If cancelled by admin, send email to admin too
        if (booking.getCancelledBy() != null && 
            !booking.getCancelledBy().getId().equals(booking.getUser().getId())) {
            String adminContent = buildAdminCancellationNotification(booking);
            sendEmail(booking.getCancelledBy().getEmail(), 
                     "Booking Cancellation Confirmation", 
                     adminContent, 
                     "ADMIN_CANCELLATION_NOTIFICATION");
        }
    }
    
    /**
     * Send maintenance notification email
     */
    @Async
    public void sendMaintenanceNotification(Booking booking, Resource resource) {
        log.info("Sending maintenance notification email to: {}", booking.getUser().getEmail());
        
        String subject = "IMPORTANT: Your booking has been cancelled - Maintenance Scheduled";
        String content = buildMaintenanceNotificationContent(booking, resource);
        
        sendEmail(booking.getUser().getEmail(), subject, content, "MAINTENANCE_NOTIFICATION");
    }
    
    /**
     * Send booking completion email with rating request
     */
    @Async
    public void sendCompletionEmail(Booking booking) {
        log.info("Sending completion email to: {}", booking.getUser().getEmail());
        
        String subject = "How was your experience? - " + booking.getResource().getName();
        String content = buildCompletionEmailContent(booking);
        
        sendEmail(booking.getUser().getEmail(), subject, content, "BOOKING_COMPLETION");
    }
    
    /**
     * Send no-show notification email
     */
    @Async
    public void sendNoShowNotification(Booking booking) {
        log.info("Sending no-show notification email to: {}", booking.getUser().getEmail());
        
        String subject = "Missed Booking Notice";
        String content = buildNoShowNotificationContent(booking);
        
        sendEmail(booking.getUser().getEmail(), subject, content, "NO_SHOW_NOTIFICATION");
    }
    
    /**
     * Core method to send email
     */
    private void sendEmail(String to, String subject, String htmlContent, String emailType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("noreply@campusresource.com");
            
            mailSender.send(message);
            
            log.info("Email sent successfully to: {}", to);
            logEmail(to, subject, emailType, EmailStatus.SENT, null);
            
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            logEmail(to, subject, emailType, EmailStatus.FAILED, e.getMessage());
        }
    }
    
    /**
     * Log email activity to database
     */
    private void logEmail(String recipientEmail, String subject, String emailType, 
                          EmailStatus status, String errorMessage) {
        try {
            EmailLog emailLog = new EmailLog();
            emailLog.setRecipientEmail(recipientEmail);
            emailLog.setSubject(subject);
            emailLog.setEmailType(emailType);
            emailLog.setStatus(status);
            emailLog.setErrorMessage(errorMessage);
            emailLog.setSentAt(status == EmailStatus.SENT ? LocalDateTime.now() : null);
            
            emailLogRepository.save(emailLog);
        } catch (Exception e) {
            log.error("Failed to log email", e);
        }
    }
    
    // ========== EMAIL CONTENT BUILDERS ==========
    
    /**
     * Build welcome email HTML content
     */
    private String buildWelcomeEmailContent(User user) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                             color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .button { display: inline-block; padding: 12px 30px; background: #667eea; 
                             color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .info-box { background: white; padding: 15px; border-left: 4px solid #667eea; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to Campus Resource Management!</h1>
                    </div>
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <p>Welcome to our Campus Resource Management System! Your account has been successfully created.</p>
                        
                        <div class="info-box">
                            <strong>Your Account Details:</strong><br>
                            Email: %s<br>
                            Role: %s<br>
                            %s
                        </div>
                        
                        <p><strong>You can now:</strong></p>
                        <ul>
                            <li>✓ Browse available campus resources</li>
                            <li>✓ Book resources for your needs</li>
                            <li>✓ Manage your bookings</li>
                            <li>✓ Rate and review resources</li>
                        </ul>
                        
                        <center>
                            <a href="http://localhost:8080/login.html" class="button">Login to Your Account</a>
                        </center>
                        
                        <p>If you have any questions, feel free to contact our support team.</p>
                        
                        <p>Best regards,<br>Campus Resource Management Team</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated email. Please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStudentId() != null ? "Student ID: " + user.getStudentId() : 
                user.getEmployeeId() != null ? "Employee ID: " + user.getEmployeeId() : ""
            );
    }
    
    /**
     * Build booking confirmation email HTML content
     */
    private String buildBookingConfirmationContent(Booking booking) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #2ecc71; color: white; padding: 30px; text-align: center; 
                             border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .booking-details { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; }
                    .detail-row { padding: 10px 0; border-bottom: 1px solid #eee; }
                    .detail-label { font-weight: bold; color: #555; }
                    .warning { background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; 
                              margin: 20px 0; border-radius: 5px; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✓ Booking Confirmed!</h1>
                    </div>
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <p>Your booking has been confirmed successfully!</p>
                        
                        <div class="booking-details">
                            <h3 style="margin-top: 0; color: #667eea;">📅 Booking Details</h3>
                            <div class="detail-row">
                                <span class="detail-label">Booking ID:</span> #%d
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Resource:</span> %s
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Date:</span> %s
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Time:</span> %s - %s
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Duration:</span> %d minutes
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Location:</span> %s
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Purpose:</span> %s
                            </div>
                            <div class="detail-row" style="border-bottom: none;">
                                <span class="detail-label">Attendees:</span> %d people
                            </div>
                        </div>
                        
                        <div class="warning">
                            <strong>⚠️ Important:</strong>
                            <ul style="margin: 10px 0;">
                                <li>Please arrive on time</li>
                                <li>Cancel at least 2 hours in advance if plans change</li>
                                <li>Resource capacity: %d people</li>
                            </ul>
                        </div>
                        
                        <p>Thank you for using our resource management system!</p>
                        
                        <p>Best regards,<br>Campus Resource Management Team</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated email. Please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                booking.getUser().getName(),
                booking.getId(),
                booking.getResource().getName(),
                booking.getBookingDate().format(DATE_FORMATTER),
                booking.getStartTime().format(TIME_FORMATTER),
                booking.getEndTime().format(TIME_FORMATTER),
                booking.getDuration(),
                booking.getResource().getLocation(),
                booking.getPurpose(),
                booking.getAttendeesCount(),
                booking.getResource().getCapacity()
            );
    }
    
    /**
     * Build booking reminder email HTML content
     */
    private String buildBookingReminderContent(Booking booking) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #f39c12; color: white; padding: 30px; text-align: center; 
                             border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .reminder-box { background: #fff3cd; padding: 20px; border-radius: 8px; 
                                   border: 2px solid #ffc107; margin: 20px 0; text-align: center; }
                    .booking-summary { background: white; padding: 15px; border-radius: 8px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔔 Booking Reminder</h1>
                    </div>
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <div class="reminder-box">
                            <h2 style="margin: 0; color: #f39c12;">Your booking is tomorrow!</h2>
                        </div>
                        
                        <p>This is a friendly reminder about your upcoming booking:</p>
                        
                        <div class="booking-summary">
                            <strong>Resource:</strong> %s<br>
                            <strong>Date:</strong> %s<br>
                            <strong>Time:</strong> %s - %s<br>
                            <strong>Location:</strong> %s
                        </div>
                        
                        <p>Please ensure you arrive on time. If you need to cancel, please do so as soon as possible.</p>
                        
                        <p>See you there!</p>
                        
                        <p>Best regards,<br>Campus Resource Management Team</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated email. Please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                booking.getUser().getName(),
                booking.getResource().getName(),
                booking.getBookingDate().format(DATE_FORMATTER),
                booking.getStartTime().format(TIME_FORMATTER),
                booking.getEndTime().format(TIME_FORMATTER),
                booking.getResource().getLocation()
            );
    }
    
    /**
     * Build cancellation email HTML content
     */
    private String buildCancellationEmailContent(Booking booking) {
        String cancelledBy = booking.getCancelledBy() != null ? 
                booking.getCancelledBy().getName() : "You";
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #e74c3c; color: white; padding: 30px; text-align: center; 
                             border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .cancellation-info { background: #fee; padding: 20px; border-radius: 8px; 
                                        border-left: 4px solid #e74c3c; margin: 20px 0; }
                    .booking-summary { background: white; padding: 15px; border-radius: 8px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Booking Cancelled</h1>
                    </div>
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <p>Your booking has been cancelled.</p>
                        
                        <div class="booking-summary">
                            <strong>Resource:</strong> %s<br>
                            <strong>Date:</strong> %s<br>
                            <strong>Time:</strong> %s - %s<br>
                            <strong>Location:</strong> %s
                        </div>
                        
                        <div class="cancellation-info">
                            <strong>Cancellation Details:</strong><br>
                            <strong>Cancelled By:</strong> %s<br>
                            <strong>Cancelled At:</strong> %s<br>
                            %s
                        </div>
                        
                        <p>You can make a new booking anytime by visiting our platform.</p>
                        
                        <p>Best regards,<br>Campus Resource Management Team</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated email. Please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                booking.getUser().getName(),
                booking.getResource().getName(),
                booking.getBookingDate().format(DATE_FORMATTER),
                booking.getStartTime().format(TIME_FORMATTER),
                booking.getEndTime().format(TIME_FORMATTER),
                booking.getResource().getLocation(),
                cancelledBy,
                booking.getCancelledAt().format(DATETIME_FORMATTER),
                booking.getCancellationReason() != null ? 
                    "<strong>Reason:</strong> " + booking.getCancellationReason() : ""
            );
    }
    
    /**
     * Build maintenance notification email content
     */
    private String buildMaintenanceNotificationContent(Booking booking, Resource resource) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #95a5a6; color: white; padding: 30px; text-align: center; 
                             border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .warning { background: #fff3cd; padding: 20px; border-radius: 8px; 
                              border-left: 4px solid #ffc107; margin: 20px 0; }
                    .maintenance-info { background: white; padding: 15px; border-radius: 8px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⚠️ Maintenance Scheduled</h1>
                    </div>
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <div class="warning">
                            <h3 style="margin-top: 0;">Important: Your Booking Has Been Cancelled</h3>
                            <p>A resource you have booked will undergo maintenance during your booking period.</p>
                        </div>
                        
                        <p><strong>Your Cancelled Booking:</strong></p>
                        <div class="maintenance-info">
                            <strong>Resource:</strong> %s<br>
                            <strong>Your Booking Date:</strong> %s<br>
                            <strong>Your Booking Time:</strong> %s - %s
                        </div>
                        
                        <p><strong>Maintenance Schedule:</strong></p>
                        <div class="maintenance-info">
                            <strong>From:</strong> %s<br>
                            <strong>To:</strong> %s<br>
                            <strong>Reason:</strong> %s
                        </div>
                        
                        <p>We sincerely apologize for any inconvenience this may cause. Please book another resource or reschedule for a different time.</p>
                        
                        <p>Thank you for your understanding!</p>
                        
                        <p>Best regards,<br>Campus Resource Management Team</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated email. Please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                booking.getUser().getName(),
                resource.getName(),
                booking.getBookingDate().format(DATE_FORMATTER),
                booking.getStartTime().format(TIME_FORMATTER),
                booking.getEndTime().format(TIME_FORMATTER),
                resource.getMaintenanceStart().format(DATETIME_FORMATTER),
                resource.getMaintenanceEnd().format(DATETIME_FORMATTER),
                resource.getMaintenanceReason()
            );
    }
    
    /**
     * Build completion email with rating request
     */
    private String buildCompletionEmailContent(Booking booking) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #3498db; color: white; padding: 30px; text-align: center; 
                             border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .rating-request { background: white; padding: 25px; border-radius: 8px; 
                                     margin: 20px 0; text-align: center; border: 2px solid #3498db; }
                    .button { display: inline-block; padding: 12px 30px; background: #3498db; 
                             color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✓ Booking Completed!</h1>
                    </div>
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <p>Your booking has been completed!</p>
                        
                        <p><strong>Booking Details:</strong></p>
                        <div style="background: white; padding: 15px; border-radius: 8px; margin: 20px 0;">
                            <strong>Resource:</strong> %s<br>
                            <strong>Date:</strong> %s<br>
                            <strong>Time:</strong> %s - %s
                        </div>
                        
                        <div class="rating-request">
                            <h3 style="color: #3498db; margin-top: 0;">How was your experience?</h3>
                            <p>We'd love to hear your feedback! Please take a moment to rate this resource.</p>
                            <center>
                                <a href="http://localhost:8080/rate-resource.html?bookingId=%d" class="button">
                                    ⭐ Rate Now
                                </a>
                            </center>
                            <p style="font-size: 13px; color: #666; margin-top: 15px;">
                                Your feedback helps us improve our services and helps other users choose the best resources.
                            </p>
                        </div>
                        
                        <p>Thank you for using our resource management system!</p>
                        
                        <p>Best regards,<br>Campus Resource Management Team</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated email. Please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                booking.getUser().getName(),
                booking.getResource().getName(),
                booking.getBookingDate().format(DATE_FORMATTER),
                booking.getStartTime().format(TIME_FORMATTER),
                booking.getEndTime().format(TIME_FORMATTER),
                booking.getId()
            );
    }
    
    /**
     * Build no-show notification email
     */
    private String buildNoShowNotificationContent(Booking booking) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #e67e22; color: white; padding: 30px; text-align: center; 
                             border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .noshow-info { background: #fff3cd; padding: 20px; border-radius: 8px; 
                                   border-left: 4px solid #e67e22; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Missed Booking Notice</h1>
                    </div>
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <div class="noshow-info">
                            <h3 style="margin-top: 0;">You missed your booking</h3>
                            <p>Our records show that you did not check in for your scheduled booking.</p>
                        </div>
                        
                        <p><strong>Booking Details:</strong></p>
                        <div style="background: white; padding: 15px; border-radius: 8px; margin: 20px 0;">
                            <strong>Resource:</strong> %s<br>
                            <strong>Date:</strong> %s<br>
                            <strong>Time:</strong> %s - %s
                        </div>
                        
                        <p>Please ensure to:</p>
                        <ul>
                            <li>Cancel bookings in advance if you cannot attend</li>
                            <li>Arrive on time for your reservations</li>
                            <li>Help us maintain fair access to resources for everyone</li>
                        </ul>
                        
                        <p>If you have any concerns, please contact our support team.</p>
                        
                        <p>Best regards,<br>Campus Resource Management Team</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated email. Please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                booking.getUser().getName(),
                booking.getResource().getName(),
                booking.getBookingDate().format(DATE_FORMATTER),
                booking.getStartTime().format(TIME_FORMATTER),
                booking.getEndTime().format(TIME_FORMATTER)
            );
    }
    
    /**
     * Build admin cancellation notification
     */
    private String buildAdminCancellationNotification(Booking booking) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #34495e; color: white; padding: 30px; text-align: center;
        		 border-radius: 10px 10px 0 0; }
                .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                .info-box { background: white; padding: 15px; border-radius: 8px; margin: 20px 0; }
                .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Booking Cancellation Confirmation</h1>
                </div>
                <div class="content">
                    <p>Dear Admin,</p>
                    
                    <p>You have successfully cancelled the following booking:</p>
                    
                    <div class="info-box">
                        <strong>Booking ID:</strong> #%d<br>
                        <strong>User:</strong> %s (%s)<br>
                        <strong>Resource:</strong> %s<br>
                        <strong>Date:</strong> %s<br>
                        <strong>Time:</strong> %s - %s<br>
                        <strong>Cancellation Reason:</strong> %s
                    </div>
                    
                    <p>The user has been notified via email about this cancellation.</p>
                    
                    <p>Best regards,<br>Campus Resource Management System</p>
                </div>
                <div class="footer">
                    <p>This is an automated email. Please do not reply.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(
            booking.getId(),
            booking.getUser().getName(),
            booking.getUser().getEmail(),
            booking.getResource().getName(),
            booking.getBookingDate().format(DATE_FORMATTER),
            booking.getStartTime().format(TIME_FORMATTER),
            booking.getEndTime().format(TIME_FORMATTER),
            booking.getCancellationReason() != null ? booking.getCancellationReason() : "No reason provided"
        );
}
        }