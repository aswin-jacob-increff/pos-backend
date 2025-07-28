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
        doNothing().when(inventoryDao).insert(any(InventoryPojo.class));

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
        assertEquals(testInventory.getId(), result.getId());
        assertEquals(testInventory.getProductId(), result.getProductId());
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
    void testUpdate_Success() {
        // Arrange
        InventoryPojo updatedInventory = new InventoryPojo();
        updatedInventory.setId(1);
        updatedInventory.setProductId(1);
        updatedInventory.setQuantity(20);

        doNothing().when(inventoryDao).update(any(Integer.class), any(InventoryPojo.class));

        // Act
        inventoryApi.update(1, updatedInventory);

        // Assert
        verify(inventoryDao).update(1, updatedInventory);
    }
} 