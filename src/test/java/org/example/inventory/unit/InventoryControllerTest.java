package org.example.inventory.unit;

import org.example.controller.InventoryController;
import org.example.dto.InventoryDto;
import org.example.api.ProductApi;
import org.example.dto.ProductDto;
import org.example.model.data.InventoryData;
import org.example.model.form.InventoryForm;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
import org.example.pojo.ProductPojo;
import org.example.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    private InventoryDto inventoryDto;

    @Mock
    private ProductApi productApi;

    @Mock
    private ProductDto productDto;

    @InjectMocks
    private InventoryController inventoryController;

    private InventoryForm testForm;
    private InventoryData testInventoryData;
    private ProductPojo testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new ProductPojo();
        testProduct.setId(1);
        testProduct.setName("Test Product");
        testProduct.setBarcode("123456789");
        testProduct.setMrp(100.0);

        testForm = new InventoryForm();
        testForm.setProductId(1);
        testForm.setQuantity(10);

        testInventoryData = new InventoryData();
        testInventoryData.setId(1);
        testInventoryData.setProductId(1);
        testInventoryData.setQuantity(10);
        testInventoryData.setProductName("Test Product");
        testInventoryData.setBarcode("123456789");
        testInventoryData.setMrp(100.0);
    }

    @Test
    void testAdd_Success() {
        // Arrange
        when(inventoryDto.add(testForm)).thenReturn(testInventoryData);

        // Act
        InventoryData result = inventoryController.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals(10, result.getQuantity());
        verify(inventoryDto).add(testForm);
    }

    @Test
    void testAdd_ThrowsException() {
        // Arrange
        when(inventoryDto.add(testForm)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> inventoryController.add(testForm));
        verify(inventoryDto).add(testForm);
    }

    @Test
    void testGet_Success() {
        // Arrange
        when(inventoryDto.get(1)).thenReturn(testInventoryData);

        // Act
        InventoryData result = inventoryController.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(inventoryDto).get(1);
    }

    @Test
    void testGet_ThrowsException() {
        // Arrange
        when(inventoryDto.get(1)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> inventoryController.get(1));
        verify(inventoryDto).get(1);
    }

    @Test
    void testGetAll_Success() {
        // Arrange
        List<InventoryData> inventories = Arrays.asList(testInventoryData);
        when(inventoryDto.getAll()).thenReturn(inventories);

        // Act
        List<InventoryData> result = inventoryController.getAll();

        // Assert
        assertEquals(1, result.size());
        verify(inventoryDto).getAll();
    }

    @Test
    void testGetAll_ThrowsException() {
        // Arrange
        when(inventoryDto.getAll()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> inventoryController.getAll());
        verify(inventoryDto).getAll();
    }

    @Test
    void testGetAllInventoryPaginated_Success() {
        // Arrange
        PaginationResponse<InventoryData> expectedResponse = new PaginationResponse<>();
        expectedResponse.setContent(Arrays.asList(testInventoryData));
        expectedResponse.setTotalElements(1);
        
        when(inventoryDto.getPaginated(any())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<PaginationResponse<InventoryData>> result = inventoryController.getAllInventoryPaginated(0, 20, "id", "ASC");

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getTotalElements());
        verify(inventoryDto).getPaginated(any());
    }

    @Test
    void testGetAllInventoryPaginated_ThrowsException() {
        // Arrange
        when(inventoryDto.getPaginated(any())).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> inventoryController.getAllInventoryPaginated(0, 20, "id", "ASC"));
        verify(inventoryDto).getPaginated(any());
    }

    @Test
    void testGetByProductNamePaginated_Success() {
        // Arrange
        when(productApi.getByName("Test Product")).thenReturn(testProduct);
        
        PaginationResponse<InventoryData> expectedResponse = new PaginationResponse<>();
        expectedResponse.setContent(Arrays.asList(testInventoryData));
        expectedResponse.setTotalElements(1);
        
        when(inventoryDto.getByProductIdPaginated(eq(1), any())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<PaginationResponse<InventoryData>> result = inventoryController.getByProductNamePaginated("Test Product", 0, 20, "id", "ASC");

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getTotalElements());
        verify(productApi).getByName("Test Product");
        verify(inventoryDto).getByProductIdPaginated(eq(1), any());
    }

    @Test
    void testGetByProductNamePaginated_ProductNotFound() {
        // Arrange
        when(productApi.getByName("NonExistent")).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryController.getByProductNamePaginated("NonExistent", 0, 20, "id", "ASC"));
        verify(productApi).getByName("NonExistent");
        verify(inventoryDto, never()).getByProductIdPaginated(any(), any());
    }

    @Test
    void testGetByProductBarcodePaginated_Success() {
        // Arrange
        when(productApi.getByBarcode("123456789")).thenReturn(testProduct);
        
        PaginationResponse<InventoryData> expectedResponse = new PaginationResponse<>();
        expectedResponse.setContent(Arrays.asList(testInventoryData));
        expectedResponse.setTotalElements(1);
        
        when(inventoryDto.getByProductIdPaginated(eq(1), any())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<PaginationResponse<InventoryData>> result = inventoryController.getByProductBarcodePaginated("123456789", 0, 20, "id", "ASC");

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getTotalElements());
        verify(productApi).getByBarcode("123456789");
        verify(inventoryDto).getByProductIdPaginated(eq(1), any());
    }

    @Test
    void testGetByProductBarcodePaginated_ProductNotFound() {
        // Arrange
        when(productApi.getByBarcode("INVALID")).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryController.getByProductBarcodePaginated("INVALID", 0, 20, "id", "ASC"));
        verify(productApi).getByBarcode("INVALID");
        verify(inventoryDto, never()).getByProductIdPaginated(any(), any());
    }

    @Test
    void testGetInventoryByAny_WithId() {
        // Arrange
        when(inventoryDto.getByProductId(1)).thenReturn(testInventoryData);

        // Act
        List<InventoryData> result = inventoryController.getInventoryByAny(1, null, null);

        // Assert
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getProductId());
        verify(inventoryDto).getByProductId(1);
    }

    @Test
    void testGetInventoryByAny_WithBarcode() {
        // Arrange
        when(productApi.getByBarcode("123456789")).thenReturn(testProduct);
        when(inventoryDto.getByProductId(1)).thenReturn(testInventoryData);

        // Act
        List<InventoryData> result = inventoryController.getInventoryByAny(null, "123456789", null);

        // Assert
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getProductId());
        verify(productApi).getByBarcode("123456789");
        verify(inventoryDto).getByProductId(1);
    }

    @Test
    void testGetInventoryByAny_WithName() {
        // Arrange
        when(productApi.getByName("Test Product")).thenReturn(testProduct);
        when(inventoryDto.getByProductId(1)).thenReturn(testInventoryData);

        // Act
        List<InventoryData> result = inventoryController.getInventoryByAny(null, null, "Test Product");

        // Assert
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getProductId());
        verify(productApi).getByName("Test Product");
        verify(inventoryDto).getByProductId(1);
    }

    @Test
    void testGetInventoryByAny_NoParameters() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryController.getInventoryByAny(null, null, null));
    }

    @Test
    void testGetInventoryByAny_ProductNotFound() {
        // Arrange
        when(productApi.getByBarcode("INVALID")).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryController.getInventoryByAny(null, "INVALID", null));
        verify(productApi).getByBarcode("INVALID");
        verify(inventoryDto, never()).getByProductId(any());
    }

    @Test
    void testGetInventoryByAny_InventoryNotFound() {
        // Arrange
        when(productApi.getByBarcode("123456789")).thenReturn(testProduct);
        when(inventoryDto.getByProductId(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryController.getInventoryByAny(null, "123456789", null));
        verify(productApi).getByBarcode("123456789");
        verify(inventoryDto).getByProductId(1);
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        when(inventoryDto.update(1, testForm)).thenReturn(testInventoryData);

        // Act
        InventoryData result = inventoryController.update(1, testForm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(inventoryDto).update(1, testForm);
    }

    @Test
    void testUpdate_ThrowsException() {
        // Arrange
        when(inventoryDto.update(1, testForm)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> inventoryController.update(1, testForm));
        verify(inventoryDto).update(1, testForm);
    }

    @Test
    void testGetByProductBarcode_Success() {
        // Arrange
        when(productApi.getByBarcode("123456789")).thenReturn(testProduct);
        when(inventoryDto.getByProductId(1)).thenReturn(testInventoryData);

        // Act
        InventoryData result = inventoryController.getByProductBarcode("123456789");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        verify(productApi).getByBarcode("123456789");
        verify(inventoryDto).getByProductId(1);
    }

    @Test
    void testGetByProductBarcode_ProductNotFound() {
        // Arrange
        when(productApi.getByBarcode("INVALID")).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryController.getByProductBarcode("INVALID"));
        verify(productApi).getByBarcode("INVALID");
        verify(inventoryDto, never()).getByProductId(any());
    }

    @Test
    void testSearchByProductBarcode_Success() {
        // Arrange
        when(productApi.getByBarcode("123456789")).thenReturn(testProduct);
        when(inventoryDto.getByProductId(1)).thenReturn(testInventoryData);

        // Act
        List<InventoryData> result = inventoryController.searchByProductBarcode("123456789");

        // Assert
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getProductId());
        verify(productApi).getByBarcode("123456789");
        verify(inventoryDto).getByProductId(1);
    }

    @Test
    void testSearchByProductBarcode_ProductNotFound() {
        // Arrange
        when(productApi.getByBarcode("INVALID")).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryController.searchByProductBarcode("INVALID"));
        verify(productApi).getByBarcode("INVALID");
        verify(inventoryDto, never()).getByProductId(any());
    }

    @Test
    void testSearchByProductName_Success() {
        // Arrange
        when(productApi.getByName("Test Product")).thenReturn(testProduct);
        when(inventoryDto.getByProductId(1)).thenReturn(testInventoryData);

        // Act
        List<InventoryData> result = inventoryController.searchByProductName("Test Product");

        // Assert
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getProductId());
        verify(productApi).getByName("Test Product");
        verify(inventoryDto).getByProductId(1);
    }

    @Test
    void testSearchByProductName_ProductNotFound() {
        // Arrange
        when(productApi.getByName("NonExistent")).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryController.searchByProductName("NonExistent"));
        verify(productApi).getByName("NonExistent");
        verify(inventoryDto, never()).getByProductId(any());
    }

    @Test
    void testAddStock_Success() {
        // Arrange
        when(productApi.getByBarcode("123456789")).thenReturn(testProduct);
        when(inventoryDto.addStockAndReturn(1, 5)).thenReturn(testInventoryData);

        // Act
        InventoryData result = inventoryController.addStock("123456789", 5);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        verify(productApi).getByBarcode("123456789");
        verify(inventoryDto).addStockAndReturn(1, 5);
    }

    @Test
    void testAddStock_NullProductId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryController.addStock(null, 5));
        verify(productApi, never()).getByBarcode(any());
        verify(inventoryDto, never()).addStockAndReturn(any(), any());
    }

    @Test
    void testAddStock_EmptyProductId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryController.addStock("", 5));
        verify(productApi, never()).getByBarcode(any());
        verify(inventoryDto, never()).addStockAndReturn(any(), any());
    }

    @Test
    void testAddStock_NullQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryController.addStock("123456789", null));
        verify(productApi, never()).getByBarcode(any());
        verify(inventoryDto, never()).addStockAndReturn(any(), any());
    }

    @Test
    void testAddStock_ProductNotFound() {
        // Arrange
        when(productApi.getByBarcode("INVALID")).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryController.addStock("INVALID", 5));
        verify(productApi).getByBarcode("INVALID");
        verify(inventoryDto, never()).addStockAndReturn(any(), any());
    }

    @Test
    void testRemoveStock_Success() {
        // Arrange
        when(productApi.getByBarcode("123456789")).thenReturn(testProduct);
        when(inventoryDto.removeStockAndReturn(1, 3)).thenReturn(testInventoryData);

        // Act
        InventoryData result = inventoryController.removeStock("123456789", 3);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        verify(productApi).getByBarcode("123456789");
        verify(inventoryDto).removeStockAndReturn(1, 3);
    }

    @Test
    void testRemoveStock_NullProductId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryController.removeStock(null, 3));
        verify(productApi, never()).getByBarcode(any());
        verify(inventoryDto, never()).removeStockAndReturn(any(), any());
    }

    @Test
    void testRemoveStock_NullQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryController.removeStock("123456789", null));
        verify(productApi, never()).getByBarcode(any());
        verify(inventoryDto, never()).removeStockAndReturn(any(), any());
    }

    @Test
    void testRemoveStock_ProductNotFound() {
        // Arrange
        when(productApi.getByBarcode("INVALID")).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryController.removeStock("INVALID", 3));
        verify(productApi).getByBarcode("INVALID");
        verify(inventoryDto, never()).removeStockAndReturn(any(), any());
    }

    @Test
    void testSetStock_Success() {
        // Arrange
        when(productApi.getByBarcode("123456789")).thenReturn(testProduct);
        when(inventoryDto.setStock(1, 50)).thenReturn(testInventoryData);

        // Act
        InventoryData result = inventoryController.setStock("123456789", 50);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        verify(productApi).getByBarcode("123456789");
        verify(inventoryDto).setStock(1, 50);
    }

    @Test
    void testSetStock_NullProductId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryController.setStock(null, 50));
        verify(productApi, never()).getByBarcode(any());
        verify(inventoryDto, never()).setStock(any(), any());
    }

    @Test
    void testSetStock_NullQuantity() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryController.setStock("123456789", null));
        verify(productApi, never()).getByBarcode(any());
        verify(inventoryDto, never()).setStock(any(), any());
    }

    @Test
    void testSetStock_ProductNotFound() {
        // Arrange
        when(productApi.getByBarcode("INVALID")).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryController.setStock("INVALID", 50));
        verify(productApi).getByBarcode("INVALID");
        verify(inventoryDto, never()).setStock(any(), any());
    }
} 