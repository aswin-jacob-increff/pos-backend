package org.example.controller;

import org.example.model.SalesReportForm;
import org.example.model.SalesReportData;
import org.example.dto.ReportsDto;
import org.example.pojo.DaySales;
import org.example.dao.DaySalesRepository;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    @Autowired
    private ReportsDto reportsDto;

    @Autowired
    private DaySalesRepository daySalesRepo;

    @GetMapping("/day-sales")
    public List<DaySales> getDaySales(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        if (startDate.isAfter(endDate)) {
            throw new ApiException("Start date cannot be after end date.");
        }
        return daySalesRepo.findByDateRange(startDate, endDate);
    }

    @PostMapping("/sales")
    public List<SalesReportData> getSalesReport(@RequestBody SalesReportForm form) {
        if (form.getStartDate().isAfter(form.getEndDate())) {
            throw new ApiException("Start date cannot be after end date.");
        }
        return reportsDto.getSalesReport(form);
    }
} 