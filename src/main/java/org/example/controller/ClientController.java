package org.example.controller;

import java.util.List;
import java.util.Objects;
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
        if (id != null) {
            clientDto.toggleStatus(id);
        } else if (name != null) {
            clientDto.toggleStatusByName(name);
        } else {
            throw new IllegalArgumentException("Either 'id' or 'name' must be provided for status toggle");
        }
    }

    //TODO refactor this
    @Operation(summary = "Upload clients via TSV file")
    @PostMapping(value = "/upload-tsv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadClientsFromTsv(
            @RequestParam("file") MultipartFile file) {

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
            return ResponseEntity.ok("Uploaded " + clientForms.size() + " clients");
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
