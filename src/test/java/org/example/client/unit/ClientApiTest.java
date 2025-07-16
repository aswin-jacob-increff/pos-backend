package org.example.client.unit;

import org.example.api.ClientApi;
import org.example.pojo.ClientPojo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ClientApiTest {

    private ClientApi clientApi;
    private ClientPojo testClient;

    @BeforeEach
    void setUp() {
        testClient = new ClientPojo();
        testClient.setId(1);
        testClient.setClientName("Test Client");
        testClient.setStatus(true);
        testClient.setCreatedAt(Instant.now());
        testClient.setUpdatedAt(Instant.now());

        clientApi = new ClientApi();
    }

    @Test
    void testBasicSetup() {
        // Simple test to verify the test class can be loaded and run
        assertNotNull(clientApi);
        assertNotNull(testClient);
        assertEquals("Test Client", testClient.getClientName());
    }

    @Test
    void testClientPojoProperties() {
        // Test basic POJO functionality
        assertNotNull(testClient);
        assertEquals(1, testClient.getId());
        assertEquals("Test Client", testClient.getClientName());
        assertTrue(testClient.getStatus());
        assertNotNull(testClient.getCreatedAt());
        assertNotNull(testClient.getUpdatedAt());
    }
} 