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
import java.time.ZoneId;
import java.util.List;

@Service
public class DaySalesScheduler {
    @Autowired
    private DaySalesDao daySalesRepo;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private OrderItemApi orderItemApi;

    // Runs every day at 11:59 PM IST (Asia/Kolkata timezone)
    @Scheduled(cron = "59 23 * * * *", zone = "Asia/Kolkata")
    @Transactional
    public void calculateDaySales() {
        // Calculate for the current day in IST
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        calculateDaySalesForDate(today);
    }

    @jakarta.annotation.PostConstruct
    @Transactional
    public void backfillDaySales() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
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
        double totalRevenue = Math.round(orders.stream()
            .mapToDouble(OrderPojo::getTotal)
            .sum() * 100.0) / 100.0;
        
        // Calculate actual items count from order items
        int itemsCount = 0;
        for (OrderPojo order : orders) {
            try {
                List<OrderItemPojo> orderItems = orderItemApi.getByOrderId(order.getId());
                itemsCount += orderItems.stream()
                    .mapToInt(OrderItemPojo::getQuantity)
                    .sum();
            } catch (Exception e) {
                // If order items cannot be fetched, skip this order
                System.err.println("Warning: Could not fetch order items for order " + order.getId() + ": " + e.getMessage());
            }
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
            System.out.println("Day sales upserted for date: " + date + 
                              " (Revenue: " + totalRevenue + 
                              ", Orders: " + ordersCount + 
                              ", Items: " + itemsCount + ")");
        } catch (Exception e) {
            System.err.println("Error upserting day sales for date " + date + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
} 