package za.co.int216d.carwash.booking.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Email notification service
 * Sends emails for membership events
 */
@Service
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String fromName;
    private final String adminEmail;
    private final String adminFromName;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        this.fromAddress = System.getenv().getOrDefault("MAIL_FROM_ADDRESS", "noreply@int216d.co.za");
        this.fromName = System.getenv().getOrDefault("MAIL_FROM_NAME", "INT216D Smart Car Wash");
        this.adminEmail = System.getenv().getOrDefault("MAIL_ADMIN", "");
        this.adminFromName = System.getenv().getOrDefault("MAIL_ADMIN_NAME", "INT216D Admin Alerts");
    }

    /**
     * Send subscription confirmation email
     */
    public void sendSubscriptionEmail(String toEmail, String clientName, String planName, Double monthlyPrice) {
        String subject = "Welcome to " + planName + " Membership!";
        String text = buildSubscriptionEmailBody(clientName, planName, monthlyPrice);
        sendEmail(toEmail, subject, text);
    }

    /**
     * Send membership renewal email
     */
    public void sendRenewalEmail(String toEmail, String clientName, String planName, LocalDateTime expiryDate) {
        String subject = "Your " + planName + " Membership Has Been Renewed";
        String text = buildRenewalEmailBody(clientName, planName, expiryDate);
        sendEmail(toEmail, subject, text);
    }

    /**
     * Send membership expiry warning email (N days before expiry)
     */
    public void sendExpiryWarningEmail(String toEmail, String clientName, Integer daysUntilExpiry) {
        String subject = "Your Membership Expires in " + daysUntilExpiry + " Days";
        String text = buildExpiryWarningEmailBody(clientName, daysUntilExpiry);
        sendEmail(toEmail, subject, text);
    }

    /**
     * Send membership expiry email
     */
    public void sendExpiryEmail(String toEmail, String clientName, String planName) {
        String subject = "Your " + planName + " Membership Has Expired";
        String text = buildExpiryEmailBody(clientName, planName);
        sendEmail(toEmail, subject, text);
    }

    /**
     * Send membership cancelled email
     */
    public void sendCancellationEmail(String toEmail, String clientName, String planName) {
        String subject = "Your " + planName + " Membership Has Been Cancelled";
        String text = buildCancellationEmailBody(clientName, planName);
        sendEmail(toEmail, subject, text);
    }

    /**
     * Send membership upgrade email
     */
    public void sendUpgradeEmail(String toEmail, String clientName, String oldPlanName, String newPlanName, Double monthlyPrice) {
        String subject = "Your Membership Has Been Upgraded to " + newPlanName + "!";
        String text = "Hi " + clientName + ",\n\n" +
            "Your membership has been upgraded from " + oldPlanName + " to " + newPlanName + ".\n\n" +
            "New Plan: " + newPlanName + "\n" +
            "Monthly Price: R" + String.format("%.2f", monthlyPrice) + "\n\n" +
            "Enjoy your upgraded benefits!\n\n" +
            "Best regards,\n" +
            "INT216D Smart Car Wash";
        sendEmail(toEmail, subject, text);
    }

    /**
     * Send booking confirmation email
     */
    public void sendBookingConfirmationEmail(String toEmail, String fullName, String serviceType, String serviceCode, String location, java.time.LocalDateTime scheduledAt, String paymentReference) {
        String subject = "Booking Confirmed - " + serviceType + " Wash";
        String text = "Hi " + fullName + ",\n\n" +
            "Your booking has been confirmed!\n\n" +
            "Service: " + serviceCode + "\n" +
            "Type: " + serviceType + "\n" +
            "Location: " + location + "\n" +
            "Date & Time: " + scheduledAt + "\n" +
            "Payment Ref: " + (paymentReference != null ? paymentReference : "N/A") + "\n\n" +
            "We look forward to serving you.\n\n" +
            "Best regards,\n" +
            "INT216D Smart Car Wash";
        sendEmail(toEmail, subject, text);
    }

    /**
     * Send admin notification for new booking
     */
    public void sendAdminNewBookingEmail(String clientName, String clientEmail, String serviceType, 
                                          String serviceCode, String location, java.time.LocalDateTime scheduledAt, 
                                          String paymentReference) {
        if (adminEmail == null || adminEmail.isBlank()) return;
        String subject = "New Booking - " + serviceType + " Wash - " + clientName;
        String text = "A new booking has been placed.\n\n" +
            "Customer: " + clientName + "\n" +
            "Email: " + clientEmail + "\n" +
            "Service: " + serviceCode + "\n" +
            "Type: " + serviceType + "\n" +
            "Location: " + location + "\n" +
            "Date & Time: " + scheduledAt + "\n" +
            "Payment Ref: " + (paymentReference != null ? paymentReference : "N/A") + "\n\n" +
            "Login to the admin dashboard to manage.\n\n" +
            "INT216D Smart Car Wash";
        sendEmail(adminEmail, subject, text, adminFromName);
    }

    /**
     * Send booking cancellation email
     */
    public void sendBookingCancellationEmail(String toEmail, String fullName, String serviceCode, java.time.LocalDateTime scheduledAt) {
        String subject = "Booking Cancelled - " + serviceCode;
        String text = "Hi " + fullName + ",\n\n" +
            "Your booking has been cancelled.\n\n" +
            "Service: " + serviceCode + "\n" +
            "Date & Time: " + scheduledAt + "\n\n" +
            "If you would like to rebook, please visit our website.\n\n" +
            "Best regards,\n" +
            "INT216D Smart Car Wash";
        sendEmail(toEmail, subject, text);
    }

    /**
     * Send generic email
     */
    private void sendEmail(String toEmail, String subject, String body, String displayName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(displayName + " <" + fromAddress + ">");
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {} with subject: {}", toEmail, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    private void sendEmail(String toEmail, String subject, String body) {
        sendEmail(toEmail, subject, body, fromName);
    }

    private String buildSubscriptionEmailBody(String clientName, String planName, Double price) {
        return "Hi " + clientName + ",\n\n" +
            "Welcome to " + planName + " membership! Your subscription is now active.\n\n" +
            "Plan: " + planName + "\n" +
            "Monthly Price: R" + String.format("%.2f", price) + "\n\n" +
            "You can now enjoy all the benefits of your membership.\n\n" +
            "Best regards,\n" +
            "INT216D Smart Car Wash";
    }

    private String buildRenewalEmailBody(String clientName, String planName, java.time.LocalDateTime expiryDate) {
        return "Hi " + clientName + ",\n\n" +
            "Your " + planName + " membership has been renewed successfully!\n\n" +
            "New Expiry Date: " + expiryDate + "\n\n" +
            "Thank you for continuing with us.\n\n" +
            "Best regards,\n" +
            "INT216D Smart Car Wash";
    }

    private String buildExpiryWarningEmailBody(String clientName, Integer daysUntilExpiry) {
        return "Hi " + clientName + ",\n\n" +
            "Your membership will expire in " + daysUntilExpiry + " days.\n\n" +
            "To continue enjoying our services, please renew your membership.\n\n" +
            "Best regards,\n" +
            "INT216D Smart Car Wash";
    }

    private String buildExpiryEmailBody(String clientName, String planName) {
        return "Hi " + clientName + ",\n\n" +
            "Your " + planName + " membership has expired.\n\n" +
            "To continue using our services, please subscribe to a membership plan.\n\n" +
            "Best regards,\n" +
            "INT216D Smart Car Wash";
    }

    private String buildCancellationEmailBody(String clientName, String planName) {
        return "Hi " + clientName + ",\n\n" +
            "Your " + planName + " membership has been cancelled.\n\n" +
            "If you have any questions, please contact our support team.\n\n" +
            "Best regards,\n" +
            "INT216D Smart Car Wash";
    }
}
