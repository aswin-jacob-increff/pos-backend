package org.example.inventory.unit;

import org.example.api.InventoryApi;
import org.example.api.ClientApi;
import org.example.api.ProductApi;
import org.example.dao.InventoryDao;
import org.example.pojo.InventoryPojo;
import org.example.pojo.ProductPojo;
import org.example.pojo.ClientPojo;
import org.example.exception.ApiException;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Mock
    private ProductApi productApi;

    @InjectMocks
    private InventoryApi inventoryApi;

    private InventoryPojo testInventory;
    private ProductPojo testProduct;
    private ClientPojo testClient;

    @BeforeEach
    void setUp() throws Exception {
        testClient = new ClientPojo();
        testClient.setId(1);
        testClient.setClientName("test client");
        testClient.setStatus(true);

        testProduct = new ProductPojo();
        testProduct.setId(1);
        testProduct.setName("Test Product");
        testProduct.setBarcode("123456789");
        testProduct.setMrp(100.0);
        testProduct.setClientId(1);

        testInventory = new InventoryPojo();
        testInventory.setId(1);
        testInventory.setProductId(1);
        testInventory.setQuantity(10);

        // Inject the dao field into AbstractApi
        java.lang.reflect.Field daoField = org.example.api.AbstractApi.class.getDeclaredField("dao");
        daoField.setAccessible(true);
        daoField.set(inventoryApi, inventoryDao);
    }

    @Test
    void testGetByProductId_Success() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);

        // Act
        InventoryPojo result = inventoryApi.getByProductId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals(10, result.getQuantity());
        verify(inventoryDao).getByProductId(1);
    }

    @Test
    void testGetByProductId_NotFound() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(null);

        // Act
        InventoryPojo result = inventoryApi.getByProductId(1);

        // Assert
        assertNull(result);
        verify(inventoryDao).getByProductId(1);
    }

    @Test
    void testAddStock_Success() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);
        doNothing().when(inventoryDao).update(anyInt(), any(InventoryPojo.class));

        // Act
        inventoryApi.addStock(1, 5);

        // Assert
        verify(inventoryDao).getByProductId(1);
        verify(productApi).get(1);
        verify(clientApi).get(1);
        verify(inventoryDao).update(eq(1), any(InventoryPojo.class));
    }

    @Test
    void testAddStock_NullQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.addStock(1, null));
        verify(inventoryDao, never()).getByProductId(any());
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testAddStock_ZeroQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.addStock(1, 0));
        verify(inventoryDao, never()).getByProductId(any());
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testAddStock_NegativeQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.addStock(1, -5));
        verify(inventoryDao, never()).getByProductId(any());
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testAddStock_InventoryNotFound() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.addStock(1, 5));
        verify(inventoryDao).getByProductId(1);
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testAddStock_ProductNotFound() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.addStock(1, 5));
        verify(inventoryDao).getByProductId(1);
        verify(productApi).get(1);
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testAddStock_ClientNotFound() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.addStock(1, 5));
        verify(inventoryDao).getByProductId(1);
        verify(productApi).get(1);
        verify(clientApi).get(1);
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testAddStock_InactiveClient() {
        // Arrange
        testClient.setStatus(false);
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.addStock(1, 5));
        verify(inventoryDao).getByProductId(1);
        verify(productApi).get(1);
        verify(clientApi).get(1);
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testAddStock_ProductWithoutClient() {
        // Arrange
        testProduct.setClientId(null);
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        doNothing().when(inventoryDao).update(anyInt(), any(InventoryPojo.class));

        // Act
        inventoryApi.addStock(1, 5);

        // Assert
        verify(inventoryDao).getByProductId(1);
        verify(productApi).get(1);
        verify(clientApi, never()).get(any());
        verify(inventoryDao).update(eq(1), any(InventoryPojo.class));
    }

    @Test
    void testRemoveStock_Success() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);
        doNothing().when(inventoryDao).update(anyInt(), any(InventoryPojo.class));

        // Act
        inventoryApi.removeStock(1, 3);

        // Assert
        verify(inventoryDao).getByProductId(1);
        verify(productApi).get(1);
        verify(clientApi).get(1);
        verify(inventoryDao).update(eq(1), any(InventoryPojo.class));
    }

    @Test
    void testRemoveStock_NullQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.removeStock(1, null));
        verify(inventoryDao, never()).getByProductId(any());
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testRemoveStock_ZeroQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.removeStock(1, 0));
        verify(inventoryDao, never()).getByProductId(any());
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testRemoveStock_NegativeQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.removeStock(1, -3));
        verify(inventoryDao, never()).getByProductId(any());
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testRemoveStock_InventoryNotFound() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.removeStock(1, 3));
        verify(inventoryDao).getByProductId(1);
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testRemoveStock_InsufficientStock() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.removeStock(1, 15));
        verify(inventoryDao).getByProductId(1);
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testRemoveStock_ProductNotFound() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.removeStock(1, 3));
        verify(inventoryDao).getByProductId(1);
        verify(productApi).get(1);
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testRemoveStock_ClientNotFound() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.removeStock(1, 3));
        verify(inventoryDao).getByProductId(1);
        verify(productApi).get(1);
        verify(clientApi).get(1);
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testRemoveStock_InactiveClient() {
        // Arrange
        testClient.setStatus(false);
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.removeStock(1, 3));
        verify(inventoryDao).getByProductId(1);
        verify(productApi).get(1);
        verify(clientApi).get(1);
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testRemoveStock_ProductWithoutClient() {
        // Arrange
        testProduct.setClientId(null);
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        doNothing().when(inventoryDao).update(anyInt(), any(InventoryPojo.class));

        // Act
        inventoryApi.removeStock(1, 3);

        // Assert
        verify(inventoryDao).getByProductId(1);
        verify(productApi).get(1);
        verify(clientApi, never()).get(any());
        verify(inventoryDao).update(eq(1), any(InventoryPojo.class));
    }

    @Test
    void testAdd_Success() {
        // Arrange
        doNothing().when(inventoryDao).insert(testInventory);

        // Act
        inventoryApi.add(testInventory);

        // Assert
        verify(inventoryDao).insert(testInventory);
    }

    @Test
    void testAdd_NullInventory() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.add(null));
        verify(inventoryDao, never()).insert(any());
    }

    @Test
    void testGet_Success() {
        // Arrange
        when(inventoryDao.select(1)).thenReturn(testInventory);

        // Act
        InventoryPojo result = inventoryApi.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(inventoryDao).select(1);
    }

    @Test
    void testGet_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.get(null));
        verify(inventoryDao, never()).select(any());
    }

    @Test
    void testGet_ZeroId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.get(0));
        verify(inventoryDao, never()).select(any());
    }

    @Test
    void testGet_NegativeId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.get(-1));
        verify(inventoryDao, never()).select(any());
    }

    @Test
    void testGet_NotFound() {
        // Arrange
        when(inventoryDao.select(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.get(1));
        verify(inventoryDao).select(1);
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        doNothing().when(inventoryDao).update(1, testInventory);

        // Act
        inventoryApi.update(1, testInventory);

        // Assert
        verify(inventoryDao).update(1, testInventory);
    }

    @Test
    void testUpdate_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.update(null, testInventory));
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testUpdate_NullInventory() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.update(1, null));
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testGetAll_Success() {
        // Arrange
        List<InventoryPojo> inventories = Arrays.asList(testInventory);
        when(inventoryDao.selectAll()).thenReturn(inventories);

        // Act
        List<InventoryPojo> result = inventoryApi.getAll();

        // Assert
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getProductId());
        verify(inventoryDao).selectAll();
    }
} 