package org.example.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;

import org.example.model.ClientData;
import org.example.model.ClientForm;
import org.example.dto.ClientDto;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/clients")
@Tag(name = "Client APIs", description = "Operations related to clients")
public class ClientController {

    @Autowired
    private ClientDto clientDto;

    @Operation(summary = "Adds a client")
    @PostMapping
    public ClientData add(@RequestBody ClientForm form) {
        return clientDto.add(form);
    }

    @Operation(summary = "Gets all clients")
    @GetMapping
    public List<ClientData> getAll() {
        return clientDto.getAll();
    }

    @GetMapping("/{id}")
    public ClientData getClient(@RequestParam(required = false) Integer id,
                               @RequestParam(required = false) String name) {
        if (id != null) {
            return clientDto.get(id);
        } else if (name != null) {
            return clientDto.getByName(name);
        } else {
            throw new IllegalArgumentException("Either 'id' or 'name' must be provided");
        }
    }

    @Operation(summary = "Updates a client by ID")
    @PutMapping("/{id}")
    public ClientData update(@PathVariable Integer id, @RequestBody ClientForm form) {
        if (id == null) {
            throw new IllegalArgumentException("Client ID cannot be empty");
        }
        return clientDto.update(id, form);
    }

    @Operation(summary = "Deletes a client by ID or name")
    @DeleteMapping
    public void delete(@RequestParam(required = false) Integer id,
                       @RequestParam(required = false) String name) {
        if (id != null) {
            clientDto.delete(id);
        } else if (name != null) {
            clientDto.deleteByName(name);
        } else {
            throw new IllegalArgumentException("Either 'id' or 'name' must be provided for deletion");
        }
    }

    @Operation(summary = "Upload clients TSV file")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadClientTsv(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {
        try {
            String contentType = request.getContentType();
            System.out.println(">>> Received Content-Type: " + contentType);

            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("No file uploaded or file is empty.");
            }

            clientDto.uploadClientTsv(file);
            return ResponseEntity.ok("Client TSV uploaded successfully.");

        } catch (Exception ex) {
            ex.printStackTrace(); // Useful for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading TSV: " + ex.getMessage());
        }
    }



}
