package org.example.inventory.unit;

import org.example.dto.InventoryDto;
import org.example.pojo.InventoryPojo;
import org.example.pojo.ProductPojo;
import org.example.model.data.InventoryData;
import org.example.model.form.InventoryForm;
import org.example.exception.ApiException;
import org.example.flow.InventoryFlow;
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
    private InventoryFlow inventoryFlow;

    @Mock
    private InventoryApi inventoryApi;

    @Mock
    private ProductApi productApi;

    @Mock
    private ClientApi clientApi;

    @InjectMocks
    private InventoryDto inventoryDto;

    private InventoryPojo testPojo;
    private InventoryData testData;
    private InventoryForm testForm;
    private ProductPojo testProduct;
    private ClientPojo testClient;

    @BeforeEach
    void setUp() throws Exception {
        testPojo = new InventoryPojo();
        testPojo.setId(1);
        testPojo.setProductId(1);
        testPojo.setQuantity(10);

        testData = new InventoryData();
        testData.setId(1);
        testData.setProductId(1);
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
        testProduct.setClientId(1);
        testProduct.setMrp(100.0);
        testProduct.setImageUrl("http://example.com/image.jpg");

        testClient = new ClientPojo();
        testClient.setId(1);
        testClient.setClientName("TestClient");

        // Manually inject the api field using reflection
        Field apiField = inventoryDto.getClass().getSuperclass().getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(inventoryDto, inventoryApi);

        // Manually inject the productApi field using reflection
        Field productApiField = inventoryDto.getClass().getDeclaredField("productApi");
        productApiField.setAccessible(true);
        productApiField.set(inventoryDto, productApi);

        // Manually inject the clientApi field using reflection
        Field clientApiField = inventoryDto.getClass().getDeclaredField("clientApi");
        clientApiField.setAccessible(true);
        clientApiField.set(inventoryDto, clientApi);
    }

    @Test
    void testAdd_Success() {
        when(productApi.getByBarcode("TEST123")).thenReturn(testProduct);
        when(productApi.get(1)).thenReturn(testProduct);
        doAnswer(invocation -> {
            InventoryPojo inventory = invocation.getArgument(0);
            inventory.setId(1); // Set the ID as if it was inserted
            return null;
        }).when(inventoryApi).add(any(InventoryPojo.class));
        
        InventoryData result = inventoryDto.add(testForm);
        assertNotNull(result);
        assertEquals(1, result.getProductId());
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
        when(productApi.get(1)).thenReturn(testProduct);

        // Act
        InventoryData result = inventoryDto.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(testPojo.getId(), result.getId());
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
        // Given
        InventoryForm form = new InventoryForm();
        form.setBarcode("UPD123");
        form.setProductName("Updated Product");
        form.setClientName("test client"); // Use lowercase to match StringUtil.format behavior
        form.setQuantity(50);
        form.setMrp(100.0);

        InventoryPojo inventory = new InventoryPojo();
        inventory.setId(1);
        inventory.setProductId(1);

        ProductPojo product = new ProductPojo();
        product.setId(1);
        product.setBarcode("UPD123");
        product.setName("Updated Product");
        product.setClientId(1);

        when(inventoryApi.get(1)).thenReturn(inventory);
        when(productApi.getByBarcode("UPD123")).thenReturn(product);
        when(productApi.get(1)).thenReturn(product);
        lenient().doNothing().when(inventoryApi).update(anyInt(), any(InventoryPojo.class));

        // When
        InventoryData result = inventoryDto.update(1, form);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals("UPD123", result.getBarcode());
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
    void testGetByProductId_Success() {
        when(inventoryFlow.getByProductId(1)).thenReturn(testPojo);
        when(productApi.get(1)).thenReturn(testProduct);
        
        InventoryData result = inventoryDto.getByProductId(1);
        assertNotNull(result);
        assertEquals(testPojo.getId(), result.getId());
        assertEquals(1, result.getProductId());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getProductName());
        verify(inventoryFlow, times(1)).getByProductId(1);
        verify(productApi, times(1)).get(1);
    }

    @Test
    void testGetByProductId_NullProductId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.getByProductId(null));
        verify(inventoryFlow, never()).getByProductId(any());
    }

    @Test
    void testAddStock_Success() {
        when(inventoryFlow.getByProductId(1)).thenReturn(testPojo);
        when(productApi.get(1)).thenReturn(testProduct);
        doNothing().when(inventoryFlow).addStock(1, 5);
        
        InventoryData result = inventoryDto.addStock(1, 5);
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getProductName());
        verify(inventoryFlow, times(1)).addStock(1, 5);
    }

    @Test
    void testRemoveStock_Success() {
        when(inventoryFlow.getByProductId(1)).thenReturn(testPojo);
        when(productApi.get(1)).thenReturn(testProduct);
        doNothing().when(inventoryFlow).removeStock(1, 3);
        
        InventoryData result = inventoryDto.removeStock(1, 3);
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getProductName());
        verify(inventoryFlow, times(1)).removeStock(1, 3);
    }

    @Test
    void testSetStock_Success() {
        when(inventoryFlow.getByProductId(1)).thenReturn(testPojo);
        when(productApi.get(1)).thenReturn(testProduct);
        doNothing().when(inventoryFlow).setStock(1, 20);
        
        InventoryData result = inventoryDto.setStock(1, 20);
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getProductName());
        verify(inventoryFlow, times(1)).setStock(1, 20);
    }
} 