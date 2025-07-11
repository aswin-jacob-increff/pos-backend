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
        userDto.signup(form);
    }

    // Login API with custom authentication (original working method)
    @PostMapping("/login")
    public ResponseEntity<UserData> login(@RequestBody UserForm form, HttpServletRequest request) {
        try {
            System.out.println("=== Login attempt for: " + form.getEmail() + " ===");
            
            // Invalidate any existing session
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                System.out.println("Invalidating old session: " + oldSession.getId());
                oldSession.invalidate();
            }
            
            // Create a new session
            HttpSession newSession = request.getSession(true);
            System.out.println("Created new session: " + newSession.getId());

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
            
            System.out.println("Authentication set for: " + authentication.getName());
            System.out.println("Session ID after login: " + newSession.getId());
            System.out.println("Session is new: " + newSession.isNew());

            return ResponseEntity.ok(userData);
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401).body(null);
        }
    }

    // Logout API
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 1. Clear Spring Security context
            SecurityContextHolder.clearContext();

            // 2. Invalidate the session
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            // 3. Clear all cookies present in the request
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    Cookie cleared = new Cookie(cookie.getName(), "");
                    cleared.setPath("/");
                    cleared.setMaxAge(0);
                    cleared.setHttpOnly(true);
                    cleared.setSecure(false); // Set to true if using HTTPS
                    response.addCookie(cleared);
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

            // 5. Add Clear-Site-Data header for extra safety
            response.setHeader("Clear-Site-Data", "\"cache\", \"cookies\", \"storage\", \"executionContexts\"");

            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Logout failed: " + e.getMessage());
        }
    }

    // Get current user info
    @GetMapping("/me")
    public ResponseEntity<UserData> getCurrentUser(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        HttpSession session = request.getSession(false);
        
        // Debug logging
        System.out.println("=== /api/user/me called ===");
        System.out.println("Authentication: " + (authentication != null ? authentication.getName() : "null"));
        System.out.println("Authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("Session: " + (session != null ? session.getId() : "null"));
        System.out.println("Session Valid: " + (session != null && !session.isNew()));
        
        // Check cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            System.out.println("Cookies received:");
            for (Cookie cookie : cookies) {
                System.out.println("  " + cookie.getName() + "=" + cookie.getValue());
            }
        } else {
            System.out.println("No cookies received");
        }
        
        String userEmail = null;
        
        // Try SecurityContext first
        if (authentication != null && authentication.isAuthenticated()) {
            userEmail = authentication.getName();
            System.out.println("User from SecurityContext: " + userEmail);
        }
        // Fallback: try to get user from session
        else if (session != null && !session.isNew()) {
            userEmail = (String) session.getAttribute("userEmail");
            System.out.println("User from session: " + userEmail);
        }
        
        if (userEmail != null) {
            try {
                UserData userData = userDto.getUserByEmail(userEmail);
                System.out.println("User found: " + userEmail);
                return ResponseEntity.ok(userData);
            } catch (Exception e) {
                System.out.println("Error getting user by email: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.notFound().build();
            }
        } else {
            System.out.println("No valid authentication found");
            return ResponseEntity.status(401).build();
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
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
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
