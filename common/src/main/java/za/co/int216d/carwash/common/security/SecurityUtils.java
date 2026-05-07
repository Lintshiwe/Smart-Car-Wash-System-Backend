package za.co.int216d.carwash.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Utility class for security operations
 */
@Component
public class SecurityUtils {

    /**
     * Get the current authenticated user ID as UUID
     * @return User ID from the security context
     */
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof AuthPrincipal) {
                return ((AuthPrincipal) principal).userId();
            }
        }
        
        return null;
    }

    /**
     * Get the current authenticated user ID as Long
     * Converts UUID to Long using XOR of most and least significant bits
     * for better distribution (reduces collision probability)
     * @return User ID as Long from the security context
     */
    public Long getCurrentUserIdAsLong() {
        UUID userId = getCurrentUserId();
        if (userId != null) {
            return userId.getMostSignificantBits() ^ userId.getLeastSignificantBits();
        }
        return null;
    }

    /**
     * Get the current authenticated user role
     * @return Role from the security context
     */
    public Role getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof AuthPrincipal) {
                return ((AuthPrincipal) principal).role();
            }
        }
        
        return null;
    }

    /**
     * Check if user is authenticated
     * @return true if user is authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Check if user has a specific role
     * @param role the role to check
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(Role role) {
        Role currentRole = getCurrentUserRole();
        return currentRole != null && currentRole.equals(role);
    }
}
