package org.example.inventory.unit;

import org.example.dto.InventoryDto;
import org.example.pojo.InventoryPojo;
import org.example.pojo.ProductPojo;
import org.example.model.InventoryData;
import org.example.model.InventoryForm;
import org.example.exception.ApiException;
import org.example.flow.InventoryFlow;
import org.example.api.InventoryApi;
import org.example.api.ProductApi;
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
    private InventoryFlow inventoryFlow;

    @Mock
    private InventoryApi inventoryApi;

    @Mock
    private ProductApi productApi;

    @InjectMocks
    private InventoryDto inventoryDto;

    private InventoryPojo testPojo;
    private InventoryData testData;
    private InventoryForm testForm;
    private ProductPojo testProduct;

    @BeforeEach
    void setUp() throws Exception {
        testPojo = new InventoryPojo();
        testPojo.setId(1);
        testPojo.setProductBarcode("TEST123");
        testPojo.setProductName("Test Product");
        testPojo.setClientName("TestClient");
        testPojo.setProductMrp(100.0);
        testPojo.setQuantity(10);

        testData = new InventoryData();
        testData.setId(1);
        testData.setBarcode("TEST123");
        testData.setProductName("Test Product");
        testData.setMrp(100.0);
        testData.setQuantity(10);

        testForm = new InventoryForm();
        testForm.setBarcode("TEST123");
        testForm.setProductName("Test Product");
        testForm.setClientName("TestClient");
        testForm.setMrp(100.0);
        testForm.setQuantity(10);

        testProduct = new ProductPojo();
        testProduct.setId(1);
        testProduct.setBarcode("TEST123");
        testProduct.setName("Test Product");
        testProduct.setClientName("TestClient");
        testProduct.setMrp(100.0);

        // Manually inject the api field using reflection
        Field apiField = inventoryDto.getClass().getSuperclass().getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(inventoryDto, inventoryApi);

        // Manually inject the productApi field using reflection
        Field productApiField = inventoryDto.getClass().getDeclaredField("productApi");
        productApiField.setAccessible(true);
        productApiField.set(inventoryDto, productApi);
    }

    @Test
    void testAdd_Success() {
        when(productApi.getByBarcode("TEST123")).thenReturn(testProduct);
        doNothing().when(inventoryApi).add(any(InventoryPojo.class));
        InventoryData result = inventoryDto.add(testForm);
        assertNotNull(result);
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getProductName());
        verify(inventoryApi, times(1)).add(any(InventoryPojo.class));
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
        when(inventoryApi.get(1)).thenReturn(testPojo);

        // Act
        InventoryData result = inventoryDto.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(testPojo.getId(), result.getId());
        assertEquals(testPojo.getProductBarcode(), result.getBarcode());
        verify(inventoryApi, times(1)).get(1);
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
        pojo1.setProductBarcode("TEST123");

        InventoryPojo pojo2 = new InventoryPojo();
        pojo2.setId(2);
        pojo2.setProductBarcode("TEST456");

        List<InventoryPojo> pojos = Arrays.asList(pojo1, pojo2);
        when(inventoryApi.getAll()).thenReturn(pojos);

        // Act
        List<InventoryData> result = inventoryDto.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("TEST123", result.get(0).getBarcode());
        assertEquals("TEST456", result.get(1).getBarcode());
        verify(inventoryApi, times(1)).getAll();
    }

    @Test
    void testUpdate_Success() {
        // Given
        InventoryForm form = new InventoryForm();
        form.setBarcode("UPD123");
        form.setProductName("Updated Product");
        form.setClientName("test client"); // Use lowercase to match StringUtil.format behavior
        form.setQuantity(50);
        form.setMrp(100.0);

        InventoryPojo inventory = new InventoryPojo();
        inventory.setId(1);
        inventory.setProductBarcode("UPD123");
        inventory.setProductName("Updated Product");

        ProductPojo product = new ProductPojo();
        product.setId(1);
        product.setBarcode("UPD123");
        product.setName("Updated Product");
        product.setClientName("test client");

        when(inventoryApi.get(1)).thenReturn(inventory);
        when(productApi.getByBarcode("UPD123")).thenReturn(product);
        lenient().doNothing().when(inventoryApi).update(anyInt(), any(InventoryPojo.class));

        // When
        InventoryData result = inventoryDto.update(1, form);

        // Then
        assertNotNull(result);
        assertEquals("Updated Product", result.getProductName());
        verify(inventoryApi).update(eq(1), any(InventoryPojo.class));
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
    void testGetByProductBarcode_Success() {
        when(inventoryFlow.getByProductBarcode("TEST123")).thenReturn(testPojo);
        when(productApi.getByBarcode("TEST123")).thenReturn(testProduct);
        InventoryData result = inventoryDto.getByProductBarcode("TEST123");
        assertNotNull(result);
        assertEquals(testPojo.getId(), result.getId());
        assertEquals("TEST123", result.getBarcode());
        verify(inventoryFlow, times(1)).getByProductBarcode("TEST123");
    }

    @Test
    void testGetByProductBarcode_NullBarcode() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.getByProductBarcode(null));
        verify(inventoryApi, never()).getByProductBarcode(any());
    }

    @Test
    void testGetByProductName_Success() {
        when(inventoryFlow.getByProductName("Test Product")).thenReturn(testPojo);
        when(productApi.getByBarcode("TEST123")).thenReturn(testProduct);
        InventoryData result = inventoryDto.getByProductName("Test Product");
        assertNotNull(result);
        assertEquals(testPojo.getId(), result.getId());
        assertEquals("Test Product", result.getProductName());
        verify(inventoryFlow, times(1)).getByProductName("Test Product");
    }

    @Test
    void testGetByProductName_NullName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.getByProductName(null));
        verify(inventoryApi, never()).getByProductName(any());
    }
} 