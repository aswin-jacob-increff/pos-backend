package org.example.inventory.unit;

import org.example.api.InventoryApi;
import org.example.pojo.InventoryPojo;
import org.example.pojo.ClientPojo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InventoryApiTest {

    private InventoryApi inventoryApi;
    private InventoryPojo testPojo;
    private ClientPojo testClient;

    @BeforeEach
    void setUp() {
        testPojo = new InventoryPojo();
        testPojo.setId(1);
        testPojo.setProductBarcode("TEST123");
        testPojo.setProductName("Test Product");
        testPojo.setClientName("TestClient");
        testPojo.setProductMrp(100.0);
        testPojo.setQuantity(10);

        testClient = new ClientPojo();
        testClient.setId(1);
        testClient.setClientName("TestClient");
        testClient.setStatus(true);

        inventoryApi = new InventoryApi();
    }

    @Test
    void testBasicSetup() {
        // Simple test to verify the test class can be loaded and run
        assertNotNull(inventoryApi);
        assertNotNull(testPojo);
        assertNotNull(testClient);
    }

    @Test
    void testInventoryPojoProperties() {
        // Test basic POJO functionality
        assertNotNull(testPojo);
        assertEquals(1, testPojo.getId());
        assertEquals("TEST123", testPojo.getProductBarcode());
        assertEquals("Test Product", testPojo.getProductName());
        assertEquals("TestClient", testPojo.getClientName());
        assertEquals(100.0, testPojo.getProductMrp());
        assertEquals(10, testPojo.getQuantity());
    }

    @Test
    void testClientPojoProperties() {
        // Test basic POJO functionality
        assertNotNull(testClient);
        assertEquals(1, testClient.getId());
        assertEquals("TestClient", testClient.getClientName());
        assertTrue(testClient.getStatus());
    }
} 