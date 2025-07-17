package org.example.controller;

import org.example.dto.UserDto;
import org.example.exception.ApiException;
import org.example.model.data.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supervisor")
public class SupervisorController {

    @Autowired
    private UserDto userDto;

    @GetMapping("/users")
    public List<UserData> getAllUsers(Authentication authentication) {
        System.out.println("=== SUPERVISOR USERS ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return userDto.getAll();
        } catch (Exception e) {
            throw new ApiException("Failed to get all users: " + e.getMessage());
        }
    }

} 