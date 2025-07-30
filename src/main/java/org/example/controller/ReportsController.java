package org.example.controller;

import org.example.dto.ReportsDto;
import org.example.exception.ApiException;
import org.example.model.constants.ApiEndpoints;
import org.example.model.data.SalesReportData;
import org.example.model.form.SalesReportForm;
import org.example.model.data.CustomDateRangeSalesData;
import org.example.model.form.CustomDateRangeSalesForm;
import org.example.model.data.DaySalesData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping(ApiEndpoints.Supervisor.REPORTS)
public class ReportsController {

    @Autowired
    private ReportsDto reportsDto;

    @PostMapping("/sales")
    public List<SalesReportData> getSalesReport(@RequestBody SalesReportForm form) {

        
        try {
            return reportsDto.getSalesReport(form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to generate sales report: " + e.getMessage());
        }
    }
    
    @PostMapping("/sales/custom-date-range")
    public List<CustomDateRangeSalesData> getCustomDateRangeSalesReport(@RequestBody CustomDateRangeSalesForm form) {

        
        try {
            return reportsDto.getCustomDateRangeSalesReport(form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to generate custom date range sales report: " + e.getMessage());
        }
    }
    
    @GetMapping("/day-sales")
    public List<DaySalesData> getAllDaySales() {

        
        try {
            return reportsDto.getAllDaySales();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get all day sales: " + e.getMessage());
        }
    }

    @GetMapping("/day-sales/by-date-range")
    public List<DaySalesData> getDaySalesByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            return reportsDto.getAllDaySales(start, end);
        } catch (DateTimeParseException e) {
            throw new ApiException("Invalid date format. Use YYYY-MM-DD format.");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get day sales by date range: " + e.getMessage());
        }
    }

} 