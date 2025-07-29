package org.example.controller;

import org.example.dto.ClientDto;
import org.example.exception.ApiException;
import org.example.model.data.ClientData;
import org.example.model.form.ClientForm;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
import org.example.model.form.PaginationQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.example.model.constants.ApiEndpoints;

import java.util.List;

@RestController
@RequestMapping(ApiEndpoints.Supervisor.CLIENTS)
public class ClientController {

    @Autowired
    private ClientDto clientDto;


    @PostMapping("/add")
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

    @GetMapping("/paginated")
    public ResponseEntity<PaginationResponse<ClientData>> getAllClientsPaginated(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            Authentication authentication) {
        System.out.println("=== SUPERVISOR CLIENT GET ALL PAGINATED ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("Page: " + page + ", Size: " + size + ", SortBy: " + sortBy + ", SortDirection: " + sortDirection);
        
        try {
            PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
            PaginationResponse<ClientData> response = clientDto.getPaginated(PaginationQuery.all(request));
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get all clients: " + e.getMessage());
        }
    }

    @GetMapping("/name/search/{name}/paginated")
    public ResponseEntity<PaginationResponse<ClientData>> searchByNamePaginated(
            @PathVariable String name,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            Authentication authentication) {
        System.out.println("=== SUPERVISOR CLIENT SEARCH BY NAME PAGINATED ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("Name: " + name + ", Page: " + page + ", Size: " + size);
        
        try {
            PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
            PaginationResponse<ClientData> response = clientDto.getByNameLikePaginated(name, request);
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to search clients by name: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
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

    @GetMapping("/name/search/{name}")
    public List<ClientData> searchByName(@PathVariable String name, Authentication authentication) {
        System.out.println("=== SUPERVISOR CLIENT SEARCH BY NAME ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return clientDto.getByNameLike(name);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to search clients by name: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health(Authentication authentication) {
        System.out.println("=== HEALTH CHECK ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        return ResponseEntity.ok("Client controller is working - Authentication: " + (authentication != null ? "YES" : "NO"));
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(Authentication authentication) {
        System.out.println("=== TEST ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        return ResponseEntity.ok("Test endpoint working - Authentication: " + (authentication != null ? "YES" : "NO"));
    }

    @PostMapping(value = "/test-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> testUpload(@RequestParam("file") MultipartFile file, Authentication authentication) {
        System.out.println("=== TEST UPLOAD ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("File received: " + (file != null ? "YES" : "NO"));
        if (file != null) {
            System.out.println("File name: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize());
            System.out.println("File content type: " + file.getContentType());
            System.out.println("File is empty: " + file.isEmpty());
        }
        
        return ResponseEntity.ok("Test upload successful - File received: " + (file != null ? "YES" : "NO"));
    }

    @PostMapping(value = "/upload-tsv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<org.example.model.data.TsvUploadResult> uploadClientsFromTsv(@RequestParam("file") MultipartFile file, Authentication authentication) {
        System.out.println("=== SUPERVISOR CLIENT UPLOAD TSV ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        System.out.println("File received: " + (file != null ? "YES" : "NO"));
        if (file != null) {
            System.out.println("File name: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize());
            System.out.println("File content type: " + file.getContentType());
            System.out.println("File is empty: " + file.isEmpty());
        }
        
        try {
            org.example.model.data.TsvUploadResult result = clientDto.uploadClientsFromTsv(file);
            System.out.println("Upload result summary: " + result.getSummary());
            System.out.println("Successful rows: " + result.getSuccessfulRows());
            System.out.println("Failed rows: " + result.getFailedRows());
            System.out.println("Errors: " + result.getErrors());
            System.out.println("Warnings: " + result.getWarnings());
            
            // Return appropriate status based on the result
            if (result.hasErrors()) {
                // If there are validation errors, return 400 Bad Request
                return ResponseEntity.badRequest().body(result);
            } else if (result.getSuccessfulRows() == 0) {
                // If no successful rows, return 400 Bad Request
                return ResponseEntity.badRequest().body(result);
            } else {
                // If there are successful rows, return 200 OK
                return ResponseEntity.ok(result);
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            org.example.model.data.TsvUploadResult errorResult = new org.example.model.data.TsvUploadResult();
            errorResult.addError("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }


}
