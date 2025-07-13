package org.example.product.unit;

import org.example.dto.ProductDto;
import org.example.flow.ProductFlow;
import org.example.model.ProductData;
import org.example.model.ProductForm;
import org.example.pojo.ProductPojo;
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
class ProductDtoTest {

    @Mock
    private ProductFlow productFlow;

    @Mock
    private org.example.api.ProductApi productApi;

    @InjectMocks
    private ProductDto productDto;

    private ProductForm testForm;
    private ProductPojo testProduct;

    @BeforeEach
    void setUp() throws Exception {
        testForm = new ProductForm();
        testForm.setName("Test Product");
        testForm.setBarcode("TEST123");
        testForm.setClientName("TestClient");
        testForm.setMrp(100.0);

        testProduct = new ProductPojo();
        testProduct.setId(1);
        testProduct.setName("Test Product");
        testProduct.setBarcode("TEST123");
        testProduct.setClientName("TestClient");
        testProduct.setMrp(100.0);

        // Inject the productFlow field
        Field productFlowField = productDto.getClass().getDeclaredField("productFlow");
        productFlowField.setAccessible(true);
        productFlowField.set(productDto, productFlow);

        // Inject the api field from AbstractDto
        Field apiField = productDto.getClass().getSuperclass().getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(productDto, productApi);
    }

    @Test
    void testAdd_Success() {
        // Arrange
        doNothing().when(productApi).add(any(ProductPojo.class));

        // Act
        ProductData result = productDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getName(), result.getName());
        assertEquals(testProduct.getBarcode(), result.getBarcode());
        verify(productApi).add(any(ProductPojo.class));
    }

    @Test
    void testAdd_NullForm() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productDto.add(null));
        verify(productFlow, never()).add(any());
    }

    @Test
    void testGet_Success() {
        // Arrange
        when(productApi.get(1)).thenReturn(testProduct);

        // Act
        ProductData result = productDto.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        assertEquals(testProduct.getName(), result.getName());
        verify(productApi).get(1);
    }

    @Test
    void testGet_NullId() {
        // When & Then
        assertThrows(ApiException.class, () -> productDto.get(null));
        verify(productApi, never()).get(any());
    }

    @Test
    void testGetAll_Success() {
        // Given
        List<ProductPojo> products = Arrays.asList(
            new ProductPojo(), new ProductPojo()
        );
        when(productFlow.getAll()).thenReturn(products);

        // When
        List<ProductData> result = productDto.getAll();

        // Then
        assertEquals(2, result.size());
        verify(productFlow).getAll();
    }

    @Test
    void testUpdate_Success() {
        // Given
        ProductForm form = new ProductForm();
        form.setName("Updated Product");
        form.setBarcode("UPD123");
        form.setClientName("Test Client");

        ProductPojo product = new ProductPojo();
        product.setId(1);
        product.setName("Updated Product");
        product.setBarcode("UPD123");

        when(productApi.get(1)).thenReturn(product);
        lenient().doNothing().when(productApi).update(anyInt(), any(ProductPojo.class));

        // When
        ProductData result = productDto.update(1, form);

        // Then
        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
        verify(productApi).update(eq(1), any(ProductPojo.class));
    }

    @Test
    void testUpdate_NullId() {
        // When & Then
        assertThrows(ApiException.class, () -> productDto.update(null, new ProductForm()));
        verify(productApi, never()).update(any(), any());
    }

    @Test
    void testUpdate_NullForm() {
        // When & Then
        assertThrows(ApiException.class, () -> productDto.update(1, null));
        verify(productApi, never()).update(any(), any());
    }
} 