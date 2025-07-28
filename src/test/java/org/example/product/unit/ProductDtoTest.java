package org.example.product.unit;

import org.example.dto.ProductDto;
import org.example.model.data.ProductData;
import org.example.model.form.ProductForm;
import org.example.pojo.ProductPojo;
import org.example.pojo.ClientPojo;
import org.example.api.ClientApi;
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
    private org.example.api.ProductApi productApi;

    @Mock
    private ClientApi clientApi;

    @InjectMocks
    private ProductDto productDto;

    private ProductForm testForm;
    private ProductPojo testProduct;
    private ClientPojo testClient;

    @BeforeEach
    void setUp() throws Exception {
        testClient = new ClientPojo();
        testClient.setId(1);
        testClient.setClientName("testclient");
        testClient.setStatus(true);

        testForm = new ProductForm();
        testForm.setName("Test Product");
        testForm.setBarcode("TEST123");
        testForm.setClientName("TestClient");
        testForm.setMrp(100.0);

        testProduct = new ProductPojo();
        testProduct.setId(1);
        testProduct.setName("Test Product");
        testProduct.setBarcode("TEST123");
        testProduct.setClientId(1);
        testProduct.setMrp(100.0);

        // Inject the clientApi field
        Field clientApiField = productDto.getClass().getDeclaredField("clientApi");
        clientApiField.setAccessible(true);
        clientApiField.set(productDto, clientApi);

        // Inject the api field from AbstractDto
        Field apiField = productDto.getClass().getSuperclass().getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(productDto, productApi);
    }

    @Test
    void testAdd_Success() {
        // Arrange
        lenient().when(clientApi.getByName("testclient")).thenReturn(testClient);
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
        verify(productApi, never()).add(any());
    }

    @Test
    void testGet_Success() {
        // Arrange
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);

        // Act
        ProductData result = productDto.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        assertEquals(testProduct.getName(), result.getName());
        assertEquals(testClient.getId(), result.getClientId());
        verify(productApi).get(1);
        verify(clientApi).get(1);
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
        List<ProductPojo> products = Arrays.asList(testProduct);
        when(productApi.getAll()).thenReturn(products);
        when(clientApi.get(1)).thenReturn(testClient);

        // When
        List<ProductData> result = productDto.getAll();

        // Then
        assertEquals(1, result.size());
        assertEquals(testClient.getId(), result.get(0).getClientId());
        verify(productApi).getAll();
        verify(clientApi).get(1);
    }

    @Test
    void testUpdate_Success() {
        // Given
        ProductForm form = new ProductForm();
        form.setName("Updated Product");
        form.setBarcode("UPD123");
        form.setClientName("Test Client");
        form.setMrp(150.0);

        ProductPojo existingProduct = new ProductPojo();
        existingProduct.setId(1);
        existingProduct.setName("Original Product");
        existingProduct.setBarcode("ORIG123");
        existingProduct.setClientId(1);

        ProductPojo updatedProduct = new ProductPojo();
        updatedProduct.setId(1);
        updatedProduct.setName("Updated Product");
        updatedProduct.setBarcode("UPD123");
        updatedProduct.setClientId(1);

        when(productApi.get(1)).thenReturn(updatedProduct);
        lenient().when(clientApi.get(1)).thenReturn(testClient);
        lenient().when(clientApi.getByName("test client")).thenReturn(testClient);
        doNothing().when(productApi).update(eq(1), any(ProductPojo.class));

        // When
        ProductData result = productDto.update(1, form);

        // Then
        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
        assertEquals("UPD123", result.getBarcode());
        verify(productApi).update(eq(1), any(ProductPojo.class));
        verify(productApi).get(1);
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

    @Test
    void testGetByBarcode_Success() {
        // Given
        when(productApi.getByBarcode("TEST123")).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);

        // When
        ProductData result = productDto.getByBarcode("TEST123");

        // Then
        assertNotNull(result);
        assertEquals("TEST123", result.getBarcode());
        assertEquals(testClient.getId(), result.getClientId());
        verify(productApi).getByBarcode("TEST123");
        verify(clientApi).get(1);
    }

    @Test
    void testGetByBarcode_NullBarcode() {
        // When & Then
        assertThrows(ApiException.class, () -> productDto.getByBarcode(null));
        verify(productApi, never()).getByBarcode(any());
    }

    @Test
    void testGetByBarcode_EmptyBarcode() {
        // When & Then
        assertThrows(ApiException.class, () -> productDto.getByBarcode(""));
        verify(productApi, never()).getByBarcode(any());
    }

    @Test
    void testGetByClientName_Success() {
        // Given
        List<ProductPojo> products = Arrays.asList(testProduct);
        when(productApi.getByClientName("testclient")).thenReturn(products);
        when(clientApi.get(1)).thenReturn(testClient);

        // When
        List<ProductData> result = productDto.getByClientName("testclient");

        // Then
        assertEquals(1, result.size());
        assertEquals(testClient.getId(), result.get(0).getClientId());
        verify(productApi).getByClientName("testclient");
        verify(clientApi).get(1);
    }

    @Test
    void testGetByClientName_NullName() {
        // When & Then
        assertThrows(ApiException.class, () -> productDto.getByClientName(null));
        verify(productApi, never()).getAll();
    }

    @Test
    void testGetByClientName_EmptyName() {
        // When & Then
        assertThrows(ApiException.class, () -> productDto.getByClientName(""));
        verify(productApi, never()).getAll();
    }
} 