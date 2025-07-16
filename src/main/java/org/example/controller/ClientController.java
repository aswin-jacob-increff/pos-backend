package org.example.controller;

import org.example.dto.ClientDto;
import org.example.exception.ApiException;
import org.example.model.ClientData;
import org.example.model.ClientForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supervisor/clients")
public class ClientController {

    @Autowired
    private ClientDto clientDto;

    @PostMapping("/add")
    @org.springframework.transaction.annotation.Transactional
    public ClientData add(@RequestBody ClientForm form, Authentication authentication) {
        System.out.println("=== SUPERVISOR CLIENT ADD ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return clientDto.add(form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to add client: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ClientData get(@PathVariable Integer id, Authentication authentication) {
        System.out.println("=== SUPERVISOR CLIENT GET ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return clientDto.get(id);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get client: " + e.getMessage());
        }
    }

    @GetMapping
    public List<ClientData> getAll(Authentication authentication) {
        System.out.println("=== SUPERVISOR CLIENT GET ALL ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return clientDto.getAll();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get all clients: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ClientData update(@PathVariable Integer id, @RequestBody ClientForm form, Authentication authentication) {
        System.out.println("=== SUPERVISOR CLIENT UPDATE ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return clientDto.update(id, form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to update client: " + e.getMessage());
        }
    }

    @PutMapping("/toggle")
    @org.springframework.transaction.annotation.Transactional
    public void toggleStatus(@RequestParam(required = false) Integer id,
                            @RequestParam(required = false) String name,
                            Authentication authentication) {
        System.out.println("=== SUPERVISOR CLIENT TOGGLE STATUS ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            clientDto.toggleStatus(id, name);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to toggle client status: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ClientData getClient(@RequestParam(required = false) Integer id,
                               @RequestParam(required = false) String name,
                               Authentication authentication) {
        System.out.println("=== SUPERVISOR CLIENT SEARCH ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return clientDto.getByNameOrId(id, name);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to search client: " + e.getMessage());
        }
    }

}
