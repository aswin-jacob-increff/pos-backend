package org.example.client.unit;

import org.example.flow.ClientFlow;
import org.example.api.ClientApi;
import org.example.api.ProductApi;
import org.example.pojo.ClientPojo;
import org.example.pojo.ProductPojo;
import org.example.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientFlowTest {

    @Mock
    private ClientApi clientApi;

    @Mock
    private ProductApi productApi;

    @InjectMocks
    private ClientFlow clientFlow;

    private ClientPojo testClient;
    private ClientPojo existingClient;

    @BeforeEach
    void setUp() throws Exception {
        testClient = new ClientPojo();
        testClient.setId(1);
        testClient.setClientName("Test Client");
        testClient.setStatus(true);
        testClient.setCreatedAt(Instant.now());
        testClient.setUpdatedAt(Instant.now());
        testClient.setVersion(1);

        existingClient = new ClientPojo();
        existingClient.setId(2);
        existingClient.setClientName("Existing Client");
        existingClient.setStatus(true);
        existingClient.setCreatedAt(Instant.now());
        existingClient.setUpdatedAt(Instant.now());
        existingClient.setVersion(1);

        // Manually inject the api field using reflection
        Field apiField = clientFlow.getClass().getSuperclass().getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(clientFlow, clientApi);

        // Manually inject the productApi field using reflection
        Field productApiField = clientFlow.getClass().getDeclaredField("productApi");
        productApiField.setAccessible(true);
        productApiField.set(clientFlow, productApi);
    }

    @Test
    void testAdd_Success() {
        // Arrange
        doNothing().when(clientApi).add(any(ClientPojo.class));

        // Act
        clientFlow.add(testClient);

        // Assert
        verify(clientApi, times(1)).add(testClient);
    }

    @Test
    void testGetAll_Success() {
        // Arrange
        List<ClientPojo> clients = Arrays.asList(testClient, existingClient);
        when(clientApi.getAll()).thenReturn(clients);

        // Act
        List<ClientPojo> result = clientFlow.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Client", result.get(0).getClientName());
        assertEquals("Existing Client", result.get(1).getClientName());
        verify(clientApi, times(1)).getAll();
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        ClientPojo client = new ClientPojo();
        client.setId(1);
        client.setClientName("Updated Client");
        client.setStatus(true);

        when(clientApi.get(1)).thenReturn(client);
        doNothing().when(clientApi).update(1, client);

        // When
        clientFlow.update(1, client);

        // Then
        verify(clientApi).update(1, client);
    }

    @Test
    void testUpdate_NullId() {
        // When & Then
        assertThrows(ApiException.class, () -> clientFlow.update(null, new ClientPojo()));
        verify(clientApi, never()).update(any(), any());
    }

    @Test
    void testUpdate_NullEntity() {
        // When & Then
        assertThrows(ApiException.class, () -> clientFlow.update(1, null));
        verify(clientApi, never()).update(any(), any());
    }

    @Test
    void testGetByName_Success() {
        // Arrange
        lenient().when(clientApi.getByName("test client")).thenReturn(testClient);
        lenient().when(clientApi.getByName("Test Client")).thenReturn(testClient);

        // Act
        ClientPojo result = clientFlow.getByName("Test Client");

        // Assert
        assertNotNull(result);
        assertEquals(testClient.getId(), result.getId());
        assertEquals(testClient.getClientName(), result.getClientName());
        assertEquals(testClient.getStatus(), result.getStatus());
        verify(clientApi, times(1)).getByName("Test Client");
    }

    @Test
    void testGetByName_NotFound() {
        // Arrange
        when(clientApi.getByName("nonexistent")).thenReturn(null);

        // Act
        ClientPojo result = clientFlow.getByName("nonexistent");

        // Assert
        assertNull(result);
        verify(clientApi, times(1)).getByName("nonexistent");
    }

    @Test
    void testToggleStatus_Success() {
        // Arrange
        when(clientApi.get(1)).thenReturn(testClient);
        doNothing().when(clientApi).toggleStatus(1);

        // Act
        clientFlow.toggleStatus(1);

        // Assert
        verify(clientApi, times(1)).get(1);
        verify(clientApi, times(1)).toggleStatus(1);
    }

    @Test
    void testToggleStatus_NotFound() {
        // Arrange
        when(clientApi.get(999)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clientFlow.toggleStatus(999);
        });

        assertEquals("Client with ID '999' not found.", exception.getMessage());
        verify(clientApi, times(1)).get(999);
        verify(clientApi, never()).toggleStatus(anyInt());
    }

    @Test
    void testToggleStatusByName_Success() {
        // Arrange
        lenient().when(clientApi.getByName("test client")).thenReturn(testClient);
        lenient().when(clientApi.getByName("Test Client")).thenReturn(testClient);
        lenient().doNothing().when(clientApi).toggleStatusByName("test client");
        lenient().doNothing().when(clientApi).toggleStatusByName("Test Client");

        // Act
        clientFlow.toggleStatusByName("Test Client");

        // Assert
        verify(clientApi, times(1)).getByName("Test Client");
        verify(clientApi, times(1)).toggleStatusByName("Test Client");
    }

    @Test
    void testToggleStatusByName_NotFound() {
        // Arrange
        lenient().when(clientApi.getByName("nonexistent")).thenReturn(null);
        lenient().when(clientApi.getByName("NonExistent")).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clientFlow.toggleStatusByName("NonExistent");
        });

        assertEquals("Client with name 'NonExistent' not found.", exception.getMessage());
        verify(clientApi, times(1)).getByName("NonExistent");
        verify(clientApi, never()).toggleStatusByName(anyString());
    }

    @Test
    void testCreateClient_Success() {
        // Arrange
        doNothing().when(clientApi).add(any(ClientPojo.class));

        // Act
        clientFlow.createClient("New Client");

        // Assert
        verify(clientApi, times(1)).add(any(ClientPojo.class));
        // Verify the created client has the correct name
        verify(clientApi).add(argThat(client -> 
            "New Client".equals(client.getClientName()) && 
            client.getStatus() != null
        ));
    }

    @Test
    void testCreateClient_WithEmptyName() {
        // Arrange
        doNothing().when(clientApi).add(any(ClientPojo.class));

        // Act
        clientFlow.createClient("");

        // Assert
        verify(clientApi, times(1)).add(any(ClientPojo.class));
        verify(clientApi).add(argThat(client -> 
            "".equals(client.getClientName())
        ));
    }

    @Test
    void testGetByField_Success() {
        // Arrange
        when(clientApi.getByField("clientName", "test client")).thenReturn(testClient);

        // Act
        ClientPojo result = clientFlow.getByField("clientName", "test client");

        // Assert
        assertNotNull(result);
        assertEquals(testClient.getId(), result.getId());
        assertEquals(testClient.getClientName(), result.getClientName());
        verify(clientApi, times(1)).getByField("clientName", "test client");
    }

    @Test
    void testFindByField_Success() {
        // Arrange
        when(clientApi.findByField("clientName", "test client")).thenReturn(testClient);

        // Act
        ClientPojo result = clientFlow.findByField("clientName", "test client");

        // Assert
        assertNotNull(result);
        assertEquals(testClient.getId(), result.getId());
        assertEquals(testClient.getClientName(), result.getClientName());
        verify(clientApi, times(1)).findByField("clientName", "test client");
    }

    @Test
    void testFindByField_NotFound() {
        // Arrange
        when(clientApi.findByField("clientName", "nonexistent")).thenReturn(null);

        // Act
        ClientPojo result = clientFlow.findByField("clientName", "nonexistent");

        // Assert
        assertNull(result);
        verify(clientApi, times(1)).findByField("clientName", "nonexistent");
    }

    @Test
    void testGetByFields_Success() {
        // Arrange
        String[] fieldNames = {"clientName", "status"};
        Object[] values = {"test client", true};
        List<ClientPojo> clients = Arrays.asList(testClient);
        when(clientApi.getByFields(fieldNames, values)).thenReturn(clients);

        // Act
        List<ClientPojo> result = clientFlow.getByFields(fieldNames, values);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testClient.getId(), result.get(0).getId());
        verify(clientApi, times(1)).getByFields(fieldNames, values);
    }
} 