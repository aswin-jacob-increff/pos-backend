package org.example.api;

import org.example.dao.DaySalesRepository;
import org.example.dao.OrderDao;
import org.example.pojo.DaySales;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.OrderPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class DaySalesScheduler {
    @Autowired
    private DaySalesRepository daySalesRepo;
    @Autowired
    private OrderDao orderDao;

    // Runs every day at 11:59 PM IST
    @Scheduled(cron = "0 59 23 * * *", zone = "Asia/Kolkata")
    public void calculateDaySales() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        List<OrderPojo> orders = orderDao.findOrdersByDate(today);
        int ordersCount = orders.size();
        int itemsCount = orders.stream().mapToInt(o -> o.getOrderItems().size()).sum();
        double totalRevenue = orders.stream()
            .flatMap(o -> o.getOrderItems().stream())
            .mapToDouble(OrderItemPojo::getAmount)
            .sum();
        DaySales daySales = new DaySales();
        daySales.setDate(today);
        daySales.setInvoicedOrdersCount(ordersCount);
        daySales.setInvoicedItemsCount(itemsCount);
        daySales.setTotalRevenue(totalRevenue);
        daySalesRepo.saveOrUpdate(daySales);
    }
} 