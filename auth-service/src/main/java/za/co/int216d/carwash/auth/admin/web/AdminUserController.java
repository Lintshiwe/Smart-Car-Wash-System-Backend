package za.co.int216d.carwash.auth.admin.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.co.int216d.carwash.auth.domain.User;
import za.co.int216d.carwash.auth.repository.UserRepository;
import za.co.int216d.carwash.common.security.Role;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        log.info("GET /admin/users - Fetching all users");
        List<Map<String, Object>> users = userRepository.findAll().stream()
            .map(this::toUserResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("users", users));
    }

    @PostMapping("/{userId}/role")
    public ResponseEntity<Map<String, String>> changeUserRole(
            @PathVariable UUID userId,
            @RequestBody Map<String, String> request) {
        log.info("POST /admin/users/{}/role - Changing role", userId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        String newRole = request.get("role");
        try {
            user.setRole(Role.valueOf(newRole.toUpperCase()));
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Role updated to " + newRole));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role: " + newRole));
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable UUID userId) {
        log.info("DELETE /admin/users/{} - Deleting user", userId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    private Map<String, Object> toUserResponse(User user) {
        return Map.of(
            "id", user.getId().toString(),
            "email", user.getEmail(),
            "role", user.getRole().name(),
            "email_verified", user.isEmailVerified(),
            "is_active", user.isActive(),
            "created_at", user.getCreatedAt().toString()
        );
    }
}
