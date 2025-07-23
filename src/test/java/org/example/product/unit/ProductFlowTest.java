package org.example.product.unit;

import org.example.api.ProductApi;
import org.example.api.InventoryApi;
import org.example.flow.ProductFlow;
import org.example.pojo.ProductPojo;
import org.example.pojo.InventoryPojo;
import org.example.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductFlowTest {

    @Mock
    private ProductApi api;

    @Mock
    private InventoryApi inventoryApi;

    @InjectMocks
    private ProductFlow productFlow;

    private ProductPojo testProduct;
    private InventoryPojo testInventory;

    @BeforeEach
    void setUp() throws Exception {
        testProduct = new ProductPojo();
        testProduct.setId(1);
        testProduct.setName("Test Product");
        testProduct.setBarcode("TEST123");
        testProduct.setMrp(100.0);
        testProduct.setClientId(1);
        testProduct.setImageUrl("test-image.jpg");

        testInventory = new InventoryPojo();
        testInventory.setId(1);
        testInventory.setProductId(1); // Use productId instead of productBarcode
        testInventory.setQuantity(10);

        // Inject AbstractFlow's api field
        Field abstractApiField = productFlow.getClass().getSuperclass().getDeclaredField("api");
        abstractApiField.setAccessible(true);
        abstractApiField.set(productFlow, api);
        
        // Inject ProductFlow's own api field
        Field apiField = productFlow.getClass().getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(productFlow, api);
    }

    @Test
    void testAdd_Success() {
        // Arrange
        doNothing().when(api).add(any(ProductPojo.class));

        // Act
        ProductPojo result = productFlow.add(testProduct);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        verify(api).add(testProduct);
    }

    @Test
    void testAdd_NullProduct() {
        // When & Then
        assertThrows(ApiException.class, () -> productFlow.add(null));
        verify(api, never()).add(any());
    }

    @Test
    void testUpdate_Success() {
        // Given
        ProductPojo existingProduct = new ProductPojo();
        existingProduct.setId(1);
        existingProduct.setName("Old Product");
        existingProduct.setBarcode("OLD123");
        existingProduct.setMrp(100.0);
        existingProduct.setClientId(1);
        existingProduct.setImageUrl("old-image.jpg");

        ProductPojo updatedProduct = new ProductPojo();
        updatedProduct.setId(1);
        updatedProduct.setName("New Product");
        updatedProduct.setBarcode("NEW123");
        updatedProduct.setMrp(150.0);
        updatedProduct.setClientId(1);
        updatedProduct.setImageUrl("new-image.jpg");

        when(api.get(1)).thenReturn(existingProduct);
        doNothing().when(api).update(1, updatedProduct);

        // When
        productFlow.update(1, updatedProduct);

        // Then
        verify(api).get(1);
        verify(api).update(1, updatedProduct);
        // Note: Inventory no longer needs to be updated when product details change
        // because inventory now only stores productId and fetches product details dynamically
    }

    @Test
    void testUpdate_NullId() {
        // When & Then
        assertThrows(ApiException.class, () -> productFlow.update(null, new ProductPojo()));
        verify(api, never()).update(any(), any());
    }

    @Test
    void testUpdate_NullEntity() {
        // When & Then
        assertThrows(ApiException.class, () -> productFlow.update(1, null));
        verify(api, never()).update(any(), any());
    }

    @Test
    void testUpdate_NotFound() {
        // Given
        when(api.get(999)).thenReturn(null);

        // When & Then
        assertThrows(ApiException.class, () -> productFlow.update(999, new ProductPojo()));
        verify(api).get(999);
        verify(api, never()).update(any(), any());
    }

    @Test
    void testGetAll_Success() {
        // Given
        when(api.getAll()).thenReturn(java.util.Arrays.asList(testProduct));

        // When
        java.util.List<ProductPojo> result = productFlow.getAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProduct, result.get(0));
        verify(api).getAll();
    }

    @Test
    void testGet_Success() {
        // Given
        when(api.get(1)).thenReturn(testProduct);

        // When
        ProductPojo result = productFlow.get(1);

        // Then
        assertNotNull(result);
        assertEquals(testProduct, result);
        verify(api).get(1);
    }

    @Test
    void testGet_NullId() {
        // When & Then
        assertThrows(ApiException.class, () -> productFlow.get(null));
        verify(api, never()).get(any());
    }

    @Test
    void testGetByBarcode_Success() {
        // Given
        when(api.getByBarcode("TEST123")).thenReturn(testProduct);

        // When
        ProductPojo result = productFlow.getByBarcode("TEST123");

        // Then
        assertNotNull(result);
        assertEquals(testProduct, result);
        verify(api).getByBarcode("TEST123");
    }

    @Test
    void testGetByBarcode_NullBarcode() {
        // When & Then
        assertThrows(ApiException.class, () -> productFlow.getByBarcode(null));
        verify(api, never()).getByBarcode(any());
    }

    @Test
    void testGetByBarcode_EmptyBarcode() {
        // When & Then
        assertThrows(ApiException.class, () -> productFlow.getByBarcode(""));
        verify(api, never()).getByBarcode(any());
    }

    @Test
    void testGetByName_Success() {
        // Given
        when(api.getByName("Test Product")).thenReturn(testProduct);

        // When
        ProductPojo result = productFlow.getByName("Test Product");

        // Then
        assertNotNull(result);
        assertEquals(testProduct, result);
        verify(api).getByName("Test Product");
    }

    @Test
    void testGetByName_NullName() {
        // When & Then
        assertThrows(ApiException.class, () -> productFlow.getByName(null));
        verify(api, never()).getByName(any());
    }

    @Test
    void testGetByName_EmptyName() {
        // When & Then
        assertThrows(ApiException.class, () -> productFlow.getByName(""));
        verify(api, never()).getByName(any());
    }
} 