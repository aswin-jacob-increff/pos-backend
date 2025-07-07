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

    @GetMapping("/search")
    public ClientData getClient(@RequestParam(required = false) Integer id,
                               @RequestParam(required = false) String name) {
        return clientDto.getByNameOrId(id, name);
    }

    @Operation(summary = "Updates a client by ID")
    @PutMapping("/{id}")
    public ClientData update(@PathVariable Integer id, @RequestBody ClientForm form) {
        return clientDto.update(id, form);
    }

    @Operation(summary = "Toggles client status by ID or name")
    @PutMapping("/toggle")
    public void toggleStatus(@RequestParam(required = false) Integer id,
                            @RequestParam(required = false) String name) {
        clientDto.toggleStatus(id, name);
    }

    @Operation(summary = "Upload clients via TSV file")
    @PostMapping(value = "/upload-tsv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadClientsFromTsv(@RequestParam("file") MultipartFile file) {
        try {
            String result = clientDto.uploadClientsFromTsv(file);
            return ResponseEntity.ok(result);
        } catch (ApiException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process file: " + e.getMessage());
        }
    }

}
