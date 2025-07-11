package org.example.controller;

import org.example.dto.UserDto;
import org.example.model.UserData;
import org.example.model.UserForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;
import org.example.exception.ApiException;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserDto userDto;

    @Autowired
    private AuthenticationManager authenticationManager;

    // Signup API
    @PostMapping("/signup")
    public void signup(@RequestBody UserForm form) {
        try {
            userDto.signup(form);
        } catch (ApiException e) {
            // Re-throw ApiException as-is
            throw e;
        } catch (Exception e) {
            throw new ApiException("Signup failed: " + e.getMessage());
        }
    }

    // Login API with custom authentication (original working method)
    @PostMapping("/login")
    public ResponseEntity<UserData> login(@RequestBody UserForm form, HttpServletRequest request) {
        try {
            // Invalidate any existing session
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            
            // Create a new session
            HttpSession newSession = request.getSession(true);

            UserData userData = userDto.login(form);
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(form.getEmail(), form.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Store user email and role in session as fallback
            newSession.setAttribute("userEmail", form.getEmail());
            // Get the user role from the authentication authorities
            String userRole = authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("ROLE_USER");
            newSession.setAttribute("userRole", userRole);

            return ResponseEntity.ok(userData);
        } catch (ApiException e) {
            // Re-throw ApiException as-is
            throw e;
        } catch (Exception e) {
            throw new ApiException("Login failed: " + e.getMessage());
        }
    }

    // Logout API
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Get current user info before logout
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            HttpSession session = request.getSession(false);
            
            // Check cookies BEFORE logout
            Cookie[] cookiesBefore = request.getCookies();
            
            // 1. Clear Spring Security context FIRST
            SecurityContextHolder.clearContext();

            // 2. Invalidate the session IMMEDIATELY
            if (session != null) {
                session.invalidate();
            }

            // 3. Clear all cookies present in the request
            if (cookiesBefore != null) {
                for (Cookie cookie : cookiesBefore) {
                    // Clear with original path and domain if they exist
                    Cookie cleared = new Cookie(cookie.getName(), "");
                    cleared.setPath(cookie.getPath() != null ? cookie.getPath() : "/");
                    cleared.setDomain(cookie.getDomain()); // Keep original domain (can be null)
                    cleared.setMaxAge(0);
                    cleared.setHttpOnly(true);
                    cleared.setSecure(false); // Set to true if using HTTPS
                    response.addCookie(cleared);
                    
                    // Also clear with root path to ensure it's gone
                    if (cookie.getPath() == null || !cookie.getPath().equals("/")) {
                        Cookie rootCleared = new Cookie(cookie.getName(), "");
                        rootCleared.setPath("/");
                        rootCleared.setDomain(cookie.getDomain());
                        rootCleared.setMaxAge(0);
                        rootCleared.setHttpOnly(true);
                        rootCleared.setSecure(false);
                        response.addCookie(rootCleared);
                    }
                }
            }

            // 4. Explicitly clear JSESSIONID and common variants with multiple paths/domains
            String[] sessionCookieNames = {"JSESSIONID", "jsessionid", "SESSION", "session"};
            String[] paths = {"/", "/api", ""};
            for (String name : sessionCookieNames) {
                for (String path : paths) {
                    Cookie c = new Cookie(name, "");
                    c.setPath(path);
                    c.setMaxAge(0);
                    c.setHttpOnly(true);
                    c.setSecure(false);
                    response.addCookie(c);
                }
                // Also try with explicit domain
                Cookie domainCookie = new Cookie(name, "");
                domainCookie.setPath("/");
                domainCookie.setDomain(request.getServerName());
                domainCookie.setMaxAge(0);
                domainCookie.setHttpOnly(true);
                domainCookie.setSecure(false);
                response.addCookie(domainCookie);
            }

            // Explicitly clear __reveal_ut cookie with all common paths and domains
            String[] revealPaths = {"/", "/api", ""};
            for (String path : revealPaths) {
                Cookie reveal = new Cookie("__reveal_ut", "");
                reveal.setPath(path);
                reveal.setMaxAge(0);
                reveal.setHttpOnly(true);
                reveal.setSecure(false); // Set to true if using HTTPS
                response.addCookie(reveal);
            }
            // Also try with explicit domain
            Cookie revealDomain = new Cookie("__reveal_ut", "");
            revealDomain.setPath("/");
            revealDomain.setDomain(request.getServerName());
            revealDomain.setMaxAge(0);
            revealDomain.setHttpOnly(true);
            revealDomain.setSecure(false);
            response.addCookie(revealDomain);
            
            // Also try with null domain (session cookie)
            Cookie revealNullDomain = new Cookie("__reveal_ut", "");
            revealNullDomain.setPath("/");
            revealNullDomain.setDomain(null);
            revealNullDomain.setMaxAge(0);
            revealNullDomain.setHttpOnly(true);
            revealNullDomain.setSecure(false);
            response.addCookie(revealNullDomain);

            // 5. Add Clear-Site-Data header for extra safety
            response.setHeader("Clear-Site-Data", "\"cache\", \"cookies\", \"storage\", \"executionContexts\"");

            // 6. Add Cache-Control headers to prevent caching of authenticated state
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");

            return ResponseEntity.ok("Logged out successfully");
        } catch (ApiException e) {
            // Re-throw ApiException as-is
            throw e;
        } catch (Exception e) {
            throw new ApiException("Logout failed: " + e.getMessage());
        }
    }

    // Get current user info
    @GetMapping("/me")
    public ResponseEntity<UserData> getCurrentUser(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            HttpSession session = request.getSession(false);
            
            String userEmail = null;
            
            // Only use SecurityContext - don't fall back to session during logout
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getName())) {
                userEmail = authentication.getName();
            }
            
            if (userEmail != null) {
                try {
                    UserData userData = userDto.getUserByEmail(userEmail);
                    return ResponseEntity.ok(userData);
                } catch (Exception e) {
                    throw new ApiException("User not found");
                }
            } else {
                throw new ApiException("User not authenticated");
            }
        } catch (ApiException e) {
            // Re-throw ApiException as-is
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get current user: " + e.getMessage());
        }
    }

    // Test endpoint to create a user programmatically
    @PostMapping("/test-create")
    public ResponseEntity<String> testCreateUser() {
        try {
            UserForm form = new UserForm();
            form.setEmail("admin@example.com");
            form.setPassword("admin123");
            form.setRole("SUPERVISOR");
            userDto.signup(form);
            return ResponseEntity.ok("User created successfully");
        } catch (ApiException e) {
            // Re-throw ApiException as-is
            throw e;
        } catch (Exception e) {
            throw new ApiException("Error creating user: " + e.getMessage());
        }
    }

    // Check authentication status
    @GetMapping("/auth-status")
    public ResponseEntity<String> checkAuthStatus(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        HttpSession session = request.getSession(false);
        
        StringBuilder status = new StringBuilder();
        status.append("Authentication: ").append(authentication != null ? authentication.getName() : "null");
        status.append(", Authenticated: ").append(authentication != null && authentication.isAuthenticated());
        status.append(", Session: ").append(session != null ? session.getId() : "null");
        status.append(", Session Valid: ").append(session != null && !session.isNew());
        
        // Add detailed cookie information
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            status.append(", Cookies: [");
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                status.append(cookie.getName()).append("=").append(cookie.getValue());
                if (i < cookies.length - 1) {
                    status.append(", ");
                }
            }
            status.append("]");
        } else {
            status.append(", Cookies: none");
        }
        
        return ResponseEntity.ok(status.toString());
    }
}
