package org.example.order.unit;

import org.example.api.OrderApi;
import org.example.pojo.OrderPojo;
import org.example.pojo.OrderItemPojo;
import org.example.model.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderApiTest {

    private OrderApi orderApi;
    private OrderPojo testOrder;
    private OrderItemPojo testOrderItem;

    @BeforeEach
    void setUp() {
        testOrder = new OrderPojo();
        testOrder.setId(1);
        testOrder.setDate(Instant.now());
        testOrder.setTotal(100.0);
        testOrder.setStatus(OrderStatus.CREATED);

        testOrderItem = new OrderItemPojo();
        testOrderItem.setId(1);
        testOrderItem.setOrderId(1);
        testOrderItem.setProductId(1);
        testOrderItem.setQuantity(2);
        testOrderItem.setSellingPrice(50.0);
        testOrderItem.setAmount(100.0);

        orderApi = new OrderApi();
    }

    @Test
    void testBasicSetup() {
        // Simple test to verify the test class can be loaded and run
        assertNotNull(orderApi);
        assertNotNull(testOrder);
        assertNotNull(testOrderItem);
    }

    @Test
    void testOrderPojoProperties() {
        // Test basic POJO functionality
        assertNotNull(testOrder);
        assertEquals(1, testOrder.getId());
        assertNotNull(testOrder.getDate());
        assertEquals(100.0, testOrder.getTotal());
        assertEquals(OrderStatus.CREATED, testOrder.getStatus());
    }

    @Test
    void testOrderItemPojoProperties() {
        // Test basic POJO functionality
        assertNotNull(testOrderItem);
        assertEquals(1, testOrderItem.getId());
        assertEquals(1, testOrderItem.getOrderId());
        assertEquals(1, testOrderItem.getProductId());
        assertEquals(2, testOrderItem.getQuantity());
        assertEquals(50.0, testOrderItem.getSellingPrice());
        assertEquals(100.0, testOrderItem.getAmount());
    }
} 