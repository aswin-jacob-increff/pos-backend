package org.example.inventory.unit;

import org.example.api.InventoryApi;
import org.example.api.ClientApi;
import org.example.api.ProductApi;
import org.example.dao.InventoryDao;
import org.example.pojo.InventoryPojo;
import org.example.pojo.ProductPojo;
import org.example.pojo.ClientPojo;
import org.example.exception.ApiException;
import org.example.model.form.PaginationRequest;
import org.example.model.data.PaginationResponse;
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

    @Mock
    private ProductApi productApi;

    @InjectMocks
    private InventoryApi inventoryApi;

    private InventoryPojo testInventory;
    private ProductPojo testProduct;
    private ClientPojo testClient;

    @BeforeEach
    void setUp() throws Exception {
        testInventory = new InventoryPojo();
        testInventory.setId(1);
        testInventory.setProductId(1);
        testInventory.setQuantity(10);

        testProduct = new ProductPojo();
        testProduct.setId(1);
        testProduct.setName("Test Product");
        testProduct.setBarcode("TEST123");
        testProduct.setMrp(100.0);
        testProduct.setClientId(1);

        testClient = new ClientPojo();
        testClient.setId(1);
        testClient.setClientName("Test Client");
        testClient.setStatus(true);

        // Manually inject dependencies using reflection
        Field clientApiField = inventoryApi.getClass().getDeclaredField("clientApi");
        clientApiField.setAccessible(true);
        clientApiField.set(inventoryApi, clientApi);

        Field productApiField = inventoryApi.getClass().getDeclaredField("productApi");
        productApiField.setAccessible(true);
        productApiField.set(inventoryApi, productApi);

        // Inject the dao field from AbstractApi
        Field abstractDaoField = inventoryApi.getClass().getSuperclass().getDeclaredField("dao");
        abstractDaoField.setAccessible(true);
        abstractDaoField.set(inventoryApi, inventoryDao);
    }

    @Test
    void testAdd_Success() {
        // Arrange
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);
        doNothing().when(inventoryDao).insert(any(InventoryPojo.class));

        // Act
        inventoryApi.add(testInventory);

        // Assert
        verify(productApi).get(1);
        verify(clientApi).get(1);
        verify(inventoryDao).insert(testInventory);
    }

    @Test
    void testAdd_NullInventory() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.add(null));
        verify(inventoryDao, never()).insert(any());
    }

    @Test
    void testAdd_NullProductId() {
        // Arrange
        testInventory.setProductId(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.add(testInventory));
        verify(inventoryDao, never()).insert(any());
    }

    @Test
    void testAdd_ProductNotFound() {
        // Arrange
        when(productApi.get(1)).thenThrow(new ApiException("Product with ID '1' not found"));

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.add(testInventory));
        verify(inventoryDao, never()).insert(any());
    }

    @Test
    void testAdd_ClientNotFound() {
        // Arrange
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenThrow(new ApiException("Client with ID '1' not found"));

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.add(testInventory));
        verify(inventoryDao, never()).insert(any());
    }

    @Test
    void testAdd_ClientInactive() {
        // Arrange
        testClient.setStatus(false);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.add(testInventory));
        verify(inventoryDao, never()).insert(any());
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        InventoryPojo updatedInventory = new InventoryPojo();
        updatedInventory.setId(1);
        updatedInventory.setProductId(1);
        updatedInventory.setQuantity(20);

        when(inventoryDao.select(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);
        doNothing().when(inventoryDao).update(any(Integer.class), any(InventoryPojo.class));

        // Act
        inventoryApi.update(1, updatedInventory);

        // Assert
        verify(inventoryDao).select(1);
        verify(productApi).get(1);
        verify(clientApi).get(1);
        verify(inventoryDao).update(1, updatedInventory);
    }

    @Test
    void testUpdate_NullInventory() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.update(1, null));
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testUpdate_NullProductId() {
        // Arrange
        InventoryPojo updatedInventory = new InventoryPojo();
        updatedInventory.setProductId(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.update(1, updatedInventory));
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testGetByProductId_Success() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);

        // Act
        InventoryPojo result = inventoryApi.getByProductId(1);

        // Assert
        assertNotNull(result);
        assertEquals(testInventory.getId(), result.getId());
        assertEquals(testInventory.getProductId(), result.getProductId());
        assertEquals(testInventory.getQuantity(), result.getQuantity());
        verify(inventoryDao).getByProductId(1);
    }

    @Test
    void testGet_Success() {
        // Arrange
        when(inventoryDao.select(1)).thenReturn(testInventory);

        // Act
        InventoryPojo result = inventoryApi.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(testInventory.getId(), result.getId());
        assertEquals(testInventory.getProductId(), result.getProductId());
        assertEquals(testInventory.getQuantity(), result.getQuantity());
        verify(inventoryDao).select(1);
    }

    @Test
    void testGet_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.get(null));
        verify(inventoryDao, never()).select(any());
    }

    @Test
    void testGet_NotFound() {
        // Arrange
        when(inventoryDao.select(999)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.get(999));
        verify(inventoryDao).select(999);
    }

    @Test
    void testAddStock_Success() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);
        doNothing().when(inventoryDao).update(any(Integer.class), any(InventoryPojo.class));

        // Act
        inventoryApi.addStock(1, 5);

        // Assert
        verify(inventoryDao).getByProductId(1);
        verify(productApi).get(1);
        verify(clientApi).get(1);
        verify(inventoryDao).update(eq(1), argThat(inventory -> 
            inventory.getProductId() == 1 && inventory.getQuantity() == 15
        ));
    }

    @Test
    void testAddStock_ZeroQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.addStock(1, 0));
        verify(inventoryDao, never()).getByProductId(any());
    }

    @Test
    void testAddStock_NegativeQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.addStock(1, -5));
        verify(inventoryDao, never()).getByProductId(any());
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
        when(productApi.get(1)).thenThrow(new ApiException("Product with ID '1' not found"));

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.addStock(1, 5));
        verify(inventoryDao).getByProductId(1);
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testAddStock_ClientNotFound() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenThrow(new ApiException("Client with ID '1' not found"));

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.addStock(1, 5));
        verify(inventoryDao).getByProductId(1);
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testAddStock_ClientInactive() {
        // Arrange
        testClient.setStatus(false);
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.addStock(1, 5));
        verify(inventoryDao).getByProductId(1);
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testRemoveStock_Success() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);
        doNothing().when(inventoryDao).update(any(Integer.class), any(InventoryPojo.class));

        // Act
        inventoryApi.removeStock(1, 3);

        // Assert
        verify(inventoryDao).getByProductId(1);
        verify(productApi).get(1);
        verify(clientApi).get(1);
        verify(inventoryDao).update(eq(1), argThat(inventory -> 
            inventory.getProductId() == 1 && inventory.getQuantity() == 7
        ));
    }

    @Test
    void testRemoveStock_ZeroQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.removeStock(1, 0));
        verify(inventoryDao, never()).getByProductId(any());
    }

    @Test
    void testRemoveStock_NegativeQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.removeStock(1, -3));
        verify(inventoryDao, never()).getByProductId(any());
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
        when(productApi.get(1)).thenThrow(new ApiException("Product with ID '1' not found"));

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.removeStock(1, 3));
        verify(inventoryDao).getByProductId(1);
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testRemoveStock_ClientNotFound() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenThrow(new ApiException("Client with ID '1' not found"));

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.removeStock(1, 3));
        verify(inventoryDao).getByProductId(1);
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testRemoveStock_ClientInactive() {
        // Arrange
        testClient.setStatus(false);
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.removeStock(1, 3));
        verify(inventoryDao).getByProductId(1);
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testSetStock_Success() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);
        doNothing().when(inventoryDao).update(any(Integer.class), any(InventoryPojo.class));

        // Act
        inventoryApi.setStock(1, 25);

        // Assert
        verify(inventoryDao).getByProductId(1);
        verify(productApi).get(1);
        verify(clientApi).get(1);
        verify(inventoryDao).update(eq(1), argThat(inventory -> 
            inventory.getProductId() == 1 && inventory.getQuantity() == 25
        ));
    }

    @Test
    void testSetStock_ZeroQuantity() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);
        doNothing().when(inventoryDao).update(any(Integer.class), any(InventoryPojo.class));

        // Act
        inventoryApi.setStock(1, 0);

        // Assert
        verify(inventoryDao).getByProductId(1);
        verify(productApi).get(1);
        verify(clientApi).get(1);
        verify(inventoryDao).update(eq(1), argThat(inventory -> 
            inventory.getProductId() == 1 && inventory.getQuantity() == 0
        ));
    }

    @Test
    void testSetStock_NegativeQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.setStock(1, -5));
        verify(inventoryDao, never()).getByProductId(any());
    }

    @Test
    void testSetStock_InventoryNotFound() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.setStock(1, 25));
        verify(inventoryDao).getByProductId(1);
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testSetStock_ProductNotFound() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenThrow(new ApiException("Product with ID '1' not found"));

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.setStock(1, 25));
        verify(inventoryDao).getByProductId(1);
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testSetStock_ClientNotFound() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenThrow(new ApiException("Client with ID '1' not found"));

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.setStock(1, 25));
        verify(inventoryDao).getByProductId(1);
        verify(inventoryDao, never()).update(any(), any());
    }

    @Test
    void testSetStock_ClientInactive() {
        // Arrange
        testClient.setStatus(false);
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryApi.setStock(1, 25));
        verify(inventoryDao).getByProductId(1);
        verify(inventoryDao, never()).update(any(), any());
    }
} 