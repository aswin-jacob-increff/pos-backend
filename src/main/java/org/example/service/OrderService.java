package org.example.service;

import org.example.dao.OrderDao;
import org.example.exception.ApiException;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.OrderPojo;
import org.example.pojo.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import org.example.model.InvoiceData;
import org.example.model.InvoiceItemData;
import org.example.util.Base64ToPdfUtil;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

@Service
public class OrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private InventoryService inventoryService;

    public OrderPojo add(OrderPojo orderPojo) {
        orderPojo.setDate(Instant.now());
        if (orderPojo.getStatus() == null) {
            orderPojo.setStatus(OrderStatus.CREATED);
        }
        orderDao.insert(orderPojo);
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
        OrderPojo order = orderDao.select(id);
        if (order == null) {
            throw new ApiException("Order with ID " + id + " not found");
        }
        return order;
    }

    public List<OrderPojo> getAll() {
        return orderDao.selectAll();
    }

    public OrderPojo update(Integer id, OrderPojo updatedOrder) {
        OrderPojo existingOrder = orderDao.select(id);
        if (existingOrder == null) {
            throw new ApiException("Order with ID " + id + " not found");
        }
        existingOrder.setDate(updatedOrder.getDate());
        if (updatedOrder.getStatus() != null) {
            validateStatusTransition(existingOrder.getStatus(), updatedOrder.getStatus());
            existingOrder.setStatus(updatedOrder.getStatus());
        }

        orderDao.update(id, existingOrder);
        return orderDao.select(id);
    }

    public void delete(Integer id) {
        OrderPojo order = orderDao.select(id);
        if (order == null) {
            throw new ApiException("Order with ID " + id + " not found");
        }
        orderDao.delete(id);
    }

    public void updateStatusToInvoiced(Integer orderId) {
        OrderPojo order = orderDao.select(orderId);
        if (order == null) {
            throw new ApiException("Order with ID " + orderId + " not found");
        }
        
        validateStatusTransition(order.getStatus(), OrderStatus.INVOICED);
        order.setStatus(OrderStatus.INVOICED);
        orderDao.update(orderId, order);
    }

    public void updateStatusToCancelled(Integer orderId) {
        OrderPojo order = orderDao.select(orderId);
        if (order == null) {
            throw new ApiException("Order with ID " + orderId + " not found");
        }
        
        validateStatusTransition(order.getStatus(), OrderStatus.CANCELLED);
        
        // Restore inventory quantities for all order items
        List<OrderItemPojo> orderItems = orderItemService.getByOrderId(orderId);
        for (OrderItemPojo orderItem : orderItems) {
            Integer productId = orderItem.getProduct().getId();
            Integer quantityToRestore = orderItem.getQuantity();
            
            // Add the quantity back to inventory
            inventoryService.addStock(productId, quantityToRestore);
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        orderDao.update(orderId, order);
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == null) {
            return; // Allow setting initial status
        }
        
        switch (currentStatus) {
            case CREATED:
                if (newStatus == OrderStatus.CREATED) {
                    throw new ApiException("Order is already in CREATED status");
                }
                break;
            case INVOICED:
                if (newStatus == OrderStatus.CREATED) {
                    throw new ApiException("Cannot revert INVOICED order to CREATED status");
                }
                break;
            case CANCELLED:
                throw new ApiException("Cannot update status of CANCELLED order");
            default:
                throw new ApiException("Unknown order status: " + currentStatus);
        }
    }

    public String generateInvoice(Integer orderId) throws Exception {
        OrderPojo order = get(orderId);
        if (order.getStatus() == OrderStatus.INVOICED) {
            throw new ApiException("Invoice already generated for order ID: " + orderId);
        }
        List<OrderItemPojo> orderItems = orderItemService.getByOrderId(orderId);
        InvoiceData invoiceData = new InvoiceData();
        invoiceData.setOrderId(orderId);
        invoiceData.setDateTime(order.getDate());
        invoiceData.setTotal(order.getTotal());
        invoiceData.setTotalQuantity(orderItems.stream().mapToInt(OrderItemPojo::getQuantity).sum());
        invoiceData.setId(order.getId());
        List<InvoiceItemData> itemDataList = orderItems.stream().map(item -> {
            InvoiceItemData itemData = new InvoiceItemData();
            itemData.setId(item.getId());
            itemData.setProductId(item.getProduct().getId());
            itemData.setProductName(item.getProduct().getName());
            itemData.setProductBarcode(item.getProduct().getBarcode());
            itemData.setClientId(item.getProduct().getClient().getId());
            itemData.setClientName(item.getProduct().getClient().getClientName());
            itemData.setPrice(item.getSellingPrice());
            itemData.setQuantity(item.getQuantity());
            itemData.setAmount(item.getAmount());
            return itemData;
        }).toList();
        invoiceData.setInvoiceItemPojoList(itemDataList);
        String invoiceAppUrl = "http://localhost:8081/api/invoice";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<InvoiceData> entity = new HttpEntity<>(invoiceData, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(invoiceAppUrl, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new ApiException("Failed to generate invoice for order ID: " + orderId);
        }
        String fileName = "order-" + orderId + ".pdf";
        String savePath = "src/main/resources/invoice/" + fileName;
        Base64ToPdfUtil.saveBase64AsPdf(response.getBody(), savePath);
        updateStatusToInvoiced(orderId);
        return "/invoice/" + fileName;
    }
}
