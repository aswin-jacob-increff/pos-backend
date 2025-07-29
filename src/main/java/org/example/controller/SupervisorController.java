package org.example.controller;

import org.example.dto.UserDto;
import org.example.exception.ApiException;
import org.example.model.data.UserData;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
import org.example.model.form.PaginationQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supervisor")
public class SupervisorController {

    @Autowired
    private UserDto userDto;

    @GetMapping("/users")
    public List<UserData> getAllUsers() {
        try {
            return userDto.getAll();
        } catch (Exception e) {
            throw new ApiException("Failed to get all users: " + e.getMessage());
        }
    }

    @GetMapping("/users/paginated")
    public ResponseEntity<PaginationResponse<UserData>> getAllUsersPaginated(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        try {
            PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
            PaginationResponse<UserData> response = userDto.getPaginated(PaginationQuery.all(request));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new ApiException("Failed to get all users: " + e.getMessage());
        }
    }

} 