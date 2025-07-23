package org.example.inventory.unit;

import org.example.api.InventoryApi;
import org.example.flow.InventoryFlow;
import org.example.pojo.InventoryPojo;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryFlowTest {

    @Mock
    private InventoryApi api;

    @InjectMocks
    private InventoryFlow inventoryFlow;

    private InventoryPojo testInventory;

    @BeforeEach
    void setUp() throws Exception {
        testInventory = new InventoryPojo();
        testInventory.setId(1);
        testInventory.setProductId(1); // Use productId instead of productName/barcode
        testInventory.setQuantity(10);

        // Inject AbstractFlow's api field
        Field abstractApiField = inventoryFlow.getClass().getSuperclass().getDeclaredField("api");
        abstractApiField.setAccessible(true);
        abstractApiField.set(inventoryFlow, api);
        
        // Inject InventoryFlow's own api field
        Field apiField = inventoryFlow.getClass().getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(inventoryFlow, api);
    }

    @Test
    void testAdd_Success() {
        // Arrange
        doNothing().when(api).add(any(InventoryPojo.class));

        // Act
        InventoryPojo result = inventoryFlow.add(testInventory);

        // Assert
        assertNotNull(result);
        assertEquals(testInventory.getId(), result.getId());
        verify(api).add(testInventory);
    }

    @Test
    void testAdd_NullInventory() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryFlow.add(null));
        verify(api, never()).add(any());
    }

    @Test
    void testUpdate_Success() {
        // Given
        InventoryPojo inventory = new InventoryPojo();
        inventory.setId(1);
        inventory.setProductId(1); // Use productId instead of productBarcode
        inventory.setQuantity(50);

        doNothing().when(api).update(1, inventory);

        // When
        inventoryFlow.update(1, inventory);

        // Then
        verify(api).update(1, inventory);
    }

    @Test
    void testUpdate_NullId() {
        // When & Then
        assertThrows(ApiException.class, () -> inventoryFlow.update(null, new InventoryPojo()));
        verify(api, never()).update(any(), any());
    }

    @Test
    void testUpdate_NullEntity() {
        // When & Then
        assertThrows(ApiException.class, () -> inventoryFlow.update(1, null));
        verify(api, never()).update(any(), any());
    }

    @Test
    void testGetAll_Success() {
        // Given
        List<InventoryPojo> inventory = Arrays.asList(
            new InventoryPojo(), new InventoryPojo()
        );
        when(api.getAll()).thenReturn(inventory);

        // When
        List<InventoryPojo> result = inventoryFlow.getAll();

        // Then
        assertEquals(2, result.size());
        verify(api).getAll();
    }

    @Test
    void testGetByProductId_Success() {
        // Arrange
        when(api.getByProductId(1)).thenReturn(testInventory);

        // Act
        InventoryPojo result = inventoryFlow.getByProductId(1);

        // Assert
        assertNotNull(result);
        assertEquals(testInventory.getId(), result.getId());
        verify(api).getByProductId(1);
    }

    @Test
    void testGetByProductId_NotFound() {
        // Arrange
        when(api.getByProductId(999)).thenReturn(null);

        // Act
        InventoryPojo result = inventoryFlow.getByProductId(999);

        // Assert
        assertNull(result);
        verify(api).getByProductId(999);
    }

    @Test
    void testAddStock_Success() {
        // Arrange
        doNothing().when(api).addStock(1, 5);

        // Act
        inventoryFlow.addStock(1, 5);

        // Assert
        verify(api).addStock(1, 5);
    }

    @Test
    void testRemoveStock_Success() {
        // Arrange
        doNothing().when(api).removeStock(1, 3);

        // Act
        inventoryFlow.removeStock(1, 3);

        // Assert
        verify(api).removeStock(1, 3);
    }

    @Test
    void testSetStock_Success() {
        // Arrange
        doNothing().when(api).setStock(1, 20);

        // Act
        inventoryFlow.setStock(1, 20);

        // Assert
        verify(api).setStock(1, 20);
    }
} 