package org.example.service;

import jakarta.transaction.Transactional;
import org.example.dao.InvoiceDao;
import org.example.dao.InvoiceItemDao;
import org.example.dao.OrderDao;
import org.example.pojo.InvoiceItemPojo;
import org.example.pojo.InvoicePojo;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.OrderPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private InvoiceDao invoiceDao;

    @Autowired
    private InvoiceItemDao invoiceItemDao;

    public OrderPojo add(OrderPojo orderPojo) {
        orderPojo.setDate(Instant.now());

        // 1. Save order to generate ID
        orderDao.insert(orderPojo);

        // 2. Add each order item
        List<OrderItemPojo> orderItems = orderPojo.getOrderItems();
        double totalAmount = 0.0;

        for (OrderItemPojo item : orderItems) {
            item.setOrder(orderPojo); // link order to item

            // Calculate amount in backend
            double amount = item.getSellingPrice() * item.getQuantity();
            item.setAmount(amount);

            orderItemService.add(item);

            totalAmount += amount;
        }

        // 3. Set total and update order again
        orderPojo.setTotal(totalAmount);
        orderDao.update(orderPojo.getId(), orderPojo);

        return orderPojo;
    }

    public OrderPojo get(Integer id) {
        return orderDao.select(id);
    }

    public List<OrderPojo> getAll() {
        return orderDao.selectAll();
    }

    public OrderPojo update(Integer id, OrderPojo updatedOrder) {
        OrderPojo existingOrder = orderDao.select(id);
        existingOrder.setDate(updatedOrder.getDate());

        // Optional: update other fields like status here
        orderDao.update(id, existingOrder);

        return orderDao.select(id);
    }

    public void delete(Integer id) {
        orderDao.delete(id);
    }
}
