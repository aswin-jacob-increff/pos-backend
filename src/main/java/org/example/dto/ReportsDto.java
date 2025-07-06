package org.example.dto;

import org.example.model.SalesReportData;
import org.example.model.SalesReportForm;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.ProductPojo;
import org.example.pojo.OrderPojo;
import org.example.flow.OrderItemFlow;
import org.example.flow.ProductFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.example.exception.ApiException;
import java.time.ZoneId;
import java.time.Instant;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Objects;

@Component
public class ReportsDto {
    
    @Autowired
    private OrderItemFlow orderItemFlow;
    @Autowired
    private ProductFlow productFlow;

    public List<SalesReportData> getSalesReport(SalesReportForm form) {
        // Validate input
        if (Objects.isNull(form.getStartDate()) || Objects.isNull(form.getEndDate())) {
            throw new ApiException("Start date and end date are required");
        }
        if (form.getEndDate().isBefore(form.getStartDate())) {
            throw new ApiException("End date cannot be before start date");
        }
        
        try {
            // Convert LocalDate to UTC Instants for filtering
            Instant start = form.getStartDate().atStartOfDay(ZoneId.of("Asia/Kolkata")).withZoneSameInstant(ZoneId.of("UTC")).toInstant();
            Instant end = form.getEndDate().plusDays(1).atStartOfDay(ZoneId.of("Asia/Kolkata")).withZoneSameInstant(ZoneId.of("UTC")).toInstant();
            List<OrderItemPojo> allItems = orderItemFlow.getAll();
            // Filter by date range
            List<OrderItemPojo> filtered = allItems.stream().filter(item -> {
                OrderPojo order = item.getOrder();
                if (Objects.isNull(order) || Objects.isNull(order.getDate())) return false;
                Instant orderDate = order.getDate();
                return !orderDate.isBefore(start) && orderDate.isBefore(end);
            }).collect(Collectors.toList());
            // Filter by brand (clientName) if provided
            if (Objects.nonNull(form.getBrand()) && !form.getBrand().isEmpty()) {
                filtered = filtered.stream().filter(item -> {
                    ProductPojo product = item.getProduct();
                    if (Objects.isNull(product) || Objects.isNull(product.getClient())) return false;
                    return form.getBrand().equalsIgnoreCase(product.getClient().getClientName());
                }).collect(Collectors.toList());
            }
            // No category field, so skip category filter for now
            // Aggregate by brand (clientName)
            Map<String, SalesReportData> resultMap = new HashMap<>();
            for (OrderItemPojo item : filtered) {
                ProductPojo product = item.getProduct();
                String brand = (Objects.nonNull(product) && Objects.nonNull(product.getClient())) ? product.getClient().getClientName() : "Unknown";
                String category = ""; // No category in model
                String key = brand + "|" + category;
                SalesReportData resp = resultMap.getOrDefault(key, new SalesReportData(brand, category, 0, 0.0));
                resp.setQuantity(resp.getQuantity() + (Objects.nonNull(item.getQuantity()) ? item.getQuantity() : 0));
                resp.setRevenue(resp.getRevenue() + (Objects.nonNull(item.getAmount()) ? item.getAmount() : 0.0));
                resultMap.put(key, resp);
            }
            return resultMap.values().stream().collect(Collectors.toList());
        } catch (Exception e) {
            throw new ApiException("Failed to generate sales report: " + e.getMessage());
        }
    }
} 