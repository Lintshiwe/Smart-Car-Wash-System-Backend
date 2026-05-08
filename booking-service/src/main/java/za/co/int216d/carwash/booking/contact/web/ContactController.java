package za.co.int216d.carwash.booking.contact.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/contact")
@Slf4j
public class ContactController {

    private final JavaMailSender mailSender;
    private final String adminEmail;
    private final String fromAddress;
    private final String fromName;

    public ContactController(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        this.adminEmail = System.getenv().getOrDefault("MAIL_ADMIN", "admin@int216d.co.za");
        this.fromAddress = System.getenv().getOrDefault("MAIL_FROM_ADDRESS", "noreply@int216d.co.za");
        this.fromName = System.getenv().getOrDefault("MAIL_FROM_NAME", "INT216D Smart Car Wash");
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> submitContact(@RequestBody Map<String, String> request) {
        log.info("POST /contact - Received contact form submission");
        
        String name = request.getOrDefault("name", "Unknown");
        String email = request.getOrDefault("email", "");
        String phone = request.getOrDefault("phone", "");
        String subject = request.getOrDefault("subject", "Contact Form Submission");
        String message = request.getOrDefault("message", "");

        if (email.isBlank() || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and message are required"));
        }

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromName + " <" + fromAddress + ">");
            mailMessage.setTo(adminEmail);
            mailMessage.setReplyTo(email);
            mailMessage.setSubject("Contact Form: " + subject);
            mailMessage.setText("Name: " + name + "\n" +
                               "Email: " + email + "\n" +
                               "Phone: " + phone + "\n" +
                               "Subject: " + subject + "\n\n" +
                               "Message:\n" + message);
            mailSender.send(mailMessage);
            log.info("Contact form email sent to admin from {}", email);
        } catch (Exception e) {
            log.error("Failed to send contact form email: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to send message. Please try again later."));
        }

        return ResponseEntity.ok(Map.of("message", "Message sent successfully"));
    }
}
