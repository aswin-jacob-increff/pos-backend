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
    @PostMapping("/signup")
    public void signup(@RequestBody UserForm form) {
        try {
            userDto.signup(form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Signup failed: " + e.getMessage());
        }
    }
    @PostMapping("/login")
    public ResponseEntity<UserData> login(@RequestBody UserForm form, HttpServletRequest request) {
        try {
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession newSession = request.getSession(true);
            UserData userData = userDto.login(form);
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(form.getEmail(), form.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            newSession.setAttribute("userEmail", form.getEmail());
            String userRole = authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("ROLE_USER");
            newSession.setAttribute("userRole", userRole);
            return ResponseEntity.ok(userData);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Login failed: " + e.getMessage());
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            HttpSession session = request.getSession(false);
            SecurityContextHolder.clearContext();
            if (session != null) {
                session.invalidate();
            }
            return ResponseEntity.ok("Logged out successfully");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Logout failed: " + e.getMessage());
        }
    }
    @GetMapping("/me")
    public ResponseEntity<UserData> getCurrentUser(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            HttpSession session = request.getSession(false);
            String userEmail = null;
            if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getName())) {
                userEmail = authentication.getName();
            }
            if (userEmail == null && session != null) {
                userEmail = (String) session.getAttribute("userEmail");
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
            throw e;
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
        HttpSession session = request.getSession(false);
        StringBuilder status = new StringBuilder();
        status.append("Authentication: ").append(authentication != null ? authentication.getName() : "null");
        status.append(", Authenticated: ").append(authentication != null && authentication.isAuthenticated());
        status.append(", Session: ").append(session != null ? session.getId() : "null");
        status.append(", Session Valid: ").append(session != null && !session.isNew());
        return ResponseEntity.ok(status.toString());
    }
}
