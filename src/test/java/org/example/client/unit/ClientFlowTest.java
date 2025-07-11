package org.example.client.unit;

import org.example.flow.ClientFlow;
import org.example.api.ClientApi;
import org.example.pojo.ClientPojo;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientFlowTest {

    @Mock
    private ClientApi clientApi;

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
    }

    @Test
    void testAdd_Success() {
        // Arrange
        doNothing().when(clientApi).add(testClient);

        // Act
        ClientPojo result = clientFlow.add(testClient);

        // Assert
        assertNotNull(result);
        assertEquals(testClient.getId(), result.getId());
        assertEquals(testClient.getClientName(), result.getClientName());
        assertEquals(testClient.getStatus(), result.getStatus());
        verify(clientApi, times(1)).add(testClient);
    }

    @Test
    void testGet_Success() {
        // Arrange
        when(clientApi.get(1)).thenReturn(testClient);

        // Act
        ClientPojo result = clientFlow.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(testClient.getId(), result.getId());
        assertEquals(testClient.getClientName(), result.getClientName());
        assertEquals(testClient.getStatus(), result.getStatus());
        verify(clientApi, times(1)).get(1);
    }

    @Test
    void testGet_NotFound() {
        // Arrange
        when(clientApi.get(999)).thenReturn(null);

        // Act
        ClientPojo result = clientFlow.get(999);

        // Assert
        assertNull(result);
        verify(clientApi, times(1)).get(999);
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
        ClientPojo updatedClient = new ClientPojo();
        updatedClient.setId(1);
        updatedClient.setClientName("Updated Client");
        updatedClient.setStatus(false);

        doNothing().when(clientApi).update(eq(1), any(ClientPojo.class));
        when(clientApi.get(1)).thenReturn(updatedClient);

        // Act
        ClientPojo result = clientFlow.update(1, updatedClient);

        // Assert
        assertNotNull(result);
        assertEquals(updatedClient.getId(), result.getId());
        assertEquals(updatedClient.getClientName(), result.getClientName());
        assertEquals(updatedClient.getStatus(), result.getStatus());
        verify(clientApi, times(1)).update(1, updatedClient);
        verify(clientApi, times(1)).get(1);
    }

    @Test
    void testDelete_Success() {
        // Arrange
        doNothing().when(clientApi).delete(1);

        // Act
        clientFlow.delete(1);

        // Assert
        verify(clientApi, times(1)).delete(1);
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
        lenient().when(clientApi.getByName("nonexistent")).thenReturn(null);
        lenient().when(clientApi.getByName("NonExistent")).thenReturn(null);

        // Act
        ClientPojo result = clientFlow.getByName("NonExistent");

        // Assert
        assertNull(result);
        verify(clientApi, times(1)).getByName("NonExistent");
    }

    @Test
    void testDeleteClientByName_Success() {
        // Arrange
        lenient().when(clientApi.getByName("test client")).thenReturn(testClient);
        lenient().when(clientApi.getByName("Test Client")).thenReturn(testClient);
        doNothing().when(clientApi).delete(1);

        // Act
        clientFlow.deleteClientByName("Test Client");

        // Assert
        verify(clientApi, times(1)).getByName("Test Client");
        verify(clientApi, times(1)).delete(1);
    }

    @Test
    void testDeleteClientByName_NotFound() {
        // Arrange
        lenient().when(clientApi.getByName("nonexistent")).thenReturn(null);
        lenient().when(clientApi.getByName("NonExistent")).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clientFlow.deleteClientByName("NonExistent");
        });

        assertEquals("Client with name 'NonExistent' not found.", exception.getMessage());
        verify(clientApi, times(1)).getByName("NonExistent");
        verify(clientApi, never()).delete(anyInt());
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

    @Test
    void testDeleteByField_Success() {
        // Arrange
        when(clientApi.getByField("clientName", "test client")).thenReturn(testClient);
        doNothing().when(clientApi).delete(1);

        // Act
        clientFlow.deleteByField("clientName", "test client");

        // Assert
        verify(clientApi, times(1)).getByField("clientName", "test client");
        verify(clientApi, times(1)).delete(1);
    }

    @Test
    void testDeleteByField_NotFound() {
        // Arrange
        when(clientApi.getByField("clientName", "nonexistent")).thenReturn(null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            clientFlow.deleteByField("clientName", "nonexistent");
        });

        assertEquals("Client with clientName 'nonexistent' not found", exception.getMessage());
        verify(clientApi, times(1)).getByField("clientName", "nonexistent");
        verify(clientApi, never()).delete(anyInt());
    }

    @Test
    void testSafeDelete_Success() {
        // Arrange
        when(clientApi.get(1)).thenReturn(testClient);
        doNothing().when(clientApi).delete(1);

        // Act
        clientFlow.safeDelete(1);

        // Assert
        verify(clientApi, times(1)).get(1);
        verify(clientApi, times(1)).delete(1);
    }

    @Test
    void testSafeDelete_NotFound() {
        // Arrange
        when(clientApi.get(999)).thenReturn(null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            clientFlow.safeDelete(999);
        });

        assertEquals("Client with ID 999 not found", exception.getMessage());
        verify(clientApi, times(1)).get(999);
        verify(clientApi, never()).delete(anyInt());
    }

    @Test
    void testSafeDeleteByField_Success() {
        // Arrange
        when(clientApi.getByField("clientName", "test client")).thenReturn(testClient);
        doNothing().when(clientApi).delete(1);

        // Act
        clientFlow.safeDeleteByField("clientName", "test client");

        // Assert
        verify(clientApi, times(2)).getByField("clientName", "test client"); // Called twice: once in validateEntityExistsByField and once in deleteByField
        verify(clientApi, times(1)).delete(1);
    }

    @Test
    void testSafeDeleteByField_NotFound() {
        // Arrange
        when(clientApi.getByField("clientName", "nonexistent")).thenReturn(null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            clientFlow.safeDeleteByField("clientName", "nonexistent");
        });

        assertEquals("Client with clientName 'nonexistent' not found", exception.getMessage());
        verify(clientApi, times(1)).getByField("clientName", "nonexistent");
        verify(clientApi, never()).delete(anyInt());
    }
} 