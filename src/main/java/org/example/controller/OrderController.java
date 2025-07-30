package org.example.controller;

import org.example.dto.OrderDto;
import org.example.exception.ApiException;
import org.example.model.constants.ApiEndpoints;
import org.example.util.AuthHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.example.model.data.OrderData;
import org.example.model.data.OrderItemData;
import org.example.model.form.OrderForm;
import org.example.model.form.OrderItemForm;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
import org.example.model.form.PaginationQuery;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping(ApiEndpoints.Supervisor.ORDERS)
public class OrderController {

    @Autowired
    private OrderDto orderDto;

    @PostMapping("/add")
    public OrderData add(@RequestBody OrderForm form) {

        String userEmail = AuthHelper.getUserId();
        form.setUserId(userEmail);
        return orderDto.add(form);
    }

    @GetMapping("/{id}")
    public OrderData get(@PathVariable Integer id) {
        return orderDto.get(id);
    }

    @GetMapping
    public List<OrderData> getAll() {
        return orderDto.getAll();
    }

    @GetMapping("/paginated")
    public ResponseEntity<PaginationResponse<OrderData>> getAllOrdersPaginated(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
        PaginationResponse<OrderData> response = orderDto.getPaginated(PaginationQuery.all(request));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<PaginationResponse<OrderData>> getOrdersByUserIdPaginated(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
        PaginationResponse<OrderData> response = orderDto.getOrdersByUserIdPaginated(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range/paginated")
    public ResponseEntity<PaginationResponse<OrderData>> getOrdersByDateRangePaginated(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
        PaginationResponse<OrderData> response = orderDto.getOrdersByDateRangePaginated(start, end, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download-invoice")
    public ResponseEntity<org.springframework.core.io.Resource> downloadInvoice(@PathVariable Integer id) {

        

        org.springframework.core.io.Resource pdfResource = orderDto.downloadInvoice(id);
        String fileName = "order-" + id + ".pdf";
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfResource);
    }

    @GetMapping("/by-date-range")
    public List<OrderData> getOrdersByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return orderDto.getOrdersByDateRange(start, end);
    }

    @GetMapping("/by-user")
    public List<OrderData> getOrdersByUserId(@RequestParam String userId) {
        return orderDto.getOrdersByUserId(userId);
    }

    // ========== SUBSTRING SEARCH ENDPOINTS ==========

    @GetMapping("/substring-id/{searchId}")
    public List<OrderData> findOrdersBySubstringId(
            @PathVariable String searchId,
            @RequestParam(defaultValue = "10") Integer maxResults) {

        return orderDto.findOrdersBySubstringId(searchId, maxResults);
    }

    @GetMapping("/substring-id/{searchId}/paginated")
    public ResponseEntity<PaginationResponse<OrderData>> findOrdersBySubstringIdPaginated(
            @PathVariable String searchId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        PaginationRequest request = new PaginationRequest(page, size, sortBy, sortDirection);
        PaginationResponse<OrderData> response = orderDto.findOrdersBySubstringIdPaginated(searchId, request);
        return ResponseEntity.ok(response);
    }
}
