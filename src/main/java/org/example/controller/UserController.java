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

            return ResponseEntity.ok(userData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body(null);
        }
    }

    // Logout API
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            System.out.println("=== Starting Enhanced Logout Process ===");
            
            // 1. Clear Spring Security context first
            SecurityContextHolder.clearContext();
            System.out.println("✓ Spring Security context cleared");
            
            // 2. Invalidate the session
            HttpSession session = request.getSession(false);
            if (session != null) {
                String sessionId = session.getId();
                session.invalidate();
                System.out.println("✓ Session invalidated: " + sessionId);
            } else {
                System.out.println("✓ No session to invalidate");
            }
            
            // 3. Enhanced cookie clearing with specific handling for JSESSIONID
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                System.out.println("Found " + cookies.length + " cookies to clear:");
                for (Cookie cookie : cookies) {
                    System.out.println("  - " + cookie.getName() + " = " + cookie.getValue());
                    
                    // Special handling for JSESSIONID cookies (including node_ prefix)
                    if (cookie.getName().equals("JSESSIONID") || 
                        cookie.getName().contains("SESSION") ||
                        cookie.getValue() != null && cookie.getValue().startsWith("node_")) {
                        
                        System.out.println("    Special handling for session cookie: " + cookie.getName());
                        
                        // Clear with multiple paths and domains
                        String[] paths = {"/", "/api", ""};
                        for (String path : paths) {
                            Cookie newCookie = new Cookie(cookie.getName(), "");
                            newCookie.setPath(path);
                            newCookie.setMaxAge(0);
                            newCookie.setHttpOnly(true);
                            newCookie.setSecure(false);
                            response.addCookie(newCookie);
                        }
                        
                        // Also try with explicit domain
                        Cookie domainCookie = new Cookie(cookie.getName(), "");
                        domainCookie.setPath("/");
                        domainCookie.setDomain(request.getServerName());
                        domainCookie.setMaxAge(0);
                        domainCookie.setHttpOnly(true);
                        domainCookie.setSecure(false);
                        response.addCookie(domainCookie);
                    } else {
                        // Clear regular cookies
                        Cookie newCookie = new Cookie(cookie.getName(), "");
                        newCookie.setPath("/");
                        newCookie.setMaxAge(0);
                        newCookie.setHttpOnly(true);
                        newCookie.setSecure(false);
                        response.addCookie(newCookie);
                    }
                }
            } else {
                System.out.println("✓ No cookies found to clear");
            }
            
            // 4. Force clear JSESSIONID with multiple approaches
            String[] jsessionVariants = {"JSESSIONID", "jsessionid", "JSESSION", "jsession"};
            for (String jsessionName : jsessionVariants) {
                Cookie forceCookie = new Cookie(jsessionName, "");
                forceCookie.setPath("/");
                forceCookie.setMaxAge(0);
                forceCookie.setHttpOnly(true);
                forceCookie.setSecure(false);
                response.addCookie(forceCookie);
                
                // Also with domain
                Cookie domainCookie = new Cookie(jsessionName, "");
                domainCookie.setPath("/");
                domainCookie.setDomain(request.getServerName());
                domainCookie.setMaxAge(0);
                domainCookie.setHttpOnly(true);
                domainCookie.setSecure(false);
                response.addCookie(domainCookie);
            }

            // Clear __reveal_ut cookie
            Cookie revealCookie = new Cookie("__reveal_ut", "");
            revealCookie.setPath("/");
            revealCookie.setDomain("localhost");
            revealCookie.setMaxAge(0);
            revealCookie.setHttpOnly(true);
            revealCookie.setSecure(false); // Set to true if using HTTPS
            response.addCookie(revealCookie);
            
            // 5. Add comprehensive response headers
            response.setHeader("Clear-Site-Data", "\"cache\", \"cookies\", \"storage\", \"executionContexts\"");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");
            
            // 6. Force session timeout with multiple Set-Cookie headers
            response.addHeader("Set-Cookie", "JSESSIONID=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT; HttpOnly");
            response.addHeader("Set-Cookie", "jsessionid=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT; HttpOnly");
            response.addHeader("Set-Cookie", "JSESSIONID=; Path=/; Domain=" + request.getServerName() + "; Expires=Thu, 01 Jan 1970 00:00:00 GMT; HttpOnly");
            
            System.out.println("✓ Enhanced logout completed - All session data cleared");
            System.out.println("=== Logout Process Complete ===");
            
            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Logout failed: " + e.getMessage());
        }
    }

    // Get current user info
    @GetMapping("/me")
    public ResponseEntity<UserData> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            try {
                UserData userData = userDto.getUserByEmail(email);
                return ResponseEntity.ok(userData);
            } catch (Exception e) {
                return ResponseEntity.notFound().build();
            }
        }
        return ResponseEntity.status(401).build();
    }

    // Test endpoint to create a user programmatically
    @PostMapping("/test-create")
    public ResponseEntity<String> testCreateUser() {
        try {
            UserForm form = new UserForm();
            form.setEmail("admin@example.com");
            form.setPassword("admin123");
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
