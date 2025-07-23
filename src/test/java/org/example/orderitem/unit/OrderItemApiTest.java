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
    private OrderItemPojo testOrderItem;
    private InventoryPojo testInventory;

    @BeforeEach
    void setUp() {
        testOrderItem = new OrderItemPojo();
        testOrderItem.setId(1);
        testOrderItem.setOrderId(1);
        testOrderItem.setProductBarcode("TEST123");
        testOrderItem.setProductName("Test Product");
        testOrderItem.setQuantity(2);
        testOrderItem.setSellingPrice(50.0);
        testOrderItem.setAmount(100.0);

        testInventory = new InventoryPojo();
        testInventory.setId(1);
        testInventory.setProductId(1); // Use productId instead of productBarcode
        testInventory.setQuantity(10);

        orderItemApi = new OrderItemApi();
    }

    @Test
    void testBasicSetup() {
        // Simple test to verify the test class can be loaded and run
        assertNotNull(orderItemApi);
        assertNotNull(testOrderItem);
        assertNotNull(testInventory);
    }

    @Test
    void testOrderItemPojoProperties() {
        // Test basic POJO functionality
        assertNotNull(testOrderItem);
        assertEquals(1, testOrderItem.getId());
        assertEquals(1, testOrderItem.getOrderId());
        assertEquals("TEST123", testOrderItem.getProductBarcode());
        assertEquals("Test Product", testOrderItem.getProductName());
        assertEquals(2, testOrderItem.getQuantity());
        assertEquals(50.0, testOrderItem.getSellingPrice());
        assertEquals(100.0, testOrderItem.getAmount());
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