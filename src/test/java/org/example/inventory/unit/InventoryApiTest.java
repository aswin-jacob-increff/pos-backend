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
        testPojo.setProductId(1);
        testPojo.setQuantity(10);
        
        testClient = new ClientPojo();
        testClient.setId(1);
        testClient.setClientName("Test Client");
        testClient.setStatus(true);
    }

    @Test
    void testBasicSetup() {
        assertNotNull(testPojo);
        assertEquals(1, testPojo.getId());
        assertEquals(1, testPojo.getProductId());
        assertEquals(10, testPojo.getQuantity());
    }

    @Test
    void testInventoryPojoProperties() {
        testPojo.setId(2);
        testPojo.setProductId(3);
        testPojo.setQuantity(20);
        
        assertEquals(2, testPojo.getId());
        assertEquals(3, testPojo.getProductId());
        assertEquals(20, testPojo.getQuantity());
    }

    @Test
    void testClientPojoProperties() {
        assertNotNull(testClient);
        assertEquals(1, testClient.getId());
        assertEquals("Test Client", testClient.getClientName());
        assertTrue(testClient.getStatus());
    }
} 