package org.example.controller;

import org.example.model.SalesReportForm;
import org.example.model.SalesReportData;
import org.example.model.CustomDateRangeSalesForm;
import org.example.model.CustomDateRangeSalesData;
import org.example.model.DaySalesForm;
import org.example.dto.ReportsDto;
import org.example.pojo.DaySalesPojo;
import org.example.dao.DaySalesDao;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    @Autowired
    private ReportsDto reportsDto;

    @Autowired
    private DaySalesDao daySalesRepo;

    @GetMapping("/day-sales")
    public List<DaySalesPojo> getDaySales(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        try {
            if (startDate.isAfter(endDate)) {
                throw new ApiException("Start date cannot be after end date.");
            }
            return daySalesRepo.findByDateRange(startDate, endDate);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get day sales: " + e.getMessage());
        }
    }

    @PostMapping("/sales")
    public List<SalesReportData> getSalesReport(@RequestBody SalesReportForm form) {
        try {
            System.out.println("Received sales report request - StartDate: " + form.getStartDate() + 
                              ", EndDate: " + form.getEndDate() + 
                              ", Brand: '" + form.getBrand() + "'" + 
                              ", Category: '" + form.getCategory() + "'");
            if (form.getStartDate().isAfter(form.getEndDate())) {
                throw new ApiException("Start date cannot be after end date.");
            }
            return reportsDto.getSalesReport(form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get sales report: " + e.getMessage());
        }
    }
    
    @PostMapping("/custom-date-range-sales")
    public List<CustomDateRangeSalesData> getCustomDateRangeSalesReport(@RequestBody CustomDateRangeSalesForm form) {
        try {
            System.out.println("Received custom date range sales report request - StartDate: " + form.getStartDate() + 
                              ", EndDate: " + form.getEndDate() + 
                              ", Brand: '" + form.getBrand() + "'" + 
                              ", Category: '" + form.getCategory() + "'");
            if (form.getStartDate().isAfter(form.getEndDate())) {
                throw new ApiException("Start date cannot be after end date.");
            }
            return reportsDto.getCustomDateRangeSalesReport(form);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get custom date range sales report: " + e.getMessage());
        }
    }
    
    @GetMapping("/day-on-day-sales")
    public List<DaySalesForm> getDayOnDaySalesReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        try {
            if (startDate.isAfter(endDate)) {
                throw new ApiException("Start date cannot be after end date.");
            }
            return reportsDto.getDayOnDaySalesReport(startDate, endDate);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get day-on-day sales report: " + e.getMessage());
        }
    }

    @GetMapping("/day-sales/{date}")
    public DaySalesPojo getDaySalesByDate(@PathVariable @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        try {
            DaySalesPojo daySales = daySalesRepo.findByDate(date);
            if (daySales == null) throw new ApiException("No sales data for this date");
            return daySales;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to get day sales by date: " + e.getMessage());
        }
    }
    
    @GetMapping("/all-day-sales")
    public List<DaySalesForm> getAllDaySales(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new ApiException("Start date cannot be after end date.");
            }
            return reportsDto.getAllDaySales(startDate, endDate);
        } else {
            return reportsDto.getAllDaySales();
        }
    }
} 