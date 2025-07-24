package org.example.orderitem.unit;

import org.example.api.OrderItemApi;
import org.example.pojo.OrderItemPojo;
import org.example.pojo.InventoryPojo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderItemApiTest {

    private OrderItemApi orderItemApi;
    private OrderItemPojo testPojo;
    private InventoryPojo testInventory;

    @BeforeEach
    void setUp() {
        testPojo = new OrderItemPojo();
        testPojo.setId(1);
        testPojo.setOrderId(1);
        testPojo.setProductId(1);
        testPojo.setQuantity(5);
        testPojo.setSellingPrice(100.0);
        testPojo.setAmount(500.0);
    }

    @Test
    void testBasicSetup() {
        // Simple test to verify the test class can be loaded and run
        assertNotNull(orderItemApi);
        assertNotNull(testPojo);
        assertNotNull(testInventory);
    }

    @Test
    void testOrderItemPojoProperties() {
        // Test basic POJO functionality
        assertNotNull(testPojo);
        assertEquals(1, testPojo.getId());
        assertEquals(1, testPojo.getOrderId());
        assertEquals(1, testPojo.getProductId());
        assertEquals(5, testPojo.getQuantity());
        assertEquals(100.0, testPojo.getSellingPrice());
        assertEquals(500.0, testPojo.getAmount());
    }

    @Test
    void testInventoryPojoProperties() {
        // Test basic POJO functionality
        assertNotNull(testInventory);
        assertEquals(1, testInventory.getId());
        assertEquals(1, testInventory.getProductId()); // Use productId instead of productBarcode
        assertEquals(10, testInventory.getQuantity());
    }
} 