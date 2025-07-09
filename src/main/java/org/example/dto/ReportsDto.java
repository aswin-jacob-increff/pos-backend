package org.example.dto;

import org.example.model.SalesReportData;
import org.example.model.SalesReportForm;
import org.example.model.CustomDateRangeSalesForm;
import org.example.model.CustomDateRangeSalesData;
import org.example.model.DaySalesForm;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.ProductPojo;
import org.example.pojo.OrderPojo;
import org.example.pojo.DaySalesPojo;
import org.example.dao.DaySalesDao;
import org.example.dao.OrderItemDao;
import org.example.flow.OrderItemFlow;
import org.example.flow.ProductFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.example.exception.ApiException;
import java.time.ZoneId;
import java.time.Instant;
import java.time.LocalDate;
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
    
    @Autowired
    private DaySalesDao daySalesRepo;
    
    @Autowired
    private OrderItemDao orderItemDao;

    public List<SalesReportData> getSalesReport(SalesReportForm form) {
        // Validate input - dates are assumed to be in UTC from frontend
        if (Objects.isNull(form.getStartDate()) || Objects.isNull(form.getEndDate())) {
            throw new ApiException("Start date and end date are required");
        }
        if (form.getEndDate().isBefore(form.getStartDate())) {
            throw new ApiException("End date cannot be before start date");
        }
        
        try {
            // Convert LocalDate to UTC Instants for filtering
            Instant start = form.getStartDate().atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
            Instant end = form.getEndDate().plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
            
            // Use direct DAO call with proper eager fetching
            List<OrderItemPojo> allItems = orderItemDao.selectAll();
            
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
            
            // Filter by category (product name) if provided
            if (Objects.nonNull(form.getCategory()) && !form.getCategory().isEmpty()) {
                System.out.println("Filtering by category: " + form.getCategory());
                filtered = filtered.stream().filter(item -> {
                    ProductPojo product = item.getProduct();
                    if (Objects.isNull(product) || Objects.isNull(product.getName())) return false;
                    boolean matches = form.getCategory().equalsIgnoreCase(product.getName());
                    if (matches) {
                        System.out.println("Found matching product: " + product.getName());
                    }
                    return matches;
                }).collect(Collectors.toList());
                System.out.println("After category filtering: " + filtered.size() + " items");
            }
            
            // Aggregate by SKU (barcode) and product name
            Map<String, SalesReportData> resultMap = new HashMap<>();
            for (OrderItemPojo item : filtered) {
                ProductPojo product = item.getProduct();
                String brand = (Objects.nonNull(product) && Objects.nonNull(product.getClient())) ? product.getClient().getClientName() : "Unknown";
                String productName = (product != null) ? product.getName() : "Unknown";
                String sku = (product != null) ? product.getBarcode() : "Unknown";
                String key = brand + "|" + sku;
                SalesReportData resp = resultMap.getOrDefault(key, new SalesReportData());
                if (resp.getBrand() == null) {
                    resp.setBrand(brand);
                    resp.setCategory("");
                    resp.setQuantity(0);
                    resp.setRevenue(0.0);
                    resp.setProductName(productName);
                    resp.setSku(sku);
                }
                resp.setQuantity(resp.getQuantity() + (Objects.nonNull(item.getQuantity()) ? item.getQuantity() : 0));
                resp.setRevenue(resp.getRevenue() + (Objects.nonNull(item.getAmount()) ? item.getAmount() : 0.0));
                resultMap.put(key, resp);
            }
            return resultMap.values().stream().collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Failed to generate sales report: " + e.getMessage());
        }
    }
    
    public List<CustomDateRangeSalesData> getCustomDateRangeSalesReport(CustomDateRangeSalesForm form) {
        // Validate input - dates are assumed to be in IST from frontend
        if (Objects.isNull(form.getStartDate()) || Objects.isNull(form.getEndDate())) {
            throw new ApiException("Start date and end date are required");
        }
        if (form.getEndDate().isBefore(form.getStartDate())) {
            throw new ApiException("End date cannot be before start date");
        }
        
        try {
            // Get all day sales within the date range
            List<DaySalesPojo> daySalesList = daySalesRepo.findByDateRange(form.getStartDate(), form.getEndDate());
            
            // Aggregate data by brand
            Map<String, CustomDateRangeSalesData> resultMap = new HashMap<>();
            
            for (DaySalesPojo daySales : daySalesList) {
                if (daySales.getOrders() != null) {
                    for (OrderPojo order : daySales.getOrders()) {
                        // Filter by brand if provided
                        if (Objects.nonNull(form.getBrand()) && !form.getBrand().isEmpty()) {
                            boolean hasBrand = order.getOrderItems().stream()
                                .anyMatch(item -> Objects.nonNull(item.getProduct()) && 
                                                Objects.nonNull(item.getProduct().getClient()) &&
                                                form.getBrand().equalsIgnoreCase(item.getProduct().getClient().getClientName()));
                            if (!hasBrand) continue;
                        }
                        
                        // Filter by category (product name) if provided
                        if (Objects.nonNull(form.getCategory()) && !form.getCategory().isEmpty()) {
                            System.out.println("CustomDateRange filtering by category: " + form.getCategory());
                            boolean hasCategory = order.getOrderItems().stream()
                                .anyMatch(item -> Objects.nonNull(item.getProduct()) && 
                                                Objects.nonNull(item.getProduct().getName()) &&
                                                form.getCategory().equalsIgnoreCase(item.getProduct().getName()));
                            if (!hasCategory) continue;
                        }
                        
                        // Aggregate by brand
                        for (OrderItemPojo item : order.getOrderItems()) {
                            String brand = (Objects.nonNull(item.getProduct()) && Objects.nonNull(item.getProduct().getClient())) 
                                ? item.getProduct().getClient().getClientName() : "Unknown";
                            
                            CustomDateRangeSalesData data = resultMap.getOrDefault(brand, 
                                new CustomDateRangeSalesData());
                            if (data.getBrand() == null) {
                                data.setBrand(brand);
                                data.setTotalAmount(0.0);
                                data.setTotalOrders(0);
                                data.setTotalItems(0);
                            }
                            
                            data.setTotalAmount(data.getTotalAmount() + (Objects.nonNull(item.getAmount()) ? item.getAmount() : 0.0));
                            data.setTotalItems(data.getTotalItems() + (Objects.nonNull(item.getQuantity()) ? item.getQuantity() : 0));
                            resultMap.put(brand, data);
                        }
                        
                        // Count unique orders per brand
                        String brand = order.getOrderItems().stream()
                            .filter(item -> Objects.nonNull(item.getProduct()) && Objects.nonNull(item.getProduct().getClient()))
                            .map(item -> item.getProduct().getClient().getClientName())
                            .findFirst()
                            .orElse("Unknown");
                        
                        CustomDateRangeSalesData data = resultMap.getOrDefault(brand, 
                            new CustomDateRangeSalesData());
                        if (data.getBrand() == null) {
                            data.setBrand(brand);
                            data.setTotalAmount(0.0);
                            data.setTotalOrders(0);
                            data.setTotalItems(0);
                        }
                        data.setTotalOrders(data.getTotalOrders() + 1);
                        resultMap.put(brand, data);
                    }
                }
            }
            
            return resultMap.values().stream().collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Failed to generate custom date range sales report: " + e.getMessage());
        }
    }
    
    public List<DaySalesForm> getDayOnDaySalesReport(LocalDate startDate, LocalDate endDate) {
        if (Objects.isNull(startDate) || Objects.isNull(endDate)) {
            throw new ApiException("Start date and end date are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new ApiException("End date cannot be before start date");
        }
        
        try {
            List<DaySalesPojo> daySalesList = daySalesRepo.findByDateRange(startDate, endDate);
            return daySalesList.stream()
                .map(daySales -> {
                    DaySalesForm form = new DaySalesForm();
                    form.setDate(daySales.getDate());
                    form.setInvoicedOrdersCount(daySales.getInvoicedOrdersCount());
                    form.setInvoicedItemsCount(daySales.getInvoicedItemsCount());
                    form.setTotalRevenue(daySales.getTotalRevenue());
                    return form;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Failed to generate day-on-day sales report: " + e.getMessage());
        }
    }
    
    public List<DaySalesForm> getAllDaySales() {
        try {
            List<DaySalesPojo> daySalesList = daySalesRepo.findAll();
            return daySalesList.stream()
                .map(daySales -> {
                    DaySalesForm form = new DaySalesForm();
                    form.setDate(daySales.getDate());
                    form.setInvoicedOrdersCount(daySales.getInvoicedOrdersCount());
                    form.setInvoicedItemsCount(daySales.getInvoicedItemsCount());
                    form.setTotalRevenue(daySales.getTotalRevenue());
                    return form;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Failed to fetch all day sales: " + e.getMessage());
        }
    }
    
    public List<DaySalesForm> getAllDaySales(LocalDate startDate, LocalDate endDate) {
        if (Objects.isNull(startDate) || Objects.isNull(endDate)) {
            throw new ApiException("Start date and end date are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new ApiException("End date cannot be before start date");
        }
        
        try {
            // Note: findByDateRange uses cb.between() which is inclusive of both start and end dates
            List<DaySalesPojo> daySalesList = daySalesRepo.findByDateRange(startDate, endDate);
            return daySalesList.stream()
                .map(daySales -> {
                    DaySalesForm form = new DaySalesForm();
                    form.setDate(daySales.getDate());
                    form.setInvoicedOrdersCount(daySales.getInvoicedOrdersCount());
                    form.setInvoicedItemsCount(daySales.getInvoicedItemsCount());
                    form.setTotalRevenue(daySales.getTotalRevenue());
                    return form;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Failed to fetch day sales for date range: " + e.getMessage());
        }
    }
} 