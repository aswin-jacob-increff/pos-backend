package org.example.controller;

import org.example.dto.ClientDto;
import org.example.exception.ApiException;
import org.example.model.data.ClientData;
import org.example.model.data.TsvUploadResult;
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
    public ClientData add(@RequestBody ClientForm form) {
        return clientDto.add(form);
    }

    @GetMapping("/{id}")
    public ClientData get(@PathVariable Integer id) {
        return clientDto.get(id);
    }

    @GetMapping("/paginated")
    public ResponseEntity<PaginationResponse<ClientData>> getAllClientsPaginated(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
        PaginationResponse<ClientData> response = clientDto.getPaginated(PaginationQuery.all(request));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/search/{name}/paginated")
    public ResponseEntity<PaginationResponse<ClientData>> searchByNamePaginated(
            @PathVariable String name,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
        PaginationResponse<ClientData> response = clientDto.getByNameLikePaginated(name, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ClientData update(@PathVariable Integer id, @RequestBody ClientForm form) {
        return clientDto.update(id, form);
    }

    @PutMapping("/toggle")
    public void toggleStatus(@RequestParam(required = false) Integer id,
                            @RequestParam(required = false) String name) {
        clientDto.toggleStatus(id, name);
    }

    @GetMapping("/search")
    public ClientData getClient(@RequestParam(required = false) Integer id,
                               @RequestParam(required = false) String name) {

        return clientDto.getByNameOrId(id, name);
    }

    @PostMapping(value = "/upload-tsv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TsvUploadResult> uploadClientsFromTsv(@RequestParam("file") MultipartFile file) {

            TsvUploadResult result = clientDto.uploadClientsFromTsv(file);
            if (result.hasErrors()) {
                return ResponseEntity.badRequest().body(result);
            } else if (result.getSuccessfulRows() == 0) {
                return ResponseEntity.badRequest().body(result);
            } else {
                return ResponseEntity.ok(result);
            }
    }
}
