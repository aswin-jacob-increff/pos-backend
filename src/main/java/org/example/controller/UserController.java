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
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            HttpSession session = request.getSession(false);
            Cookie[] cookiesBefore = request.getCookies();
            SecurityContextHolder.clearContext();
            if (session != null) {
                session.invalidate();
            }
            if (cookiesBefore != null) {
                for (Cookie cookie : cookiesBefore) {
                    Cookie cleared = new Cookie(cookie.getName(), "");
                    cleared.setPath(cookie.getPath() != null ? cookie.getPath() : "/");
                    cleared.setDomain(cookie.getDomain()); // Keep original domain (can be null)
                    cleared.setMaxAge(0);
                    cleared.setHttpOnly(true);
                    cleared.setSecure(false); // Set to true if using HTTPS
                    response.addCookie(cleared);
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
                Cookie domainCookie = new Cookie(name, "");
                domainCookie.setPath("/");
                domainCookie.setDomain(request.getServerName());
                domainCookie.setMaxAge(0);
                domainCookie.setHttpOnly(true);
                domainCookie.setSecure(false);
                response.addCookie(domainCookie);
            }
            String[] revealPaths = {"/", "/api", ""};
            for (String path : revealPaths) {
                Cookie reveal = new Cookie("__reveal_ut", "");
                reveal.setPath(path);
                reveal.setMaxAge(0);
                reveal.setHttpOnly(true);
                reveal.setSecure(false); // Set to true if using HTTPS
                response.addCookie(reveal);
            }
            Cookie revealDomain = new Cookie("__reveal_ut", "");
            revealDomain.setPath("/");
            revealDomain.setDomain(request.getServerName());
            revealDomain.setMaxAge(0);
            revealDomain.setHttpOnly(true);
            revealDomain.setSecure(false);
            response.addCookie(revealDomain);
            Cookie revealNullDomain = new Cookie("__reveal_ut", "");
            revealNullDomain.setPath("/");
            revealNullDomain.setDomain(null);
            revealNullDomain.setMaxAge(0);
            revealNullDomain.setHttpOnly(true);
            revealNullDomain.setSecure(false);
            response.addCookie(revealNullDomain);
            response.setHeader("Clear-Site-Data", "\"cache\", \"cookies\", \"storage\", \"executionContexts\"");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
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
