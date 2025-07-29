package org.example.inventory.unit;

import org.example.dto.InventoryDto;
import org.example.api.InventoryApi;
import org.example.api.ProductApi;
import org.example.model.data.InventoryData;
import org.example.model.form.InventoryForm;
import org.example.model.data.TsvUploadResult;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
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
class InventoryDtoTest {

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
        testProduct.setClientId(1);

        testForm = new InventoryForm();
        testForm.setProductId(1);
        testForm.setQuantity(10);

        testInventory = new InventoryPojo();
        testInventory.setId(1);
        testInventory.setProductId(1);
        testInventory.setQuantity(10);

        testInventoryData = new InventoryData();
        testInventoryData.setId(1);
        testInventoryData.setProductId(1);
        testInventoryData.setQuantity(10);
        testInventoryData.setProductName("Test Product");
        testInventoryData.setBarcode("123456789");
        testInventoryData.setMrp(100.0);

        // Inject the api field from AbstractDto
        Field apiField = inventoryDto.getClass().getSuperclass().getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(inventoryDto, inventoryApi);
    }

    @Test
    void testAdd_Success_WithProductId() {
        // Arrange
        doNothing().when(inventoryApi).add(any(InventoryPojo.class));

        // Act
        InventoryData result = inventoryDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals(10, result.getQuantity());
        verify(inventoryApi).add(any(InventoryPojo.class));
    }

    @Test
    void testAdd_Success_WithBarcode() {
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
        assertEquals(10, result.getQuantity());
        verify(productApi).getByBarcode("123456789");
        verify(inventoryApi).add(any(InventoryPojo.class));
    }

    @Test
    void testAdd_WithBarcode_ProductNotFound() {
        // Arrange
        testForm.setProductId(null);
        testForm.setBarcode("nonexistent");
        when(productApi.getByBarcode("nonexistent")).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.add(testForm));
        verify(productApi).getByBarcode("nonexistent");
        verify(inventoryApi, never()).add(any());
    }

    @Test
    void testAdd_NullForm() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.add(null));
        verify(inventoryApi, never()).add(any());
    }

    @Test
    void testAdd_NoProductIdOrBarcode() {
        // Arrange
        testForm.setProductId(null);
        testForm.setBarcode(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.add(testForm));
        verify(inventoryApi, never()).add(any());
    }

    @Test
    void testAdd_ZeroProductId() {
        // Arrange
        testForm.setProductId(0);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.add(testForm));
        verify(inventoryApi, never()).add(any());
    }

    @Test
    void testAdd_NegativeProductId() {
        // Arrange
        testForm.setProductId(-1);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.add(testForm));
        verify(inventoryApi, never()).add(any());
    }

    @Test
    void testAdd_EmptyBarcode() {
        // Arrange
        testForm.setProductId(null);
        testForm.setBarcode("");

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.add(testForm));
        verify(inventoryApi, never()).add(any());
    }

    @Test
    void testAdd_NullQuantity() {
        // Arrange
        testForm.setQuantity(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.add(testForm));
        verify(inventoryApi, never()).add(any());
    }

    @Test
    void testAdd_NegativeQuantity() {
        // Arrange
        testForm.setQuantity(-5);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.add(testForm));
        verify(inventoryApi, never()).add(any());
    }

    @Test
    void testGet_Success() {
        // Arrange
        when(inventoryApi.get(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);

        // Act
        InventoryData result = inventoryDto.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(testInventory.getId(), result.getId());
        assertEquals(testInventory.getProductId(), result.getProductId());
        assertEquals(testInventory.getQuantity(), result.getQuantity());
        assertEquals(testProduct.getName(), result.getProductName());
        assertEquals(testProduct.getBarcode(), result.getBarcode());
        assertEquals(testProduct.getMrp(), result.getMrp());
        verify(inventoryApi).get(1);
        verify(productApi).get(1);
    }

    @Test
    void testGet_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.get(null));
        verify(inventoryApi, never()).get(any());
    }

    @Test
    void testGet_ProductNotFound() {
        // Arrange
        when(inventoryApi.get(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenThrow(new ApiException("Product not found"));

        // Act
        InventoryData result = inventoryDto.get(1);

        // Assert
        assertNotNull(result);
        assertEquals("Unknown", result.getProductName());
        assertEquals("Unknown", result.getBarcode());
        assertEquals(0.0, result.getMrp());
        verify(inventoryApi).get(1);
        verify(productApi).get(1);
    }

    @Test
    void testGetAll_Success() {
        // Arrange
        List<InventoryPojo> inventories = Arrays.asList(testInventory, testInventory);
        when(inventoryApi.getAll()).thenReturn(inventories);
        when(productApi.get(1)).thenReturn(testProduct);

        // Act
        List<InventoryData> result = inventoryDto.getAll();

        // Assert
        assertEquals(2, result.size());
        verify(inventoryApi).getAll();
        verify(productApi, times(2)).get(1);
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        when(inventoryApi.get(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        doNothing().when(inventoryApi).update(anyInt(), any(InventoryPojo.class));

        // Act
        InventoryData result = inventoryDto.update(1, testForm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals(10, result.getQuantity());
        verify(inventoryApi).update(eq(1), any(InventoryPojo.class));
    }

    @Test
    void testUpdate_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.update(null, testForm));
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testUpdate_NullForm() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.update(1, null));
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testGetByProductId_Success() {
        // Arrange
        when(inventoryApi.getByField("productId", 1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);

        // Act
        InventoryData result = inventoryDto.getByProductId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals(10, result.getQuantity());
        verify(inventoryApi).getByField("productId", 1);
    }

    @Test
    void testGetByProductId_NullProductId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.getByProductId(null));
        verify(inventoryApi, never()).getByField(any(), any());
    }

    @Test
    void testGetByProductIdPaginated_Success() {
        // Arrange
        PaginationRequest request = new PaginationRequest();
        request.setPageNumber(0);
        request.setPageSize(10);
        
        PaginationResponse<InventoryPojo> expectedResponse = new PaginationResponse<>();
        expectedResponse.setContent(Arrays.asList(testInventory));
        expectedResponse.setTotalElements(1);
        
        when(inventoryApi.getPaginated(any())).thenReturn(expectedResponse);
        when(productApi.get(1)).thenReturn(testProduct);

        // Act
        PaginationResponse<InventoryData> result = inventoryDto.getByProductIdPaginated(1, request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(inventoryApi).getPaginated(any());
    }

    @Test
    void testUpdateQuantity_Success() {
        // Arrange
        when(inventoryApi.getByProductId(1)).thenReturn(testInventory);
        doNothing().when(inventoryApi).update(anyInt(), any(InventoryPojo.class));

        // Act
        inventoryDto.updateQuantity(1, 20);

        // Assert
        verify(inventoryApi).getByProductId(1);
        verify(inventoryApi).update(eq(1), any(InventoryPojo.class));
    }

    @Test
    void testUpdateQuantity_NullProductId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.updateQuantity(null, 20));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testUpdateQuantity_ZeroProductId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.updateQuantity(0, 20));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testUpdateQuantity_NegativeProductId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.updateQuantity(-1, 20));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testUpdateQuantity_NullQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.updateQuantity(1, null));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testUpdateQuantity_NegativeQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.updateQuantity(1, -5));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testUpdateQuantity_InventoryNotFound() {
        // Arrange
        when(inventoryApi.getByProductId(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.updateQuantity(1, 20));
        verify(inventoryApi).getByProductId(1);
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testAddStock_Success() {
        // Arrange
        when(inventoryApi.getByProductId(1)).thenReturn(testInventory);
        doNothing().when(inventoryApi).update(anyInt(), any(InventoryPojo.class));

        // Act
        inventoryDto.addStock(1, 5);

        // Assert
        verify(inventoryApi).getByProductId(1);
        verify(inventoryApi).update(eq(1), any(InventoryPojo.class));
    }

    @Test
    void testAddStock_NullProductId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.addStock(null, 5));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testAddStock_ZeroProductId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.addStock(0, 5));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testAddStock_NegativeProductId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.addStock(-1, 5));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testAddStock_NullQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.addStock(1, null));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testAddStock_ZeroQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.addStock(1, 0));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testAddStock_NegativeQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.addStock(1, -5));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testAddStock_InventoryNotFound() {
        // Arrange
        when(inventoryApi.getByProductId(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.addStock(1, 5));
        verify(inventoryApi).getByProductId(1);
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testRemoveStock_Success() {
        // Arrange
        when(inventoryApi.getByProductId(1)).thenReturn(testInventory);
        doNothing().when(inventoryApi).update(anyInt(), any(InventoryPojo.class));

        // Act
        inventoryDto.removeStock(1, 3);

        // Assert
        verify(inventoryApi).getByProductId(1);
        verify(inventoryApi).update(eq(1), any(InventoryPojo.class));
    }

    @Test
    void testRemoveStock_NullProductId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.removeStock(null, 3));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testRemoveStock_ZeroProductId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.removeStock(0, 3));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testRemoveStock_NegativeProductId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.removeStock(-1, 3));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testRemoveStock_NullQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.removeStock(1, null));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testRemoveStock_ZeroQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.removeStock(1, 0));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testRemoveStock_NegativeQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.removeStock(1, -3));
        verify(inventoryApi, never()).getByProductId(any());
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testRemoveStock_InventoryNotFound() {
        // Arrange
        when(inventoryApi.getByProductId(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.removeStock(1, 3));
        verify(inventoryApi).getByProductId(1);
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testRemoveStock_InsufficientStock() {
        // Arrange
        when(inventoryApi.getByProductId(1)).thenReturn(testInventory);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.removeStock(1, 15));
        verify(inventoryApi).getByProductId(1);
        verify(inventoryApi, never()).update(any(), any());
    }

    @Test
    void testAddStockAndReturn_Success() {
        // Arrange
        when(inventoryApi.getByProductId(1)).thenReturn(testInventory);
        when(inventoryApi.getByField("productId", 1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        doNothing().when(inventoryApi).update(anyInt(), any(InventoryPojo.class));

        // Act
        InventoryData result = inventoryDto.addStockAndReturn(1, 5);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        verify(inventoryApi).getByProductId(1);
        verify(inventoryApi).update(eq(1), any(InventoryPojo.class));
        verify(inventoryApi).getByField("productId", 1);
    }

    @Test
    void testRemoveStockAndReturn_Success() {
        // Arrange
        when(inventoryApi.getByProductId(1)).thenReturn(testInventory);
        when(inventoryApi.getByField("productId", 1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        doNothing().when(inventoryApi).update(anyInt(), any(InventoryPojo.class));

        // Act
        InventoryData result = inventoryDto.removeStockAndReturn(1, 3);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        verify(inventoryApi).getByProductId(1);
        verify(inventoryApi).update(eq(1), any(InventoryPojo.class));
        verify(inventoryApi).getByField("productId", 1);
    }

    @Test
    void testSetStock_Success() {
        // Arrange
        when(inventoryApi.getByProductId(1)).thenReturn(testInventory);
        when(inventoryApi.getByField("productId", 1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        doNothing().when(inventoryApi).update(anyInt(), any(InventoryPojo.class));

        // Act
        InventoryData result = inventoryDto.setStock(1, 25);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        verify(inventoryApi).getByProductId(1);
        verify(inventoryApi).update(eq(1), any(InventoryPojo.class));
        verify(inventoryApi).getByField("productId", 1);
    }

    @Test
    void testUploadInventoryFromTsv_Success() throws Exception {
        // Arrange
        String tsvContent = "productId\tquantity\n1\t10\n2\t20";
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
    void testUploadInventoryFromTsv_NullFile() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.uploadInventoryFromTsv(null));
    }

    @Test
    void testUploadInventoryFromTsv_EmptyFile() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.tsv", "text/tab-separated-values", new byte[0]);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.uploadInventoryFromTsv(file));
    }
} 