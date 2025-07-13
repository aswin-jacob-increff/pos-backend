package org.example.product.unit;

import org.example.flow.ProductFlow;
import org.example.api.ProductApi;
import org.example.api.InventoryApi;
import org.example.api.OrderItemApi;
import org.example.pojo.ProductPojo;
import org.example.pojo.InventoryPojo;
import org.example.pojo.OrderItemPojo;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductFlowTest {

    @Mock
    private ProductApi api;

    @Mock
    private InventoryApi inventoryApi;

    @Mock
    private OrderItemApi orderItemApi;

    @InjectMocks
    private ProductFlow productFlow;

    private ProductPojo testProduct;
    private InventoryPojo testInventory;
    private OrderItemPojo testOrderItem;

    @BeforeEach
    void setUp() throws Exception {
        testProduct = new ProductPojo();
        testProduct.setId(1);
        testProduct.setName("Test Product");
        testProduct.setBarcode("TEST123");
        testProduct.setClientName("TestClient");
        testProduct.setMrp(100.0);

        testInventory = new InventoryPojo();
        testInventory.setId(1);
        testInventory.setProductBarcode("TEST123");
        testInventory.setQuantity(10);

        testOrderItem = new OrderItemPojo();
        testOrderItem.setId(1);
        testOrderItem.setProductBarcode("TEST123");

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
        existingProduct.setClientName("Old Client");
        existingProduct.setImageUrl("old-image.jpg");

        ProductPojo updatedProduct = new ProductPojo();
        updatedProduct.setId(1);
        updatedProduct.setName("New Product");
        updatedProduct.setBarcode("NEW123");
        updatedProduct.setMrp(150.0);
        updatedProduct.setClientName("New Client");
        updatedProduct.setImageUrl("new-image.jpg");

        InventoryPojo inventory = new InventoryPojo();
        inventory.setId(1);
        inventory.setProductBarcode("OLD123");
        inventory.setProductName("Old Product");
        inventory.setProductMrp(100.0);
        inventory.setClientName("Old Client");
        inventory.setProductImageUrl("old-image.jpg");

        when(api.get(1)).thenReturn(existingProduct);
        doNothing().when(api).update(1, updatedProduct);
        when(inventoryApi.getByProductBarcode("OLD123")).thenReturn(inventory);
        doNothing().when(inventoryApi).update(1, inventory);

        // When
        productFlow.update(1, updatedProduct);

        // Then
        verify(api).get(1);
        verify(api).update(1, updatedProduct);
        verify(inventoryApi).getByProductBarcode("OLD123");
        verify(inventoryApi).update(1, inventory);
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
        verify(api, never()).update(any(), any());
    }

    @Test
    void testGetAll_Success() {
        // Given
        List<ProductPojo> products = Arrays.asList(
            new ProductPojo(), new ProductPojo()
        );
        when(api.getAll()).thenReturn(products);

        // When
        List<ProductPojo> result = productFlow.getAll();

        // Then
        assertEquals(2, result.size());
        verify(api).getAll();
    }

    @Test
    void testCanDeleteProduct_Success() {
        // Given
        when(api.get(1)).thenReturn(testProduct);
        when(orderItemApi.getByProductBarcode("TEST123")).thenReturn(Arrays.asList());

        // When
        boolean result = productFlow.canDeleteProduct(1);

        // Then
        assertTrue(result);
        verify(api).get(1);
        verify(orderItemApi).getByProductBarcode("TEST123");
    }

    @Test
    void testCanDeleteProduct_NullId() {
        // When & Then
        assertThrows(ApiException.class, () -> productFlow.canDeleteProduct(null));
    }

    @Test
    void testCanDeleteProduct_NotFound() {
        // Given
        when(api.get(999)).thenReturn(null);

        // When
        boolean result = productFlow.canDeleteProduct(999);

        // Then
        assertTrue(result); // Product doesn't exist, so it can be "deleted"
        verify(api).get(999);
    }

    @Test
    void testCanDeleteProduct_UsedInOrders() {
        // Given
        when(api.get(1)).thenReturn(testProduct);
        when(orderItemApi.getByProductBarcode("TEST123")).thenReturn(Arrays.asList(testOrderItem));

        // When
        boolean result = productFlow.canDeleteProduct(1);

        // Then
        assertFalse(result);
        verify(api).get(1);
        verify(orderItemApi).getByProductBarcode("TEST123");
    }

    @Test
    void testGetOrderItemCountForProduct_Success() {
        // Given
        when(api.get(1)).thenReturn(testProduct);
        List<OrderItemPojo> orderItems = Arrays.asList(testOrderItem, testOrderItem);
        when(orderItemApi.getByProductBarcode("TEST123")).thenReturn(orderItems);

        // When
        int result = productFlow.getOrderItemCountForProduct(1);

        // Then
        assertEquals(2, result);
        verify(api).get(1);
        verify(orderItemApi).getByProductBarcode("TEST123");
    }

    @Test
    void testGetOrderItemCountForProduct_NullId() {
        // When & Then
        assertThrows(ApiException.class, () -> productFlow.getOrderItemCountForProduct(null));
        verify(api, never()).get(any());
    }

    @Test
    void testGetOrderItemCountForProduct_NotFound() {
        // Given
        when(api.get(999)).thenReturn(null);

        // When
        int result = productFlow.getOrderItemCountForProduct(999);

        // Then
        assertEquals(0, result);
        verify(api).get(999);
        verify(orderItemApi, never()).getByProductBarcode(any());
    }
} 