package org.example.inventory.unit;

import org.example.api.InventoryApi;
import org.example.api.ClientApi;
import org.example.dao.InventoryDao;
import org.example.pojo.InventoryPojo;
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
class InventoryApiTest {

    @Mock
    private InventoryDao inventoryDao;

    @Mock
    private ClientApi clientApi;

    @InjectMocks
    private InventoryApi inventoryApi;

    private InventoryPojo testPojo;
    private ClientPojo testClient;

    @BeforeEach
    void setUp() throws Exception {
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

        // Manually inject the dao field using reflection (AbstractApi has 'dao' field)
        Field daoField = inventoryApi.getClass().getSuperclass().getDeclaredField("dao");
        daoField.setAccessible(true);
        daoField.set(inventoryApi, inventoryDao);

        // Manually inject the clientApi field using reflection
        Field clientApiField = inventoryApi.getClass().getDeclaredField("clientApi");
        clientApiField.setAccessible(true);
        clientApiField.set(inventoryApi, clientApi);
    }

    @Test
    void testAdd_Success() {
        // Arrange
        when(clientApi.getByName("TestClient")).thenReturn(testClient);
        doNothing().when(inventoryDao).insert(any(InventoryPojo.class));

        // Act
        inventoryApi.add(testPojo);

        // Assert
        verify(inventoryDao, times(1)).insert(testPojo);
    }

    @Test
    void testAdd_NullPojo() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.add(null));
        verify(inventoryDao, never()).insert(any());
    }

    @Test
    void testGet_Success() {
        // Arrange
        when(inventoryDao.select(1)).thenReturn(testPojo);

        // Act
        InventoryPojo result = inventoryApi.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(testPojo.getId(), result.getId());
        verify(inventoryDao, times(1)).select(1);
    }

    @Test
    void testGet_InvalidId() {
        // Given
        when(inventoryDao.select(0)).thenReturn(null);
        when(inventoryDao.select(-1)).thenReturn(null);

        // When & Then
        assertThrows(ApiException.class, () -> inventoryApi.get(0));
        assertThrows(ApiException.class, () -> inventoryApi.get(-1));
        assertThrows(ApiException.class, () -> inventoryApi.get(null));
        verify(inventoryDao).select(0);
        verify(inventoryDao).select(-1);
        verify(inventoryDao, never()).select(null);
    }

    @Test
    void testGetAll_Success() {
        // Arrange
        InventoryPojo pojo1 = new InventoryPojo();
        pojo1.setId(1);
        pojo1.setProductBarcode("TEST123");
        InventoryPojo pojo2 = new InventoryPojo();
        pojo2.setId(2);
        pojo2.setProductBarcode("TEST456");
        List<InventoryPojo> pojos = Arrays.asList(pojo1, pojo2);
        when(inventoryDao.selectAll()).thenReturn(pojos);

        // Act
        List<InventoryPojo> result = inventoryApi.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("TEST123", result.get(0).getProductBarcode());
        assertEquals("TEST456", result.get(1).getProductBarcode());
        verify(inventoryDao, times(1)).selectAll();
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        when(clientApi.getByName("TestClient")).thenReturn(testClient);
        when(inventoryDao.select(1)).thenReturn(testPojo);
        doNothing().when(inventoryDao).update(eq(1), any(InventoryPojo.class));

        // Act
        inventoryApi.update(1, testPojo);

        // Assert
        verify(inventoryDao, times(1)).update(1, testPojo);
    }

    @Test
    void testUpdate_InvalidId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.update(0, testPojo));
        assertThrows(ApiException.class, () -> inventoryApi.update(-1, testPojo));
        assertThrows(ApiException.class, () -> inventoryApi.update(null, testPojo));
        verify(inventoryDao, never()).update(anyInt(), any());
    }

    @Test
    void testUpdate_NullPojo() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.update(1, null));
        verify(inventoryDao, never()).update(anyInt(), any());
    }
} 