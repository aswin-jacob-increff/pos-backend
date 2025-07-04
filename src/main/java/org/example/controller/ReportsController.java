package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.SalesReportRequest;
import org.example.dto.SalesReportResponse;
import org.example.exception.ApiException;
import org.example.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    @Autowired
    private ReportsService reportsService;

    @GetMapping("/sales")
    public List<SalesReportResponse> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category) {
        if (startDate == null || endDate == null) {
            throw new ApiException("Start date and end date are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new ApiException("End date cannot be before start date");
        }
        SalesReportRequest request = new SalesReportRequest(startDate, endDate, brand, category);
        return reportsService.getSalesReport(request);
    }
} 