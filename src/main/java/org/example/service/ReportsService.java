package org.example.service;

import org.example.dto.SalesReportRequest;
import org.example.dto.SalesReportResponse;
import org.springframework.stereotype.Service;
import org.example.service.OrderItemService;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.ProductPojo;
import org.example.pojo.OrderPojo;
import org.example.pojo.ClientPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.exception.ApiException;
import java.time.ZoneId;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class ReportsService {
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private ProductService productService;

    public List<SalesReportResponse> getSalesReport(SalesReportRequest request) {
        try {
            // Convert LocalDate to UTC Instants for filtering
            Instant start = request.getStartDate().atStartOfDay(ZoneId.of("Asia/Kolkata")).withZoneSameInstant(ZoneId.of("UTC")).toInstant();
            Instant end = request.getEndDate().plusDays(1).atStartOfDay(ZoneId.of("Asia/Kolkata")).withZoneSameInstant(ZoneId.of("UTC")).toInstant();
            List<OrderItemPojo> allItems = orderItemService.getAll();
            // Filter by date range
            List<OrderItemPojo> filtered = allItems.stream().filter(item -> {
                OrderPojo order = item.getOrder();
                if (order == null || order.getDate() == null) return false;
                Instant orderDate = order.getDate();
                return !orderDate.isBefore(start) && orderDate.isBefore(end);
            }).collect(Collectors.toList());
            // Filter by brand (clientName) if provided
            if (request.getBrand() != null && !request.getBrand().isEmpty()) {
                filtered = filtered.stream().filter(item -> {
                    ProductPojo product = item.getProduct();
                    if (product == null || product.getClient() == null) return false;
                    return request.getBrand().equalsIgnoreCase(product.getClient().getClientName());
                }).collect(Collectors.toList());
            }
            // No category field, so skip category filter for now
            // Aggregate by brand (clientName)
            Map<String, SalesReportResponse> resultMap = new HashMap<>();
            for (OrderItemPojo item : filtered) {
                ProductPojo product = item.getProduct();
                String brand = (product != null && product.getClient() != null) ? product.getClient().getClientName() : "Unknown";
                String category = ""; // No category in model
                String key = brand + "|" + category;
                SalesReportResponse resp = resultMap.getOrDefault(key, new SalesReportResponse(brand, category, 0, 0.0));
                resp.setQuantity(resp.getQuantity() + (item.getQuantity() != null ? item.getQuantity() : 0));
                resp.setRevenue(resp.getRevenue() + (item.getAmount() != null ? item.getAmount() : 0.0));
                resultMap.put(key, resp);
            }
            return resultMap.values().stream().collect(Collectors.toList());
        } catch (Exception e) {
            throw new ApiException("Failed to generate sales report: " + e.getMessage());
        }
    }
} 