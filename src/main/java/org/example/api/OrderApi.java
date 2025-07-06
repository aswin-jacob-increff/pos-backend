package org.example.api;

import org.example.dao.OrderDao;
import org.example.exception.ApiException;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.OrderPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.example.model.InvoiceData;
import org.example.model.InvoiceItemData;
import org.example.util.Base64ToPdfUtil;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.example.util.TimeUtil;

@Service
public class OrderApi {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemApi orderItemApi;

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private RestTemplate restTemplate;

    public OrderPojo add(OrderPojo orderPojo) {
        if (Objects.isNull(orderPojo)) {
            throw new ApiException("Order cannot be null");
        }
        if (Objects.isNull(orderPojo.getOrderItems()) || orderPojo.getOrderItems().isEmpty()) {
            throw new ApiException("Order must contain at least one item");
        }
        orderPojo.setDate(Objects.nonNull(orderPojo.getDate()) ? orderPojo.getDate() : Instant.now());
        
        // Insert order first to get the ID
        orderDao.insert(orderPojo);
        
        // Combine order items with the same product ID
        List<OrderItemPojo> combinedItems = combineOrderItems(orderPojo.getOrderItems());
        
        double totalAmount = 0.0;
        for (OrderItemPojo item : combinedItems) {
            if (Objects.isNull(item)) {
                throw new ApiException("Order item cannot be null");
            }
            item.setOrder(orderPojo); // link order to item
            double amount = item.getSellingPrice() * item.getQuantity();
            item.setAmount(amount);
            orderItemApi.add(item);
            totalAmount += amount;
        }
        orderPojo.setTotal(totalAmount);
        orderDao.update(orderPojo.getId(), orderPojo);
        return orderPojo;
    }

    public OrderPojo get(Integer id) {
        OrderPojo order = orderDao.select(id);
        if (Objects.isNull(order)) {
            throw new ApiException("Order with ID " + id + " not found");
        }
        return order;
    }

    public List<OrderPojo> getAll() {
        return orderDao.selectAll();
    }

    public OrderPojo update(Integer id, OrderPojo updatedOrder) {
        OrderPojo existingOrder = orderDao.select(id);
        if (Objects.isNull(existingOrder)) {
            throw new ApiException("Order with ID " + id + " not found");
        }
        existingOrder.setDate(updatedOrder.getDate());
        existingOrder.setTotal(updatedOrder.getTotal());
        existingOrder.setOrderItems(updatedOrder.getOrderItems());

        orderDao.update(id, existingOrder);
        return orderDao.select(id);
    }

    public void delete(Integer id) {
        OrderPojo order = orderDao.select(id);
        if (Objects.isNull(order)) {
            throw new ApiException("Order with ID " + id + " not found");
        }
        orderDao.delete(id);
    }

    public void cancelOrder(Integer orderId) {
        OrderPojo order = orderDao.select(orderId);
        if (Objects.isNull(order)) {
            throw new ApiException("Order with ID " + orderId + " not found");
        }
        
        // Restore inventory quantities for all order items
        List<OrderItemPojo> orderItems = orderItemApi.getByOrderId(orderId);
        for (OrderItemPojo orderItem : orderItems) {
            Integer productId = orderItem.getProduct().getId();
            Integer quantityToRestore = orderItem.getQuantity();
            
            // Add the quantity back to inventory
            inventoryApi.addStock(productId, quantityToRestore);
        }
        
        // Delete the order (this will cascade to order items)
        orderDao.delete(orderId);
    }

    public String generateInvoice(Integer orderId) throws Exception {
        OrderPojo order = get(orderId);
        List<OrderItemPojo> orderItems = orderItemApi.getByOrderId(orderId);
        InvoiceData invoiceData = new InvoiceData();
        invoiceData.setOrderId(orderId);
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
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<InvoiceData> entity = new HttpEntity<>(invoiceData, headers);
        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity(invoiceAppUrl, entity, String.class);
        } catch (Exception e) {
            throw new ApiException("Failed to connect to invoice service: " + e.getMessage());
        }
        if (Objects.isNull(response) || !response.getStatusCode().is2xxSuccessful() || Objects.isNull(response.getBody())) {
            throw new ApiException("Failed to generate invoice for order ID: " + orderId);
        }
        String fileName = "order-" + orderId + ".pdf";
        String savePath = "src/main/resources/invoice/" + fileName;
        try {
            Base64ToPdfUtil.saveBase64AsPdf(response.getBody(), savePath);
        } catch (Exception e) {
            throw new ApiException("Failed to save invoice PDF: " + e.getMessage());
        }
        return "/invoice/" + fileName;
    }

    /**
     * Combines order items with the same product ID by adding their quantities
     * and using the selling price from the first occurrence
     */
    private List<OrderItemPojo> combineOrderItems(List<OrderItemPojo> orderItems) {
        java.util.Map<Integer, OrderItemPojo> combinedMap = new java.util.HashMap<>();
        
        for (OrderItemPojo item : orderItems) {
            Integer productId = item.getProduct().getId();
            
            if (combinedMap.containsKey(productId)) {
                // Product already exists, add quantities
                OrderItemPojo existingItem = combinedMap.get(productId);
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
            } else {
                // New product, add to map
                combinedMap.put(productId, item);
            }
        }
        
        return new java.util.ArrayList<>(combinedMap.values());
    }
} 