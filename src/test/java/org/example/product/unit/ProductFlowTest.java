package org.example.product.unit;

import org.example.api.ProductApi;
import org.example.pojo.ProductPojo;
import org.example.exception.ApiException;
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
class ProductFlowTest {

    @Mock
    private ProductApi productApi;

    private org.example.flow.AbstractFlow<ProductPojo> productFlow;

    private ProductPojo testProduct;

    @BeforeEach
    void setUp() throws Exception {
        testProduct = new ProductPojo();
        testProduct.setId(1);
        testProduct.setName("Test Product");
        testProduct.setBarcode("123456789");
        testProduct.setMrp(100.0);
        testProduct.setImageUrl("test-image.jpg");
        testProduct.setClientId(1);

        // Create a concrete implementation of AbstractFlow for testing
        productFlow = new org.example.flow.AbstractFlow<ProductPojo>(ProductPojo.class) {
            @Override
            protected String getEntityName() {
                return "Product";
            }
        };
        
        // Inject the mocked API
        java.lang.reflect.Field apiField = org.example.flow.AbstractFlow.class.getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(productFlow, productApi);
    }

    @Test
    void testAdd_Success() {
        // Arrange
        doNothing().when(productApi).add(testProduct);

        // Act
        ProductPojo result = productFlow.add(testProduct);

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productApi).add(testProduct);
    }

    @Test
    void testAdd_NullProduct() {
        // Arrange - API should throw exception for null
        doThrow(new ApiException("Product cannot be null")).when(productApi).add(null);

        // Act & Assert - AbstractFlow.add calls API which throws exception
        assertThrows(ApiException.class, () -> productFlow.add(null));
        verify(productApi).add(null);
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        doNothing().when(productApi).update(1, testProduct);

        // Act
        productFlow.update(1, testProduct);

        // Assert
        verify(productApi).update(1, testProduct);
    }

    @Test
    void testUpdate_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productFlow.update(null, testProduct));
        verify(productApi, never()).update(any(), any());
    }

    @Test
    void testUpdate_NullProduct() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productFlow.update(1, null));
        verify(productApi, never()).update(any(), any());
    }

    @Test
    void testGetAll_Success() {
        // Arrange
        List<ProductPojo> products = Arrays.asList(testProduct);
        when(productApi.getAll()).thenReturn(products);

        // Act
        List<ProductPojo> result = productFlow.getAll();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        verify(productApi).getAll();
    }

    @Test
    void testGetByField_Success() {
        // Arrange
        when(productApi.getByField("name", "Test Product")).thenReturn(testProduct);

        // Act
        ProductPojo result = productFlow.getByField("name", "Test Product");

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productApi).getByField("name", "Test Product");
    }

    @Test
    void testGetByField_NullFieldName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productFlow.getByField(null, "Test Product"));
        verify(productApi, never()).getByField(any(), any());
    }

    @Test
    void testGetByField_NullValue() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productFlow.getByField("name", null));
        verify(productApi, never()).getByField(any(), any());
    }

    @Test
    void testFindByField_Success() {
        // Arrange
        when(productApi.findByField("name", "Test Product")).thenReturn(testProduct);

        // Act
        ProductPojo result = productFlow.findByField("name", "Test Product");

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productApi).findByField("name", "Test Product");
    }

    @Test
    void testFindByField_NotFound() {
        // Arrange
        when(productApi.findByField("name", "Nonexistent Product")).thenReturn(null);

        // Act
        ProductPojo result = productFlow.findByField("name", "Nonexistent Product");

        // Assert
        assertNull(result);
        verify(productApi).findByField("name", "Nonexistent Product");
    }

    @Test
    void testGetByFields_Success() {
        // Arrange
        List<ProductPojo> products = Arrays.asList(testProduct);
        when(productApi.getByFields(new String[]{"name", "clientId"}, new Object[]{"Test Product", 1})).thenReturn(products);

        // Act
        List<ProductPojo> result = productFlow.getByFields(new String[]{"name", "clientId"}, new Object[]{"Test Product", 1});

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        verify(productApi).getByFields(new String[]{"name", "clientId"}, new Object[]{"Test Product", 1});
    }

    @Test
    void testGetByFields_NullFieldNames() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productFlow.getByFields(null, new Object[]{"Test Product", 1}));
        verify(productApi, never()).getByFields(any(), any());
    }

    @Test
    void testGetByFields_NullValues() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productFlow.getByFields(new String[]{"name", "clientId"}, null));
        verify(productApi, never()).getByFields(any(), any());
    }

    @Test
    void testGetByFields_MismatchedArrays() {
        // Act & Assert - AbstractFlow.getByFields doesn't validate array length mismatch
        // It only validates that arrays are not null, the validation happens in DTO layer
        List<ProductPojo> result = productFlow.getByFields(new String[]{"name"}, new Object[]{"Test Product", 1});
        assertNotNull(result);
        verify(productApi).getByFields(new String[]{"name"}, new Object[]{"Test Product", 1});
    }
} 