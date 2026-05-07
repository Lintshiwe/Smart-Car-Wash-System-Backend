package za.co.int216d.carwash.auth.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.int216d.carwash.auth.config.AuthAppProperties;
import za.co.int216d.carwash.auth.dto.LoginRequest;
import za.co.int216d.carwash.auth.dto.LoginResponse;
import za.co.int216d.carwash.auth.dto.RegisterRequest;
import za.co.int216d.carwash.auth.dto.RegisterResponse;
import za.co.int216d.carwash.auth.dto.ResendOtpRequest;
import za.co.int216d.carwash.auth.dto.VerifyEmailRequest;
import za.co.int216d.carwash.auth.dto.PasswordResetRequest;
import za.co.int216d.carwash.auth.dto.PasswordResetConfirmRequest;
import za.co.int216d.carwash.auth.service.AuthService;
import za.co.int216d.carwash.common.web.ApiResponse;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthAppProperties props;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Account created", response));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.message("Email verified"));
    }

    @PostMapping("/verify-email/resend")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        authService.resendOtp(request.email());
        return ResponseEntity.ok(ApiResponse.message("A new verification code has been sent to your email"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request,
                                                            HttpServletResponse response) {
        AuthService.LoginResult result = authService.login(request);
        attachRefreshCookie(response, result.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok("Login successful", result.response()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(HttpServletRequest request,
                                                               HttpServletResponse response) {
        String cookie = readRefreshCookie(request);
        AuthService.LoginResult result = authService.refresh(cookie);
        attachRefreshCookie(response, result.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok(result.response()));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.message("If the email exists, a reset code has been sent"));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.message("Password has been reset successfully"));
    }

    private String readRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        String name = props.getCookie().getRefreshName();
        for (var c : request.getCookies()) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    private void attachRefreshCookie(HttpServletResponse response, String value) {
        AuthAppProperties.Cookie cfg = props.getCookie();
        ResponseCookie cookie = ResponseCookie.from(cfg.getRefreshName(), value)
                .httpOnly(true)
                .secure(cfg.isSecure())
                .sameSite(cfg.getSameSite())
                .path(cfg.getPath())
                .maxAge(Duration.ofDays(7))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
