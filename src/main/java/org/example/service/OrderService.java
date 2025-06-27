package org.example.service;

import jakarta.transaction.Transactional;
import org.example.dao.InvoiceDao;
import org.example.dao.InvoiceItemDao;
import org.example.pojo.InvoiceItemPojo;
import org.example.pojo.InvoicePojo;
import org.example.pojo.OrderItemPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.example.dao.OrderDao;
import org.example.pojo.OrderPojo;

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
        orderPojo.setDateTime(Instant.now());
        orderDao.insert(orderPojo);
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
        existingOrder.setDateTime(updatedOrder.getDate());
        orderDao.update(id, existingOrder);
        return orderDao.select(id);
    }

    public InvoicePojo generateInvoice(Integer orderId) {
        OrderPojo order = orderDao.select(orderId);
        InvoicePojo invoice = new InvoicePojo();
        invoice.setOrder(order);
        invoice.setTotalQuantity(0);
        invoice.setTotal(0.0);
        List<OrderItemPojo> orderItemPojoList = orderItemService.getByOrderId(orderId);
        List<InvoiceItemPojo> invoiceItemPojoList = new ArrayList<>();
        for (OrderItemPojo orderItemPojo : orderItemPojoList) {
            InvoiceItemPojo invoiceItemPojo = new InvoiceItemPojo();
            invoiceItemPojo.setOrderItem(orderItemPojo);
            invoiceItemPojo.setName(orderItemPojo.getProduct().getName());
            invoiceItemPojo.setPrice(orderItemPojo.getSellingPrice());
            invoiceItemPojo.setQuantity(orderItemPojo.getQuantity());
            invoiceItemPojo.setAmount(invoiceItemPojo.getPrice() * invoice.getTotalQuantity());
            invoiceItemDao.insert(invoiceItemPojo);
            invoiceItemPojoList.add(invoiceItemPojo);
            invoice.setTotal(invoice.getTotal() + invoiceItemPojo.getAmount());
            invoice.setTotalQuantity(invoice.getTotalQuantity() + invoiceItemPojo.getQuantity());
        }
        invoice.setInvoiceItemList(invoiceItemPojoList);
        invoiceDao.insert(invoice);
        return invoice;
    }

    public void delete(Integer id) {
        orderDao.delete(id);
    }
}
