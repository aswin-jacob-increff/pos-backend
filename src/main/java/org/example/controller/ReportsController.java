package org.example.controller;

import org.example.dto.ReportsDto;
import org.example.exception.ApiException;
import org.example.model.SalesReportData;
import org.example.model.SalesReportForm;
import org.example.model.CustomDateRangeSalesData;
import org.example.model.CustomDateRangeSalesForm;
import org.example.model.DaySalesForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supervisor/reports")
public class ReportsController {

    @Autowired
    private ReportsDto reportsDto;

    @PostMapping("/sales")
    public List<SalesReportData> getSalesReport(@RequestBody SalesReportForm form, Authentication authentication) {
        System.out.println("=== SUPERVISOR REPORTS SALES ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return reportsDto.getSalesReport(form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to generate sales report: " + e.getMessage());
        }
    }
    
    @PostMapping("/sales/custom-date-range")
    public List<CustomDateRangeSalesData> getCustomDateRangeSalesReport(@RequestBody CustomDateRangeSalesForm form, Authentication authentication) {
        System.out.println("=== SUPERVISOR REPORTS CUSTOM DATE RANGE SALES ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return reportsDto.getCustomDateRangeSalesReport(form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to generate custom date range sales report: " + e.getMessage());
        }
    }
    
    @GetMapping("/day-sales")
    public List<DaySalesForm> getAllDaySales(Authentication authentication) {
        System.out.println("=== SUPERVISOR REPORTS GET ALL DAY SALES ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            return reportsDto.getAllDaySales();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get all day sales: " + e.getMessage());
        }
    }

    @GetMapping("/day-sales/by-date-range")
    public List<DaySalesForm> getDaySalesByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            Authentication authentication) {
        System.out.println("=== SUPERVISOR REPORTS GET DAY SALES BY DATE RANGE ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
        
        try {
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
            return reportsDto.getAllDaySales(start, end);
        } catch (java.time.format.DateTimeParseException e) {
            throw new ApiException("Invalid date format. Please use yyyy-MM-dd format (e.g., 2024-01-15)");
        } catch (Exception e) {
            throw new ApiException("Failed to get day sales by date range: " + e.getMessage());
        }
    }

} 