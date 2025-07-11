package org.example.client.unit;

import org.example.api.AbstractApi;
import org.example.api.ClientApi;
import org.example.api.ProductApi;
import org.example.dao.ClientDao;
import org.example.exception.ApiException;
import org.example.pojo.ClientPojo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ClientApiTest {

    @Mock
    private ClientDao clientDao;

    @Mock
    private ProductApi productApi;

    private ClientApi clientApi;

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

        existingClient = new ClientPojo();
        existingClient.setId(2);
        existingClient.setClientName("Existing Client");
        existingClient.setStatus(true);
        existingClient.setCreatedAt(Instant.now());
        existingClient.setUpdatedAt(Instant.now());

        clientApi = new ClientApi();
        
        // Inject the clientDao mock into the dao field of ClientApi
        Field daoField = ClientApi.class.getDeclaredField("dao");
        daoField.setAccessible(true);
        daoField.set(clientApi, clientDao);
        
        // Also inject into the AbstractApi dao field
        Field abstractDaoField = AbstractApi.class.getDeclaredField("dao");
        abstractDaoField.setAccessible(true);
        abstractDaoField.set(clientApi, clientDao);
        
        // Inject the productApi mock into the productApi field of ClientApi
        Field productApiField = ClientApi.class.getDeclaredField("productApi");
        productApiField.setAccessible(true);
        productApiField.set(clientApi, productApi);
    }

    @Test
    void testBasicSetup() {
        // Simple test to verify the test class can be loaded and run
        assertNotNull(clientApi);
        assertNotNull(testClient);
        assertEquals("Test Client", testClient.getClientName());
    }

    @Test
    void testGetByName_Success() {
        // Arrange
        lenient().when(clientDao.selectByField(eq("clientName"), eq("test client"))).thenReturn(testClient);
        lenient().when(clientDao.selectByField(eq("clientName"), eq("Test Client"))).thenReturn(testClient);

        // Act
        ClientPojo result = clientApi.getByName("Test Client");

        // Assert
        assertNotNull(result);
        assertEquals("Test Client", result.getClientName());
        verify(clientDao, atLeastOnce()).selectByField(eq("clientName"), anyString());
    }

    @Test
    void testGetByName_NotFound() {
        // Arrange
        lenient().when(clientDao.selectByField(eq("clientName"), eq("nonexistent"))).thenReturn(null);
        lenient().when(clientDao.selectByField(eq("clientName"), eq("NonExistent"))).thenReturn(null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            clientApi.getByName("NonExistent");
        });

        assertEquals("Client with clientName 'nonexistent' not found", exception.getMessage());
        verify(clientDao, atLeastOnce()).selectByField(eq("clientName"), anyString());
    }

    @Test
    void testToggleStatusById_Success_ActiveToInactive() {
        // Arrange
        when(clientDao.select(1)).thenReturn(testClient);
        when(productApi.hasProductsByClientName("Test Client")).thenReturn(false);
        doNothing().when(clientDao).toggleStatus(1);

        // Act
        clientApi.toggleStatus(1);

        // Assert
        verify(clientDao, times(1)).select(1);
        verify(productApi, times(1)).hasProductsByClientName("Test Client");
        verify(clientDao, times(1)).toggleStatus(1);
    }

    @Test
    void testToggleStatusById_Success_InactiveToActive() {
        // Arrange
        testClient.setStatus(false);
        when(clientDao.select(1)).thenReturn(testClient);
        doNothing().when(clientDao).toggleStatus(1);

        // Act
        clientApi.toggleStatus(1);

        // Assert
        verify(clientDao, times(1)).select(1);
        verify(productApi, never()).hasProductsByClientName(anyString());
        verify(clientDao, times(1)).toggleStatus(1);
    }

    @Test
    void testToggleStatusById_ClientNotFound() {
        // Arrange
        when(clientDao.select(999)).thenReturn(null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            clientApi.toggleStatus(999);
        });

        assertEquals("Client with ID 999 not found", exception.getMessage());
        verify(clientDao, times(1)).select(999);
        verify(clientDao, never()).toggleStatus(anyInt());
    }

    @Test
    void testToggleStatusById_HasProducts() {
        // Arrange
        when(clientDao.select(1)).thenReturn(testClient);
        when(productApi.hasProductsByClientName("Test Client")).thenReturn(true);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            clientApi.toggleStatus(1);
        });

        assertEquals("Client status toggle failed. Client has products.", exception.getMessage());
        verify(clientDao, times(1)).select(1);
        verify(productApi, times(1)).hasProductsByClientName("Test Client");
        verify(clientDao, never()).toggleStatus(anyInt());
    }

    @Test
    void testToggleStatusByName_Success_ActiveToInactive() {
        // Arrange
        lenient().when(clientDao.selectByField(eq("clientName"), eq("test client"))).thenReturn(testClient);
        when(productApi.hasProductsByClientName("Test Client")).thenReturn(false);
        doNothing().when(clientDao).toggleStatusByName("Test Client");

        // Act
        clientApi.toggleStatusByName("Test Client");

        // Assert
        verify(clientDao, times(1)).selectByField(eq("clientName"), eq("test client"));
        verify(productApi, times(1)).hasProductsByClientName("Test Client");
        verify(clientDao, times(1)).toggleStatusByName("Test Client");
    }

    @Test
    void testToggleStatusByName_Success_InactiveToActive() {
        // Arrange
        testClient.setStatus(false);
        lenient().when(clientDao.selectByField(eq("clientName"), eq("test client"))).thenReturn(testClient);
        doNothing().when(clientDao).toggleStatusByName("Test Client");

        // Act
        clientApi.toggleStatusByName("Test Client");

        // Assert
        verify(clientDao, times(1)).selectByField(eq("clientName"), eq("test client"));
        verify(productApi, never()).hasProductsByClientName(anyString());
        verify(clientDao, times(1)).toggleStatusByName("Test Client");
    }

    @Test
    void testToggleStatusByName_ClientNotFound() {
        // Arrange
        lenient().when(clientDao.selectByField(eq("clientName"), eq("nonexistent"))).thenReturn(null);
        lenient().when(clientDao.selectByField(eq("clientName"), eq("NonExistent"))).thenReturn(null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            clientApi.toggleStatusByName("NonExistent");
        });

        assertEquals("Client with clientName 'nonexistent' not found", exception.getMessage());
        verify(clientDao, times(1)).selectByField(eq("clientName"), anyString());
        verify(clientDao, never()).toggleStatusByName(anyString());
    }

    @Test
    void testToggleStatusByName_HasProducts() {
        // Arrange
        lenient().when(clientDao.selectByField(eq("clientName"), eq("test client"))).thenReturn(testClient);
        when(productApi.hasProductsByClientName("Test Client")).thenReturn(true);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            clientApi.toggleStatusByName("Test Client");
        });

        assertEquals("Client status toggle failed. Client has products.", exception.getMessage());
        verify(clientDao, times(1)).selectByField(eq("clientName"), eq("test client"));
        verify(productApi, times(1)).hasProductsByClientName("Test Client");
        verify(clientDao, never()).toggleStatusByName(anyString());
    }

    @Test
    void testAdd_Success() {
        // Arrange
        lenient().when(clientDao.selectByName("test client")).thenReturn(null);
        lenient().when(clientDao.selectByName("Test Client")).thenReturn(null);
        doNothing().when(clientDao).insert(any(ClientPojo.class));

        // Act
        clientApi.add(testClient);

        // Assert
        verify(clientDao, times(1)).selectByName(anyString());
        verify(clientDao, times(1)).insert(testClient);
    }

    @Test
    void testAdd_ClientAlreadyExists() {
        // Arrange
        lenient().when(clientDao.selectByName("existing client")).thenReturn(existingClient);
        lenient().when(clientDao.selectByName("Existing Client")).thenReturn(existingClient);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            existingClient.setClientName("Existing Client");
            clientApi.add(existingClient);
        });

        assertEquals("Client already exists", exception.getMessage());
        verify(clientDao, times(1)).selectByName(anyString());
        verify(clientDao, never()).insert(any(ClientPojo.class));
    }

    @Test
    void testGetAll_Success() {
        // Arrange
        List<ClientPojo> clients = Arrays.asList(testClient, existingClient);
        when(clientDao.selectAll()).thenReturn(clients);

        // Act
        List<ClientPojo> result = clientApi.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Client", result.get(0).getClientName());
        assertEquals("Existing Client", result.get(1).getClientName());
        verify(clientDao, times(1)).selectAll();
    }

    @Test
    void testGet_Success() {
        // Arrange
        when(clientDao.select(1)).thenReturn(testClient);

        // Act
        ClientPojo result = clientApi.get(1);

        // Assert
        assertNotNull(result);
        assertEquals("Test Client", result.getClientName());
        verify(clientDao, times(1)).select(1);
    }

    @Test
    void testGet_NotFound() {
        // Arrange
        when(clientDao.select(999)).thenReturn(null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            clientApi.get(999);
        });

        assertEquals("Client with ID 999 not found", exception.getMessage());
        verify(clientDao, times(1)).select(999);
    }

    @Test
    void testDelete_Success() {
        // Arrange
        when(clientDao.select(1)).thenReturn(testClient);
        doNothing().when(clientDao).delete(1);

        // Act
        clientApi.delete(1);

        // Assert
        verify(clientDao, times(1)).select(1);
        verify(clientDao, times(1)).delete(1);
    }

    @Test
    void testDelete_NotFound() {
        // Arrange
        when(clientDao.select(999)).thenReturn(null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            clientApi.delete(999);
        });

        assertEquals("Client with ID 999 not found", exception.getMessage());
        verify(clientDao, times(1)).select(999);
        verify(clientDao, never()).delete(anyInt());
    }
} 