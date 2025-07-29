package org.example.inventory.integration;

import org.example.dto.InventoryDto;
import org.example.api.InventoryApi;
import org.example.api.ProductApi;
import org.example.model.data.InventoryData;
import org.example.model.form.InventoryForm;
import org.example.model.data.TsvUploadResult;
import org.example.pojo.InventoryPojo;
import org.example.pojo.ProductPojo;
import org.example.exception.ApiException;
import org.example.util.InventoryTsvParser;
import org.example.util.FileValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryIntegrationTest {

    @Mock
    private InventoryApi inventoryApi;

    @Mock
    private ProductApi productApi;

    @InjectMocks
    private InventoryDto inventoryDto;

    private InventoryForm testForm;
    private InventoryPojo testInventory;
    private InventoryData testInventoryData;
    private ProductPojo testProduct;

    @BeforeEach
    void setUp() throws Exception {
        testProduct = new ProductPojo();
        testProduct.setId(1);
        testProduct.setName("Test Product");
        testProduct.setBarcode("123456789");
        testProduct.setMrp(100.0);

        testForm = new InventoryForm();
        testForm.setProductId(1);
        testForm.setQuantity(25);

        testInventory = new InventoryPojo();
        testInventory.setId(1);
        testInventory.setProductId(1);
        testInventory.setQuantity(25);

        testInventoryData = new InventoryData();
        testInventoryData.setId(1);
        testInventoryData.setProductId(1);
        testInventoryData.setQuantity(25);
        testInventoryData.setProductName("Test Product");
        testInventoryData.setBarcode("123456789");
        testInventoryData.setMrp(100.0);

        // Inject the api field from AbstractDto
        Field apiField = inventoryDto.getClass().getSuperclass().getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(inventoryDto, inventoryApi);
    }

    @Test
    void testAdd_Integration_Success() {
        // Arrange - API layer adds inventory
        doNothing().when(inventoryApi).add(any(InventoryPojo.class));

        // Act - DTO calls API
        InventoryData result = inventoryDto.add(testForm);

        // Assert - Verify the entire flow
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals(25, result.getQuantity());
        verify(inventoryApi).add(any(InventoryPojo.class));
    }

    @Test
    void testAdd_Integration_WithBarcode() {
        // Arrange
        testForm.setProductId(null);
        testForm.setBarcode("123456789");
        when(productApi.getByBarcode("123456789")).thenReturn(testProduct);
        doNothing().when(inventoryApi).add(any(InventoryPojo.class));

        // Act
        InventoryData result = inventoryDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals(25, result.getQuantity());
        verify(productApi).getByBarcode("123456789");
        verify(inventoryApi).add(any(InventoryPojo.class));
    }

    @Test
    void testAdd_Integration_ValidationFailure_NoProductIdOrBarcode() {
        // Arrange
        testForm.setProductId(null);
        testForm.setBarcode(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.add(testForm));
        verify(inventoryApi, never()).add(any());
    }

    @Test
    void testAdd_Integration_ValidationFailure_NegativeQuantity() {
        // Arrange
        testForm.setQuantity(-1);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.add(testForm));
        verify(inventoryApi, never()).add(any());
    }

    @Test
    void testGet_Integration_Success() {
        // Arrange - API layer returns inventory
        when(inventoryApi.get(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);

        // Act - DTO calls API
        InventoryData result = inventoryDto.get(1);

        // Assert - Verify the entire flow
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(1, result.getProductId());
        assertEquals(25, result.getQuantity());
        assertEquals("Test Product", result.getProductName());
        verify(inventoryApi).get(1);
        verify(productApi).get(1);
    }

    @Test
    void testGet_Integration_InventoryNotFound() {
        // Arrange
        when(inventoryApi.get(1)).thenThrow(new ApiException("Inventory not found"));

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.get(1));
        verify(inventoryApi).get(1);
        verify(productApi, never()).get(any());
    }

    @Test
    void testUpdate_Integration_Success() {
        // Arrange
        when(inventoryApi.get(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        doNothing().when(inventoryApi).update(anyInt(), any(InventoryPojo.class));

        // Act
        InventoryData result = inventoryDto.update(1, testForm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals(25, result.getQuantity());
        verify(inventoryApi).update(eq(1), any(InventoryPojo.class));
    }

    @Test
    void testUpdate_Integration_InventoryNotFound() {
        // Arrange
        when(inventoryApi.get(1)).thenThrow(new ApiException("Inventory not found"));

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.update(1, testForm));
        verify(inventoryApi).get(1);
        verify(inventoryApi).update(eq(1), any(InventoryPojo.class));
    }

    @Test
    void testGetAll_Integration_Success() {
        // Arrange
        List<InventoryPojo> inventories = Arrays.asList(testInventory);
        when(inventoryApi.getAll()).thenReturn(inventories);
        when(productApi.get(1)).thenReturn(testProduct);

        // Act
        List<InventoryData> result = inventoryDto.getAll();

        // Assert
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getProductId());
        assertEquals(25, result.get(0).getQuantity());
        assertEquals("Test Product", result.get(0).getProductName());
        verify(inventoryApi).getAll();
        verify(productApi).get(1);
    }

    @Test
    void testGetByProductId_Integration_Success() {
        // Arrange
        when(inventoryApi.getByField("productId", 1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);

        // Act
        InventoryData result = inventoryDto.getByProductId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals(25, result.getQuantity());
        verify(inventoryApi).getByField("productId", 1);
    }

    @Test
    void testGetByProductId_Integration_NotFound() {
        // Arrange
        when(inventoryApi.getByField("productId", 1)).thenThrow(new ApiException("Inventory not found"));

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.getByProductId(1));
        verify(inventoryApi).getByField("productId", 1);
    }

    @Test
    void testUpdateQuantity_Integration_Success() {
        // Arrange
        when(inventoryApi.getByProductId(1)).thenReturn(testInventory);
        doNothing().when(inventoryApi).update(anyInt(), any(InventoryPojo.class));

        // Act
        inventoryDto.updateQuantity(1, 50);

        // Assert
        verify(inventoryApi).getByProductId(1);
        verify(inventoryApi).update(eq(1), any(InventoryPojo.class));
    }

    @Test
    void testUpdateQuantity_Integration_InvalidProductId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.updateQuantity(null, 50));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(anyInt(), any());
    }

    @Test
    void testUpdateQuantity_Integration_NegativeQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.updateQuantity(1, -1));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(anyInt(), any());
    }

    @Test
    void testUpdateQuantity_Integration_InventoryNotFound() {
        // Arrange
        when(inventoryApi.getByProductId(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.updateQuantity(1, 50));
        verify(inventoryApi).getByProductId(1);
        verify(inventoryApi, never()).update(anyInt(), any());
    }

    @Test
    void testAddStock_Integration_Success() {
        // Arrange
        when(inventoryApi.getByProductId(1)).thenReturn(testInventory);
        doNothing().when(inventoryApi).update(anyInt(), any(InventoryPojo.class));

        // Act
        inventoryDto.addStock(1, 10);

        // Assert
        verify(inventoryApi).getByProductId(1);
        verify(inventoryApi).update(eq(1), any(InventoryPojo.class));
    }

    @Test
    void testAddStock_Integration_InvalidProductId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.addStock(null, 10));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(anyInt(), any());
    }

    @Test
    void testAddStock_Integration_InvalidQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.addStock(1, 0));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(anyInt(), any());
    }

    @Test
    void testAddStock_Integration_InventoryNotFound() {
        // Arrange
        when(inventoryApi.getByProductId(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.addStock(1, 10));
        verify(inventoryApi).getByProductId(1);
        verify(inventoryApi, never()).update(anyInt(), any());
    }

    @Test
    void testRemoveStock_Integration_Success() {
        // Arrange
        when(inventoryApi.getByProductId(1)).thenReturn(testInventory);
        doNothing().when(inventoryApi).update(anyInt(), any(InventoryPojo.class));

        // Act
        inventoryDto.removeStock(1, 10);

        // Assert
        verify(inventoryApi).getByProductId(1);
        verify(inventoryApi).update(eq(1), any(InventoryPojo.class));
    }

    @Test
    void testRemoveStock_Integration_InsufficientStock() {
        // Arrange
        when(inventoryApi.getByProductId(1)).thenReturn(testInventory);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.removeStock(1, 30));
        verify(inventoryApi).getByProductId(1);
        verify(inventoryApi, never()).update(anyInt(), any());
    }

    @Test
    void testRemoveStock_Integration_InvalidProductId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.removeStock(null, 10));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(anyInt(), any());
    }

    @Test
    void testRemoveStock_Integration_InvalidQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.removeStock(1, 0));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(anyInt(), any());
    }

    @Test
    void testRemoveStock_Integration_InventoryNotFound() {
        // Arrange
        when(inventoryApi.getByProductId(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.removeStock(1, 10));
        verify(inventoryApi).getByProductId(1);
        verify(inventoryApi, never()).update(anyInt(), any());
    }

    @Test
    void testUploadInventoryFromTsv_Integration_Success() throws Exception {
        // Arrange
        String tsvContent = "productId\tquantity\n1\t25\n2\t50";
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        // Mock the parser to return a successful result
        TsvUploadResult mockResult = new TsvUploadResult();
        mockResult.setTotalRows(2);
        mockResult.setSuccessfulRows(2);
        mockResult.setFailedRows(0);
        
        // Mock the static method call
        try (MockedStatic<InventoryTsvParser> mockedParser = mockStatic(InventoryTsvParser.class);
             MockedStatic<FileValidationUtil> mockedValidation = mockStatic(FileValidationUtil.class)) {
            
            doNothing().when(FileValidationUtil.class);
            FileValidationUtil.validateTsvFile(any(MockMultipartFile.class));
            mockedParser.when(() -> InventoryTsvParser.parseWithDuplicateDetection(any(InputStream.class)))
                .thenReturn(mockResult);

            // Act
            TsvUploadResult result = inventoryDto.uploadInventoryFromTsv(file);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getTotalRows());
            assertEquals(2, result.getSuccessfulRows());
            assertEquals(0, result.getFailedRows());
        }
    }

    @Test
    void testUploadInventoryFromTsv_Integration_NullFile() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.uploadInventoryFromTsv(null));
    }

    @Test
    void testUploadInventoryFromTsv_Integration_EmptyFile() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.tsv", "text/tab-separated-values", new byte[0]);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.uploadInventoryFromTsv(file));
    }
} 