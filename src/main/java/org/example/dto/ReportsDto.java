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
import java.util.ArrayList;
import org.example.util.TimeUtil;

@Component
public class ReportsDto {
    
    @Autowired
    private OrderItemFlow orderItemFlow;
    @Autowired
    private ProductFlow productFlow;
    @Autowired
    private org.example.flow.OrderFlow orderFlow;
    
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
                    return form.getBrand().equalsIgnoreCase(item.getClientName());
                }).collect(Collectors.toList());
            }
            
            // Filter by category (product name) if provided
            if (Objects.nonNull(form.getCategory()) && !form.getCategory().isEmpty()) {
                System.out.println("Filtering by category: " + form.getCategory());
                filtered = filtered.stream().filter(item -> {
                    if (Objects.isNull(item.getProductName())) return false;
                    boolean matches = form.getCategory().equalsIgnoreCase(item.getProductName());
                    if (matches) {
                        System.out.println("Found matching product: " + item.getProductName());
                    }
                    return matches;
                }).collect(Collectors.toList());
                System.out.println("After category filtering: " + filtered.size() + " items");
            }
            
            // Aggregate by SKU (barcode) and product name
            Map<String, SalesReportData> resultMap = new HashMap<>();
            for (OrderItemPojo item : filtered) {
                String brand = item.getClientName() != null ? item.getClientName() : "Unknown";
                String productName = item.getProductName() != null ? item.getProductName() : "Unknown";
                String sku = item.getProductBarcode() != null ? item.getProductBarcode() : "Unknown";
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
                if (orderDateIST.isBefore(start) || orderDateIST.isAfter(end)) continue;
                String brand = item.getClientName() != null ? item.getClientName() : null;
                String category = item.getProductName() != null ? item.getProductName() : null;
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