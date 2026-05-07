package za.co.int216d.carwash.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import za.co.int216d.carwash.auth.config.AuthAppProperties;

import jakarta.mail.internet.InternetAddress;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final AuthAppProperties props;

    @Async
    public void sendOtp(String toEmail, String otp, int ttlMinutes) {
        String project = props.getMail().getProjectName();
        String subject = project + " - Verify your email";
        String body = """
            Hello,

            Your %s verification code is: %s

            This code expires in %d minutes.

            If you did not request this code, please ignore this email.
            """.formatted(project, otp, ttlMinutes);
        safeSend(toEmail, subject, body);
    }

    @Async
    public void sendAccountCreated(String toEmail) {
        String project = props.getMail().getProjectName();
        String subject = project + " - Account created";
        String body = """
            Hello,

            Your account has been created successfully on %s.

            Please verify your email address using the OTP code we sent you.
            """.formatted(project);
        safeSend(toEmail, subject, body);
    }

    @Async
    public void sendWelcome(String toEmail) {
        String project = props.getMail().getProjectName();
        String subject = project + " - Email verified";
        String body = """
            Hello,

            Your email is verified and your account is now active.

            You can log in and start booking services on %s.
            """.formatted(project);
        safeSend(toEmail, subject, body);
    }

    @Async
    public void sendPasswordReset(String toEmail, String resetCode, int ttlMinutes) {
        String project = props.getMail().getProjectName();
        String subject = project + " - Password reset request";
        String body = """
            Hello,

            We received a password reset request for your %s account.

            Reset code: %s
            This code expires in %d minutes.

            If you did not request this, ignore this message.
            """.formatted(project, resetCode, ttlMinutes);
        safeSend(toEmail, subject, body);
    }

    @Async
    public void sendPasswordChanged(String toEmail) {
        String project = props.getMail().getProjectName();
        String subject = project + " - Password changed";
        String body = """
            Hello,

            Your password was changed successfully for %s.

            If this was not you, contact support immediately.
            """.formatted(project);
        safeSend(toEmail, subject, body);
    }

    @Async
    public void sendSecurityAlert(String toEmail, String activity) {
        String project = props.getMail().getProjectName();
        String subject = project + " - Security alert";
        String body = """
            Hello,

            Security alert for your %s account:
            %s

            If this activity is unexpected, secure your account immediately.
            """.formatted(project, activity);
        safeSend(toEmail, subject, body);
    }

    void safeSend(String toEmail, String subject, String body) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, false);
            helper.setFrom(new InternetAddress(
                props.getMail().getFromAddress(),
                props.getMail().getFromName(),
                StandardCharsets.UTF_8.name()));
            helper.setReplyTo(props.getMail().getFromAddress());
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send mail to {}: {}", toEmail, ex.getMessage());
        }
    }
}
