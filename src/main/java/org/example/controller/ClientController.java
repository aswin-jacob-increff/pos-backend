package org.example.controller;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.util.ClientTsvParser;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import org.example.model.ClientData;
import org.example.model.ClientForm;
import org.example.dto.ClientDto;
import org.example.exception.ApiException;

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

    @Operation(summary = "Upload clients via TSV file")
    @PostMapping(value = "/upload-tsv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadClientsFromTsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "uploadedBy", required = false) String uploadedBy) {

        if (file == null || file.isEmpty() || !file.getOriginalFilename().endsWith(".tsv")) {
            throw new ApiException("Please upload a valid non-empty .tsv file.");
        }
        try {
            List<ClientForm> clientForms = ClientTsvParser.parse(file.getInputStream());
            if (clientForms.size() > 5000) {
                throw new ApiException("File upload limit exceeded: Maximum 5000 rows allowed.");
            }
            for (ClientForm form : clientForms) {
                clientDto.add(form);
            }
            return ResponseEntity.ok("Uploaded " + clientForms.size() + " clients" +
                    (uploadedBy != null ? " by " + uploadedBy : "") + ".");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        } catch (ApiException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process file: " + e.getMessage());
        }
    }

}
