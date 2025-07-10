package org.example.api;

import org.example.pojo.InventoryPojo;
import org.example.pojo.OrderItemPojo;
import org.example.dao.OrderItemDao;
import org.example.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Objects;

@Service
public class OrderItemApi {

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private InventoryApi inventoryApi;

    public OrderItemPojo add(OrderItemPojo orderItemPojo) {
        String productBarcode = orderItemPojo.getProductBarcode();
        InventoryPojo inventoryPojo = inventoryApi.getByProductBarcode(productBarcode);

        if (Objects.isNull(inventoryPojo)) {
            throw new ApiException("No inventory found for product barcode: " + productBarcode);
        }

        if (orderItemPojo.getQuantity() > inventoryPojo.getQuantity()) {
            throw new ApiException("Quantity must be less than available stock in inventory. Available: " + inventoryPojo.getQuantity() + ", Requested: " + orderItemPojo.getQuantity());
        }

        // ✅ Calculate amount
        double amount = orderItemPojo.getSellingPrice() * orderItemPojo.getQuantity();
        orderItemPojo.setAmount(amount);

        // ✅ Update inventory
        inventoryApi.removeStock(productBarcode, orderItemPojo.getQuantity());

        orderItemDao.insert(orderItemPojo);
        return orderItemPojo;
    }

    public OrderItemPojo get(Integer id) {
        OrderItemPojo orderItem = orderItemDao.select(id);
        if (Objects.isNull(orderItem)) {
            throw new ApiException("Order item with ID " + id + " not found");
        }
        return orderItem;
    }

    public List<OrderItemPojo> getAll() {
        return orderItemDao.selectAll();
    }

    public List<OrderItemPojo> getByOrderId(Integer orderId) {
        return orderItemDao.selectByOrderId(orderId);
    }

    public List<OrderItemPojo> getByProductBarcode(String barcode) {
        return orderItemDao.selectByProductBarcode(barcode);
    }

    public OrderItemPojo update(Integer id, OrderItemPojo updatedOrderItem) {
        OrderItemPojo existingOrderItem = orderItemDao.select(id);
        if (Objects.isNull(existingOrderItem)) {
            throw new ApiException("Order item with ID " + id + " not found");
        }

        InventoryPojo inventoryPojo = inventoryApi.getByProductBarcode(existingOrderItem.getProductBarcode());
        if (Objects.isNull(inventoryPojo)) {
            throw new ApiException("No inventory found for product barcode: " + existingOrderItem.getProductBarcode());
        }

        // ✅ Revert old quantity to inventory before check
        int oldQty = existingOrderItem.getQuantity();
        int newQty = updatedOrderItem.getQuantity();
        int available = inventoryPojo.getQuantity() + oldQty;

        if (newQty > available) {
            throw new ApiException("Quantity must be less than available stock in inventory. Available: " + available + ", Requested: " + newQty);
        }

        // ✅ Update inventory - first restore old quantity, then remove new quantity
        inventoryApi.addStock(existingOrderItem.getProductBarcode(), oldQty);
        inventoryApi.removeStock(existingOrderItem.getProductBarcode(), newQty);

        // ✅ Update item and recalculate amount
        existingOrderItem.setOrder(updatedOrderItem.getOrder());
        existingOrderItem.setProductBarcode(updatedOrderItem.getProductBarcode());
        existingOrderItem.setProductName(updatedOrderItem.getProductName());
        existingOrderItem.setClientName(updatedOrderItem.getClientName());
        existingOrderItem.setProductMrp(updatedOrderItem.getProductMrp());
        existingOrderItem.setProductImageUrl(updatedOrderItem.getProductImageUrl());
        existingOrderItem.setQuantity(newQty);
        existingOrderItem.setSellingPrice(updatedOrderItem.getSellingPrice());

        double amount = updatedOrderItem.getSellingPrice() * newQty;
        existingOrderItem.setAmount(amount);

        orderItemDao.update(id, existingOrderItem);
        return orderItemDao.select(id);
    }

    public void delete(Integer id) {
        OrderItemPojo orderItem = get(id);
        InventoryPojo inventoryPojo = inventoryApi.getByProductBarcode(orderItem.getProductBarcode());

        // ✅ Restore inventory
        inventoryApi.addStock(orderItem.getProductBarcode(), orderItem.getQuantity());

        orderItemDao.delete(id);
    }
} 