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
import org.example.exception.ApiException;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserDto userDto;
    @Autowired
    private AuthenticationManager authenticationManager;
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserForm form) {
        try {
            // Validate input
            if (form.getEmail() == null || form.getEmail().trim().isEmpty()) {
                throw new ApiException("Email is required");
            }
            if (form.getPassword() == null || form.getPassword().trim().isEmpty()) {
                throw new ApiException("Password is required");
            }
            if (form.getPassword().length() < 6) {
                throw new ApiException("Password must be at least 6 characters long");
            }
            if (form.getRole() == null || form.getRole().trim().isEmpty()) {
                throw new ApiException("Role is required");
            }
            
            userDto.signup(form);
            return ResponseEntity.ok("User registered successfully");
        } catch (ApiException e) {
            throw e;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new ApiException("User with this email already exists");
        } catch (Exception e) {
            throw new ApiException("Signup failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserData> login(@RequestBody UserForm form, HttpServletRequest request) {
        try {
            // Validate input
            if (form.getEmail() == null || form.getEmail().trim().isEmpty()) {
                throw new ApiException("Email is required");
            }
            if (form.getPassword() == null || form.getPassword().trim().isEmpty()) {
                throw new ApiException("Password is required");
            }
            
            // Authenticate using Spring Security's AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(form.getEmail().trim().toLowerCase(), form.getPassword())
            );
            
            // Create a new session
            HttpSession session = request.getSession(true);
            
            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Store the SecurityContext in the session using the correct attribute name
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            
            // Also store the authentication directly in the session as a backup
            session.setAttribute("AUTHENTICATION", authentication);
            
            // Get user data after successful authentication
            UserData userData = userDto.getUserByEmail(form.getEmail().trim().toLowerCase());
            
            return ResponseEntity.ok(userData);
        } catch (ApiException e) {
            throw e;
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new ApiException("Invalid email or password. Please check your credentials and try again.");
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            throw new ApiException("User not found. Please check your email address.");
        } catch (org.springframework.security.authentication.DisabledException e) {
            throw new ApiException("Your account has been disabled. Please contact administrator.");
        } catch (org.springframework.security.authentication.LockedException e) {
            throw new ApiException("Your account has been locked. Please contact administrator.");
        } catch (Exception e) {
            throw new ApiException("Login failed: " + e.getMessage());
        }
    }


    @GetMapping("/me")
    public ResponseEntity<UserData> getCurrentUser(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // If not authenticated in current context, try to restore from session
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getName())) {
                
                HttpSession session = request.getSession(false);
                if (session != null) {
                    // Try to get authentication from session
                    Authentication sessionAuth = (Authentication) session.getAttribute("AUTHENTICATION");
                    if (sessionAuth != null && sessionAuth.isAuthenticated()) {
                        // Restore the authentication in the security context
                        SecurityContextHolder.getContext().setAuthentication(sessionAuth);
                        authentication = sessionAuth;
                    }
                }
            }
            
            if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getName())) {
                
                String userEmail = authentication.getName();
                UserData userData = userDto.getUserByEmail(userEmail);
                return ResponseEntity.ok(userData);
            } else {
                throw new ApiException("Session expired. Please log in again.");
            }
        } catch (ApiException e) {
            throw e;
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            throw new ApiException("User account not found. Please contact administrator.");
        } catch (Exception e) {
            throw new ApiException("Failed to get current user: " + e.getMessage());
        }
    }
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
            throw e;
        } catch (Exception e) {
            throw new ApiException("Error creating user: " + e.getMessage());
        }
    }
    @GetMapping("/auth-status")
    public ResponseEntity<String> checkAuthStatus(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        StringBuilder status = new StringBuilder();
        status.append("Authentication: ").append(authentication != null ? authentication.getName() : "null");
        status.append(", Authenticated: ").append(authentication != null && authentication.isAuthenticated());
        status.append(", Session: ").append(request.getSession(false) != null ? request.getSession().getId() : "null");
        status.append(", Session Valid: ").append(request.getSession(false) != null && !request.getSession().isNew());
        return ResponseEntity.ok(status.toString());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        try {
            // Clear the security context
            SecurityContextHolder.clearContext();
            
            // Invalidate the session
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            return ResponseEntity.ok("{\"message\":\"Logout successful\"}");
        } catch (Exception e) {
            throw new ApiException("Logout failed: " + e.getMessage());
        }
    }
}
