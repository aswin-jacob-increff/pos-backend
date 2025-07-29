package org.example.util;

import org.example.exception.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for handling authentication-related operations.
 * Provides centralized methods for accessing user information from the security context.
 */
public class AuthHelper {

    /**
     * Gets the current user ID (email) from the security context.
     * 
     * @return The current user's email address as the user ID
     * @throws ApiException if the user is not authenticated
     */
    public static String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        throw new ApiException("User not authenticated");
    }

    /**
     * Gets the current user ID (email) from the provided authentication object.
     * 
     * @param authentication The authentication object
     * @return The current user's email address as the user ID
     * @throws ApiException if the user is not authenticated
     */
    public static String getUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        throw new ApiException("User not authenticated");
    }

    /**
     * Checks if the current user is authenticated.
     * 
     * @return true if the user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Checks if the provided authentication object represents an authenticated user.
     * 
     * @param authentication The authentication object
     * @return true if the user is authenticated, false otherwise
     */
    public static boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Gets the current authentication object from the security context.
     * 
     * @return The current authentication object, or null if not authenticated
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
} 