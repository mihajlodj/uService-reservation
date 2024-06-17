package ftn.reservationservice.utils;

import ftn.reservationservice.exception.exceptions.AuthorizationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.UUID;

public class AuthUtils {

    public static String getLoggedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return (String) authentication.getPrincipal();
        }

        throw new AuthorizationException("Not logged in");
    }

    public static UUID getLoggedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object details = authentication.getDetails();
            if (details instanceof Map<?, ?> detailsMap) {
                String userId = (String) detailsMap.get("userId");
                return UUID.fromString(userId);
            }
        }

        throw new AuthorizationException("Not logged in");
    }

}
