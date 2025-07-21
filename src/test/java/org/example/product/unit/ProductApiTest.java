package org.example.product.unit;

import org.example.api.ProductApi;
import org.example.pojo.ProductPojo;
import org.example.pojo.ClientPojo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductApiTest {

    private ProductApi productApi;
    private ProductPojo testProduct;
    private ClientPojo testClient;

    @BeforeEach
    void setUp() {
        testProduct = new ProductPojo();
        testProduct.setId(1);
        testProduct.setName("Test Product");
        testProduct.setBarcode("TEST123");
        testProduct.setClientId(1);
        testProduct.setMrp(100.0);

        testClient = new ClientPojo();
        testClient.setId(1);
        testClient.setClientName("TestClient");
        testClient.setStatus(true);

        productApi = new ProductApi();
    }

    @Test
    void testBasicSetup() {
        // Simple test to verify the test class can be loaded and run
        assertNotNull(productApi);
        assertNotNull(testProduct);
        assertNotNull(testClient);
    }

    @Test
    void testProductPojoProperties() {
        // Test basic POJO functionality
        assertNotNull(testProduct);
        assertEquals(1, testProduct.getId());
        assertEquals("Test Product", testProduct.getName());
        assertEquals("TEST123", testProduct.getBarcode());
        assertEquals(1, testProduct.getClientId());
        assertEquals(100.0, testProduct.getMrp());
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