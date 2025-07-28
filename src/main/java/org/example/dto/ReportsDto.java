package org.example.dto;

import org.example.model.data.SalesReportData;
import org.example.model.form.SalesReportForm;
import org.example.model.form.CustomDateRangeSalesForm;
import org.example.model.data.CustomDateRangeSalesData;
import org.example.model.form.DaySalesForm;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.OrderPojo;
import org.example.pojo.DaySalesPojo;
import org.example.dao.DaySalesDao;
import org.example.dao.OrderItemDao;

import org.example.api.ClientApi;
import org.example.api.ProductApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.example.exception.ApiException;

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import org.example.util.TimeUtil;

@Component
public class ReportsDto {
    

    @Autowired
    private org.example.flow.OrderFlow orderFlow;
    
    @Autowired
    private DaySalesDao daySalesRepo;
    
    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private ClientApi clientApi;

    @Autowired
    private ProductApi productApi;

    public List<SalesReportData> getSalesReport(SalesReportForm form) {
        // Validate input - dates are assumed to be in UTC from frontend
        if (Objects.isNull(form.getStartDate()) || Objects.isNull(form.getEndDate())) {
            throw new ApiException("Start date and end date are required");
        }
        if (form.getEndDate().isBefore(form.getStartDate())) {
            throw new ApiException("End date cannot be before start date");
        }
        
        try {
            // Convert LocalDate to IST for filtering
            LocalDate start = form.getStartDate();
            LocalDate end = form.getEndDate();
            
            // Use direct DAO call with proper eager fetching
            List<OrderItemPojo> allItems = orderItemDao.selectAll();
            
            // Filter by date range - need to get orders for each item
            List<OrderItemPojo> filtered = new ArrayList<>();
            for (OrderItemPojo item : allItems) {
                OrderPojo order = orderFlow.get(item.getOrderId());
                if (Objects.nonNull(order) && Objects.nonNull(order.getDate())) {
                    // Convert order date (Instant) to IST LocalDate
                    LocalDate orderDateIST = TimeUtil.toIST(order.getDate()).toLocalDate();
                    if ((orderDateIST.isEqual(start) || orderDateIST.isAfter(start)) && orderDateIST.isBefore(end.plusDays(1))) {
                        filtered.add(item);
                    }
                }
            }
            
            // Filter by brand (clientName) if provided
            if (Objects.nonNull(form.getBrand()) && !form.getBrand().isEmpty()) {
                filtered = filtered.stream().filter(item -> {
                    try {
                        org.example.pojo.ProductPojo product = productApi.get(item.getProductId());
                        if (product != null && product.getClientId() != null && product.getClientId() > 0) {
                            org.example.pojo.ClientPojo client = clientApi.get(product.getClientId());
                            return client != null && form.getBrand().equalsIgnoreCase(client.getClientName());
                        }
                    } catch (Exception e) {
                        // Product or client not found, exclude from results
                    }
                    return false;
                }).collect(Collectors.toList());
            }
            
            // Filter by category (product name) if provided
            if (Objects.nonNull(form.getCategory()) && !form.getCategory().isEmpty()) {
                filtered = filtered.stream().filter(item -> {
                    try {
                        org.example.pojo.ProductPojo product = productApi.get(item.getProductId());
                        return product != null && form.getCategory().equalsIgnoreCase(product.getName());
                    } catch (Exception e) {
                        // Product not found, exclude from results
                        return false;
                    }
                }).collect(Collectors.toList());
            }
            
            // Group by brand and category
            Map<String, SalesReportData> resultMap = new HashMap<>();
            for (OrderItemPojo item : filtered) {
                String brand = "Unknown";
                String productName = "Unknown";
                String sku = "Unknown";
                
                try {
                    org.example.pojo.ProductPojo product = productApi.get(item.getProductId());
                    if (product != null) {
                        productName = product.getName() != null ? product.getName() : "Unknown";
                        sku = product.getBarcode() != null ? product.getBarcode() : "Unknown";
                        
                        // Get client name
                        if (product.getClientId() != null && product.getClientId() > 0) {
                            try {
                                org.example.pojo.ClientPojo client = clientApi.get(product.getClientId());
                                if (client != null) {
                                    brand = client.getClientName() != null ? client.getClientName() : "Unknown";
                                }
                            } catch (Exception e) {
                                // Client not found, use "Unknown"
                            }
                        }
                    }
                } catch (Exception e) {
                    // Product not found, use "Unknown" values
                }
                
                String key = brand + "|" + sku;
                SalesReportData resp = resultMap.getOrDefault(key, new SalesReportData());
                if (resp.getBrand() == null) {
                    resp.setBrand(brand);
                    resp.setCategory(productName); // Set category to product name
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
            LocalDate start = form.getStartDate();
            LocalDate end = form.getEndDate();
            List<OrderItemPojo> allItems = orderItemDao.selectAll();
            Map<String, CustomDateRangeSalesData> resultMap = new HashMap<>();
            Map<String, Map<Integer, Boolean>> processedOrders = new HashMap<>(); // Track processed orders for each group
            for (OrderItemPojo item : allItems) {
                OrderPojo order = orderFlow.get(item.getOrderId());
                if (Objects.isNull(order) || Objects.isNull(order.getDate())) continue;
                // Convert order date (Instant) to IST LocalDate
                LocalDate orderDateIST = TimeUtil.toIST(order.getDate()).toLocalDate();
                // Use inclusive date range filtering (same as getSalesReport)
                if ((orderDateIST.isEqual(start) || orderDateIST.isAfter(start)) && orderDateIST.isBefore(end.plusDays(1))) {
                    // Fetch product and client information using productId
                    String brand = null;
                    String category = null;
                    try {
                        org.example.pojo.ProductPojo product = productApi.get(item.getProductId());
                        if (product != null) {
                            category = product.getName();
                            // Fetch client information
                            if (product.getClientId() != null && product.getClientId() > 0) {
                                try {
                                    org.example.pojo.ClientPojo client = clientApi.get(product.getClientId());
                                    if (client != null) {
                                        brand = client.getClientName();
                                    }
                                } catch (Exception e) {
                                    // Client not found, continue with null
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Product not found, continue with null values
                    }
                    
                    // Filtering logic
                    boolean brandMatch = (form.getBrand() == null || form.getBrand().isEmpty() || (brand != null && form.getBrand().equalsIgnoreCase(brand)));
                    boolean categoryMatch = (form.getCategory() == null || form.getCategory().isEmpty() || (category != null && form.getCategory().equalsIgnoreCase(category)));
                    if (!brandMatch || !categoryMatch) continue;
                    // Grouping key: both present -> brand|category, only brand -> brand|, only category -> |category, neither -> |
                    String key = (brand != null ? brand : "") + "|" + (category != null ? category : "");
                    CustomDateRangeSalesData data = resultMap.getOrDefault(key, new CustomDateRangeSalesData());
                    if (data.getBrand() == null && brand != null) data.setBrand(brand);
                    if (data.getCategory() == null && category != null) data.setCategory(category);
                    if (data.getTotalAmount() == null) data.setTotalAmount(0.0);
                    if (data.getTotalOrders() == null) data.setTotalOrders(0);
                    if (data.getTotalItems() == null) data.setTotalItems(0);
                    data.setTotalAmount(data.getTotalAmount() + (Objects.nonNull(item.getAmount()) ? item.getAmount() : 0.0));
                    data.setTotalItems(data.getTotalItems() + (Objects.nonNull(item.getQuantity()) ? item.getQuantity() : 0));
                    // Count unique orders per group
                    processedOrders.putIfAbsent(key, new HashMap<>());
                    if (!processedOrders.get(key).containsKey(order.getId())) {
                        data.setTotalOrders(data.getTotalOrders() + 1);
                        processedOrders.get(key).put(order.getId(), true);
                    }
                    resultMap.put(key, data);
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
            List<DaySalesPojo> daySalesList = daySalesRepo.selectAll();
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