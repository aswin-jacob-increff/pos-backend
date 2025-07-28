package org.example.inventory.unit;

import org.example.dto.InventoryDto;
import org.example.pojo.InventoryPojo;
import org.example.pojo.ProductPojo;
import org.example.model.data.InventoryData;
import org.example.model.form.InventoryForm;
import org.example.exception.ApiException;
import org.example.api.InventoryApi;
import org.example.api.ProductApi;
import org.example.api.ClientApi;
import org.example.pojo.ClientPojo;
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
class InventoryDtoTest {

    @Mock
    private InventoryApi inventoryApi;

    @Mock
    private ProductApi productApi;

    @Mock
    private ClientApi clientApi;

    @InjectMocks
    private InventoryDto inventoryDto;

    private InventoryPojo testInventory;
    private InventoryData testData;
    private InventoryForm testForm;
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

        testForm = new InventoryForm();
        testForm.setProductId(1);
        testForm.setQuantity(10);

        // Manually inject dependencies using reflection
        Field productApiField = inventoryDto.getClass().getDeclaredField("productApi");
        productApiField.setAccessible(true);
        productApiField.set(inventoryDto, productApi);

        // Inject the api field from AbstractDto
        Field abstractApiField = inventoryDto.getClass().getSuperclass().getDeclaredField("api");
        abstractApiField.setAccessible(true);
        abstractApiField.set(inventoryDto, inventoryApi);
    }

    @Test
    void testAdd_Success() {
        // Arrange
        doNothing().when(inventoryApi).add(any(InventoryPojo.class));
        when(productApi.get(1)).thenReturn(testProduct);

        // Act
        InventoryData result = inventoryDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getProductName());
        assertEquals(10, result.getQuantity());
        verify(inventoryApi).add(any(InventoryPojo.class));
    }

    @Test
    void testAdd_NullForm() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.add(null));
        verify(inventoryApi, never()).add(any());
    }

    @Test
    void testGet_Success() {
        // Arrange
        when(inventoryApi.get(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);

        // Act
        InventoryData result = inventoryDto.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(testInventory.getId(), result.getId());
        assertEquals(1, result.getProductId());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getProductName());
        verify(inventoryApi, times(1)).get(1);
        verify(productApi, times(1)).get(1);
    }

    @Test
    void testGet_InvalidId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.get(0));
        assertThrows(ApiException.class, () -> inventoryDto.get(-1));
        assertThrows(ApiException.class, () -> inventoryDto.get(null));
        
        verify(inventoryApi, never()).get(anyInt());
    }

    @Test
    void testGetAll_Success() {
        // Arrange
        InventoryPojo pojo1 = new InventoryPojo();
        pojo1.setId(1);
        pojo1.setProductId(1);

        InventoryPojo pojo2 = new InventoryPojo();
        pojo2.setId(2);
        pojo2.setProductId(2);

        List<InventoryPojo> pojos = Arrays.asList(pojo1, pojo2);
        when(inventoryApi.getAll()).thenReturn(pojos);
        when(productApi.get(1)).thenReturn(testProduct);
        when(productApi.get(2)).thenReturn(testProduct);

        // Act
        List<InventoryData> result = inventoryDto.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getProductId());
        assertEquals(2, result.get(1).getProductId());
        verify(inventoryApi, times(1)).getAll();
        verify(productApi, times(2)).get(anyInt());
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        doNothing().when(inventoryApi).update(eq(1), any(InventoryPojo.class));
        when(inventoryApi.get(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);

        // Act
        InventoryData result = inventoryDto.update(1, testForm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getProductName());
        assertEquals(10, result.getQuantity());
        verify(inventoryApi).update(eq(1), any(InventoryPojo.class));
        verify(inventoryApi).get(1);
    }

    @Test
    void testUpdate_NullId() {
        // When & Then
        assertThrows(ApiException.class, () -> inventoryDto.update(null, new InventoryForm()));
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testUpdate_NullForm() {
        // When & Then
        assertThrows(ApiException.class, () -> inventoryDto.update(1, null));
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testGetByProductId_Success() {
        when(inventoryApi.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        
        InventoryData result = inventoryDto.getByProductId(1);
        assertNotNull(result);
        assertEquals(testInventory.getId(), result.getId());
        assertEquals(1, result.getProductId());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getProductName());
        verify(inventoryApi, times(1)).getByProductId(1);
        verify(productApi, times(1)).get(1);
    }

    @Test
    void testGetByProductId_NullProductId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.getByProductId(null));
        verify(inventoryApi, never()).getByProductId(any());
    }

    @Test
    void testAddStock_Success() {
        when(inventoryApi.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        doNothing().when(inventoryApi).addStock(1, 5);
        
        InventoryData result = inventoryDto.addStock(1, 5);
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getProductName());
        verify(inventoryApi, times(1)).addStock(1, 5);
    }

    @Test
    void testRemoveStock_Success() {
        when(inventoryApi.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        doNothing().when(inventoryApi).removeStock(1, 3);
        
        InventoryData result = inventoryDto.removeStock(1, 3);
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getProductName());
        verify(inventoryApi, times(1)).removeStock(1, 3);
    }

    @Test
    void testSetStock_Success() {
        when(inventoryApi.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        doNothing().when(inventoryApi).setStock(1, 20);
        
        InventoryData result = inventoryDto.setStock(1, 20);
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getProductName());
        verify(inventoryApi, times(1)).setStock(1, 20);
    }
} 