package org.example.client.unit;

import org.example.api.ClientApi;
import org.example.api.ProductApi;
import org.example.dao.ClientDao;
import org.example.pojo.ClientPojo;
import org.example.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
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

    @InjectMocks
    private ClientApi clientApi;

    private ClientPojo testClient;

    @BeforeEach
    void setUp() throws Exception {
        testClient = new ClientPojo();
        testClient.setId(1);
        testClient.setClientName("test client");
        testClient.setStatus(true);

        // Inject the dao field from AbstractApi
        Field daoField = clientApi.getClass().getSuperclass().getDeclaredField("dao");
        daoField.setAccessible(true);
        daoField.set(clientApi, clientDao);
    }

    @Test
    void testGetByName_Success() {
        // Arrange
        when(clientDao.selectByField("clientName", "test client")).thenReturn(testClient);

        // Act
        ClientPojo result = clientApi.getByName("Test Client");

        // Assert
        assertNotNull(result);
        assertEquals(testClient.getId(), result.getId());
        assertEquals(testClient.getClientName(), result.getClientName());
        verify(clientDao).selectByField("clientName", "test client");
    }

    @Test
    void testGetByName_NullName() {
        // Act
        ClientPojo result = clientApi.getByName(null);

        // Assert
        assertNull(result);
        verify(clientDao, never()).selectByField(anyString(), anyString());
    }

    @Test
    void testGetByName_EmptyName() {
        // Act
        ClientPojo result = clientApi.getByName("");

        // Assert
        assertNull(result);
        verify(clientDao, never()).selectByField(anyString(), anyString());
    }

    @Test
    void testGetByName_WhitespaceName() {
        // Act
        ClientPojo result = clientApi.getByName("   ");

        // Assert
        assertNull(result);
        verify(clientDao, never()).selectByField(anyString(), anyString());
    }

    @Test
    void testGetByName_NotFound() {
        // Arrange
        when(clientDao.selectByField("clientName", "test client")).thenReturn(null);

        // Act
        ClientPojo result = clientApi.getByName("Test Client");

        // Assert
        assertNull(result);
        verify(clientDao).selectByField("clientName", "test client");
    }

    @Test
    void testGetByNameLike_Success() {
        // Arrange
        List<ClientPojo> clients = Arrays.asList(testClient);
        when(clientDao.selectByFieldLike("clientName", "test")).thenReturn(clients);

        // Act
        List<ClientPojo> result = clientApi.getByNameLike("test");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testClient.getId(), result.get(0).getId());
        verify(clientDao).selectByFieldLike("clientName", "test");
    }

    @Test
    void testGetByNameLike_NullName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientApi.getByNameLike(null));
        verify(clientDao, never()).selectByFieldLike(anyString(), anyString());
    }

    @Test
    void testGetByNameLike_EmptyName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientApi.getByNameLike(""));
        verify(clientDao, never()).selectByFieldLike(anyString(), anyString());
    }

    @Test
    void testToggleStatus_Success_ActiveToInactive() {
        // Arrange
        when(clientDao.select(1)).thenReturn(testClient);
        when(productApi.hasProductsByClientId(1)).thenReturn(false);
        doNothing().when(clientDao).toggleStatus(1);

        // Act
        clientApi.toggleStatus(1);

        // Assert
        verify(clientDao).select(1);
        verify(productApi).hasProductsByClientId(1);
        verify(clientDao).toggleStatus(1);
    }

    @Test
    void testToggleStatus_Success_InactiveToActive() {
        // Arrange
        testClient.setStatus(false);
        when(clientDao.select(1)).thenReturn(testClient);
        doNothing().when(clientDao).toggleStatus(1);

        // Act
        clientApi.toggleStatus(1);

        // Assert
        verify(clientDao).select(1);
        verify(productApi, never()).hasProductsByClientId(anyInt());
        verify(clientDao).toggleStatus(1);
    }

    @Test
    void testToggleStatus_ClientNotFound() {
        // Arrange
        when(clientDao.select(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> clientApi.toggleStatus(1));
        verify(clientDao).select(1);
        verify(productApi, never()).hasProductsByClientId(anyInt());
        verify(clientDao, never()).toggleStatus(anyInt());
    }

    @Test
    void testToggleStatus_ActiveClientWithProducts() {
        // Arrange
        when(clientDao.select(1)).thenReturn(testClient);
        when(productApi.hasProductsByClientId(1)).thenReturn(true);

        // Act & Assert
        assertThrows(ApiException.class, () -> clientApi.toggleStatus(1));
        verify(clientDao).select(1);
        verify(productApi).hasProductsByClientId(1);
        verify(clientDao, never()).toggleStatus(anyInt());
    }

    @Test
    void testToggleStatusByName_Success_ActiveToInactive() {
        // Arrange
        when(clientDao.selectByField("clientName", "test client")).thenReturn(testClient);
        when(productApi.hasProductsByClientId(1)).thenReturn(false);
        doNothing().when(clientDao).toggleStatusByName("Test Client");

        // Act
        clientApi.toggleStatusByName("Test Client");

        // Assert
        verify(clientDao).selectByField("clientName", "test client");
        verify(productApi).hasProductsByClientId(1);
        verify(clientDao).toggleStatusByName("Test Client");
    }

    @Test
    void testToggleStatusByName_Success_InactiveToActive() {
        // Arrange
        testClient.setStatus(false);
        when(clientDao.selectByField("clientName", "test client")).thenReturn(testClient);
        doNothing().when(clientDao).toggleStatusByName("Test Client");

        // Act
        clientApi.toggleStatusByName("Test Client");

        // Assert
        verify(clientDao).selectByField("clientName", "test client");
        verify(productApi, never()).hasProductsByClientId(anyInt());
        verify(clientDao).toggleStatusByName("Test Client");
    }

    @Test
    void testToggleStatusByName_ClientNotFound() {
        // Arrange
        when(clientDao.selectByField("clientName", "test client")).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> clientApi.toggleStatusByName("Test Client"));
        verify(clientDao).selectByField("clientName", "test client");
        verify(productApi, never()).hasProductsByClientId(anyInt());
        verify(clientDao, never()).toggleStatusByName(anyString());
    }

    @Test
    void testToggleStatusByName_ActiveClientWithProducts() {
        // Arrange
        when(clientDao.selectByField("clientName", "test client")).thenReturn(testClient);
        when(productApi.hasProductsByClientId(1)).thenReturn(true);

        // Act & Assert
        assertThrows(ApiException.class, () -> clientApi.toggleStatusByName("Test Client"));
        verify(clientDao).selectByField("clientName", "test client");
        verify(productApi).hasProductsByClientId(1);
        verify(clientDao, never()).toggleStatusByName(anyString());
    }

    @Test
    void testAdd_Success() {
        // Arrange
        doNothing().when(clientDao).insert(any(ClientPojo.class));

        // Act
        clientApi.add(testClient);

        // Assert
        verify(clientDao).insert(testClient);
    }

    @Test
    void testAdd_NullClient() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientApi.add(null));
        verify(clientDao, never()).insert(any());
    }

    @Test
    void testGet_Success() {
        // Arrange
        when(clientDao.select(1)).thenReturn(testClient);

        // Act
        ClientPojo result = clientApi.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(testClient.getId(), result.getId());
        verify(clientDao).select(1);
    }

    @Test
    void testGet_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientApi.get(null));
        verify(clientDao, never()).select(any());
    }

    @Test
    void testGet_NotFound() {
        // Arrange
        when(clientDao.select(999)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> clientApi.get(999));
        verify(clientDao).select(999);
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        doNothing().when(clientDao).update(eq(1), any(ClientPojo.class));

        // Act
        clientApi.update(1, testClient);

        // Assert
        verify(clientDao).update(1, testClient);
    }

    @Test
    void testUpdate_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientApi.update(null, testClient));
        verify(clientDao, never()).update(any(), any());
    }

    @Test
    void testUpdate_NullClient() {
        // Act & Assert
        assertThrows(ApiException.class, () -> clientApi.update(1, null));
        verify(clientDao, never()).update(any(), any());
    }

    @Test
    void testGetAll_Success() {
        // Arrange
        List<ClientPojo> clients = Arrays.asList(testClient, testClient);
        when(clientDao.selectAll()).thenReturn(clients);

        // Act
        List<ClientPojo> result = clientApi.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(clientDao).selectAll();
    }
} 