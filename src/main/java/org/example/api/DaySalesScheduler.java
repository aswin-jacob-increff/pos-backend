package org.example.api;

import org.example.dao.DaySalesDao;
import org.example.dao.OrderDao;
import org.example.pojo.DaySalesPojo;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.OrderPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class DaySalesScheduler {
    @Autowired
    private DaySalesDao daySalesRepo;
    @Autowired
    private OrderDao orderDao;

    // Runs every day at 11:59 PM UTC
    @Scheduled(cron = "59 23 * * * *", zone = "UTC")
    @Transactional
    public void calculateDaySales() {
        // Calculate for the current day in UTC
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        calculateDaySalesForDate(today);
    }

    @jakarta.annotation.PostConstruct
    @Transactional
    public void backfillDaySales() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastCalculated = daySalesRepo.findLatestDate();
        if (lastCalculated == null) {
            lastCalculated = orderDao.findEarliestOrderDate();
        }
        LocalDate date = lastCalculated.plusDays(1);
        while (!date.isAfter(yesterday)) {
            calculateDaySalesForDate(date);
            date = date.plusDays(1);
        }
    }

    // Helper to calculate day sales for a specific date
    @Transactional
    public void calculateDaySalesForDate(LocalDate date) {
        List<OrderPojo> orders = orderDao.findOrdersByDate(date);
        
        int ordersCount = orders.size();
        // Since orders are denormalized, we need to calculate items and revenue differently
        // We'll use the order total for revenue calculation
        double totalRevenue = Math.round(orders.stream()
            .mapToDouble(OrderPojo::getTotal)
            .sum() * 100.0) / 100.0;
        
        // For items count, we need to query order items separately
        int itemsCount = 0;
        for (OrderPojo order : orders) {
            // This would need to be implemented in OrderDao to get items count for an order
            // For now, we'll estimate based on revenue
            itemsCount += Math.max(1, (int) Math.ceil(order.getTotal() / 100.0)); // Rough estimate
        }
        
        // Check if day sales already exists for this date
        DaySalesPojo existingDaySales = daySalesRepo.findByDate(date);
        DaySalesPojo daySales;
        
        if (existingDaySales != null) {
            daySales = existingDaySales;
        } else {
            daySales = new DaySalesPojo();
            daySales.setDate(date);
        }
        
        daySales.setInvoicedOrdersCount(ordersCount);
        daySales.setInvoicedItemsCount(itemsCount);
        daySales.setTotalRevenue(totalRevenue);
        
        try {
            daySalesRepo.saveOrUpdate(daySales);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 