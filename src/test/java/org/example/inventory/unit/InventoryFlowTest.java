package org.example.inventory.unit;

import org.example.api.InventoryApi;
import org.example.flow.AbstractFlow;
import org.example.pojo.InventoryPojo;
import org.example.exception.ApiException;
import org.example.model.form.PaginationRequest;
import org.example.model.form.PaginationQuery;
import org.example.model.data.PaginationResponse;
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
class InventoryFlowTest {

    @Mock
    private InventoryApi inventoryApi;

    @InjectMocks
    private TestInventoryFlow inventoryFlow;

    private InventoryPojo testInventory;

    @BeforeEach
    void setUp() {
        testInventory = new InventoryPojo();
        testInventory.setId(1);
        testInventory.setProductId(1);
        testInventory.setQuantity(10);

        // Inject the api field into AbstractFlow
        try {
            java.lang.reflect.Field apiField = AbstractFlow.class.getDeclaredField("api");
            apiField.setAccessible(true);
            apiField.set(inventoryFlow, inventoryApi);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock API", e);
        }
    }

    @Test
    void testAdd_Success() {
        // Act
        inventoryFlow.add(testInventory);

        // Assert
        verify(inventoryApi).add(testInventory);
    }

    @Test
    void testAdd_NullEntity() {
        // Arrange - mock the API to throw exception for null entity
        doThrow(new ApiException("InventoryPojo cannot be null")).when(inventoryApi).add(null);
        
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryFlow.add(null));
        verify(inventoryApi).add(null);
    }

    @Test
    void testUpdate_Success() {
        // Act
        inventoryFlow.update(1, testInventory);

        // Assert
        verify(inventoryApi).update(1, testInventory);
    }

    @Test
    void testUpdate_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryFlow.update(null, testInventory));
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testUpdate_NullEntity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryFlow.update(1, null));
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testGetAll_Success() {
        // Arrange
        List<InventoryPojo> inventories = Arrays.asList(testInventory);
        when(inventoryApi.getAll()).thenReturn(inventories);

        // Act
        List<InventoryPojo> result = inventoryFlow.getAll();

        // Assert
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        verify(inventoryApi).getAll();
    }

    @Test
    void testGetByField_Success() {
        // Arrange
        when(inventoryApi.getByField("productId", 1)).thenReturn(testInventory);

        // Act
        InventoryPojo result = inventoryFlow.getByField("productId", 1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        verify(inventoryApi).getByField("productId", 1);
    }

    @Test
    void testGetByField_NullFieldName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryFlow.getByField(null, 1));
        verify(inventoryApi, never()).getByField(any(), any());
    }

    @Test
    void testGetByField_NullValue() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryFlow.getByField("productId", null));
        verify(inventoryApi, never()).getByField(any(), any());
    }

    @Test
    void testFindByField_Success() {
        // Arrange
        when(inventoryApi.findByField("productId", 1)).thenReturn(testInventory);

        // Act
        InventoryPojo result = inventoryFlow.findByField("productId", 1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        verify(inventoryApi).findByField("productId", 1);
    }

    @Test
    void testFindByField_NotFound() {
        // Arrange
        when(inventoryApi.findByField("productId", 999)).thenReturn(null);

        // Act
        InventoryPojo result = inventoryFlow.findByField("productId", 999);

        // Assert
        assertNull(result);
        verify(inventoryApi).findByField("productId", 999);
    }

    @Test
    void testGetByFields_Success() {
        // Arrange
        List<InventoryPojo> inventories = Arrays.asList(testInventory);
        when(inventoryApi.getByFields(new String[]{"productId", "quantity"}, new Object[]{1, 10}))
            .thenReturn(inventories);

        // Act
        List<InventoryPojo> result = inventoryFlow.getByFields(
            new String[]{"productId", "quantity"}, 
            new Object[]{1, 10}
        );

        // Assert
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getProductId());
        verify(inventoryApi).getByFields(new String[]{"productId", "quantity"}, new Object[]{1, 10});
    }

    @Test
    void testGetByFields_NullFieldNames() {
        // Act & Assert
        assertThrows(ApiException.class, () -> {
            inventoryFlow.getByFields(null, new Object[]{1, 10});
        });
        verify(inventoryApi, never()).getByFields(any(), any());
    }

    @Test
    void testGetByFields_NullValues() {
        // Act & Assert
        assertThrows(ApiException.class, () -> {
            inventoryFlow.getByFields(new String[]{"productId", "quantity"}, null);
        });
        verify(inventoryApi, never()).getByFields(any(), any());
    }



    @Test
    void testGetPaginated_Success() {
        // Arrange
        PaginationQuery query = PaginationQuery.all(new PaginationRequest(0, 10));
        PaginationResponse<InventoryPojo> expectedResponse = new PaginationResponse<>();
        expectedResponse.setContent(Arrays.asList(testInventory));
        expectedResponse.setTotalElements(1);
        
        when(inventoryApi.getPaginated(query)).thenReturn(expectedResponse);

        // Act
        PaginationResponse<InventoryPojo> result = inventoryFlow.getPaginated(query);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(inventoryApi).getPaginated(query);
    }

    @Test
    void testGetPaginated_NullQuery() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryFlow.getPaginated(null));
        verify(inventoryApi, never()).getPaginated(any());
    }



    // Test implementation of AbstractFlow for inventory
    private static class TestInventoryFlow extends AbstractFlow<InventoryPojo> {
        
        public TestInventoryFlow() {
            super(InventoryPojo.class);
        }

        @Override
        protected Integer getEntityId(InventoryPojo entity) {
            return entity.getId();
        }

        @Override
        protected String getEntityName() {
            return "Inventory";
        }
    }
} 