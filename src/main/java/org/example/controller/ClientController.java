package org.example.controller;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.example.model.ClientData;
import org.example.model.ClientForm;
import org.example.dto.ClientDto;
import org.example.exception.ApiException;

@RestController
@RequestMapping("/api/clients")
@Tag(name = "Client APIs", description = "Operations related to clients")
public class ClientController {

    @Autowired
    private ClientDto dto;

    @Operation(summary = "Adds a client")
    @PostMapping
    @org.springframework.transaction.annotation.Transactional
    public ClientData add(@RequestBody ClientForm form) {
        try {
            return dto.add(form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to add client: " + e.getMessage());
        }
    }

    @Operation(summary = "Gets all clients")
    @GetMapping
    public List<ClientData> getAll() {
        try {
            return dto.getAll();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get all clients: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ClientData getClient(@RequestParam(required = false) Integer id,
                               @RequestParam(required = false) String name) {
        try {
            return dto.getByNameOrId(id, name);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get client: " + e.getMessage());
        }
    }

    @Operation(summary = "Updates a client by ID")
    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ClientData update(@PathVariable Integer id, @RequestBody ClientForm form) {
        try {
            return dto.update(id, form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to update client: " + e.getMessage());
        }
    }

    @Operation(summary = "Toggles client status by ID or name")
    @PutMapping("/toggle")
    @org.springframework.transaction.annotation.Transactional
    public void toggleStatus(@RequestParam(required = false) Integer id,
                            @RequestParam(required = false) String name) {
        try {
            dto.toggleStatus(id, name);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to toggle client status: " + e.getMessage());
        }
    }

    @Operation(summary = "Upload clients via TSV file")
    @PostMapping(value = "/upload-tsv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<String> uploadClientsFromTsv(@RequestParam("file") MultipartFile file) {
        try {
            String result = dto.uploadClientsFromTsv(file);
            return ResponseEntity.ok(result);
        } catch (ApiException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process file: " + e.getMessage());
        }
    }

}
