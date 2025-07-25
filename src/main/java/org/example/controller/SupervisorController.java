package org.example.controller;

import org.example.dto.UserDto;
import org.example.exception.ApiException;
import org.example.model.data.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.example.model.form.PaginationRequest;
import org.example.model.data.PaginationResponse;
import org.springframework.http.ResponseEntity;

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

    @GetMapping("/users/paginated")
    public ResponseEntity<org.example.model.data.PaginationResponse<UserData>> getAllUsersPaginated(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            Authentication authentication) {
        System.out.println("=== SUPERVISOR USERS PAGINATED ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("Page: " + page + ", Size: " + size + ", SortBy: " + sortBy + ", SortDirection: " + sortDirection);
        
        try {
            org.example.model.form.PaginationRequest request = new org.example.model.form.PaginationRequest(page, size, sortBy, sortDirection);
            org.example.model.data.PaginationResponse<UserData> response = userDto.getAllPaginated(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new ApiException("Failed to get all users: " + e.getMessage());
        }
    }

} 