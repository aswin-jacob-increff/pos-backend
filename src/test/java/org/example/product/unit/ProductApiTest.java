package org.example.product.unit;

import org.example.api.ProductApi;
import org.example.api.ClientApi;
import org.example.dao.ProductDao;
import org.example.dao.InventoryDao;
import org.example.pojo.ProductPojo;
import org.example.pojo.ClientPojo;
import org.example.exception.ApiException;
import org.example.model.form.PaginationRequest;
import org.example.model.data.PaginationResponse;
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
class ProductApiTest {

    @Mock
    private ProductDao productDao;

    @Mock
    private InventoryDao inventoryDao;

    @Mock
    private ClientApi clientApi;

    @InjectMocks
    private ProductApi productApi;

    private ProductPojo testProduct;
    private ClientPojo testClient;

    @BeforeEach
    void setUp() throws Exception {
        testProduct = new ProductPojo();
        testProduct.setId(1);
        testProduct.setName("Test Product");
        testProduct.setBarcode("TEST123");
        testProduct.setClientId(1);
        testProduct.setMrp(100.0);
        // ProductPojo doesn't have status field

        testClient = new ClientPojo();
        testClient.setId(1);
        testClient.setClientName("Test Client");
        testClient.setStatus(true);

        // Manually inject dependencies using reflection
        Field inventoryDaoField = productApi.getClass().getDeclaredField("inventoryDao");
        inventoryDaoField.setAccessible(true);
        inventoryDaoField.set(productApi, inventoryDao);

        Field clientApiField = productApi.getClass().getDeclaredField("clientApi");
        clientApiField.setAccessible(true);
        clientApiField.set(productApi, clientApi);

        // Inject the dao field from AbstractApi
        Field abstractDaoField = productApi.getClass().getSuperclass().getDeclaredField("dao");
        abstractDaoField.setAccessible(true);
        abstractDaoField.set(productApi, productDao);
    }

    @Test
    void testAdd_Success() {
        // Arrange
        when(clientApi.get(1)).thenReturn(testClient);
        when(productDao.selectByBarcode("TEST123")).thenReturn(null);
        doNothing().when(productDao).insert(any(ProductPojo.class));

        // Act
        productApi.add(testProduct);

        // Assert
        verify(clientApi).get(1);
        verify(productDao).selectByBarcode("TEST123");
        verify(productDao).insert(testProduct);
    }

    @Test
    void testAdd_NullProduct() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.add(null));
        verify(productDao, never()).insert(any());
    }

    @Test
    void testAdd_ClientNotFound() {
        // Arrange
        when(clientApi.get(1)).thenThrow(new ApiException("Client with ID '1' not found"));

        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.add(testProduct));
        verify(productDao, never()).insert(any());
    }

    @Test
    void testAdd_ClientInactive() {
        // Arrange
        testClient.setStatus(false);
        when(clientApi.get(1)).thenReturn(testClient);

        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.add(testProduct));
        verify(productDao, never()).insert(any());
    }

    @Test
    void testAdd_DuplicateBarcode() {
        // Arrange
        when(clientApi.get(1)).thenReturn(testClient);
        when(productDao.selectByBarcode("TEST123")).thenReturn(new ProductPojo());

        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.add(testProduct));
        verify(productDao, never()).insert(any());
    }

    @Test
    void testGet_Success() {
        // Arrange
        when(productDao.select(1)).thenReturn(testProduct);

        // Act
        ProductPojo result = productApi.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        assertEquals(testProduct.getName(), result.getName());
        verify(productDao).select(1);
    }

    @Test
    void testGet_NotFound() {
        // Arrange
        when(productDao.select(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.get(1));
        verify(productDao).select(1);
    }

    @Test
    void testGet_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.get(null));
        verify(productDao, never()).select(any());
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        ProductPojo existingProduct = new ProductPojo();
        existingProduct.setId(1);
        existingProduct.setName("Existing Product");
        existingProduct.setBarcode("EXISTING123");
        existingProduct.setClientId(1);
        existingProduct.setMrp(50.0);

        when(productDao.select(1)).thenReturn(existingProduct);
        when(clientApi.get(1)).thenReturn(testClient);
        when(productDao.selectByBarcode("TEST123")).thenReturn(null);
        doNothing().when(productDao).update(any(Integer.class), any(ProductPojo.class));

        // Act
        productApi.update(1, testProduct);

        // Assert
        verify(productDao).select(1);
        verify(clientApi).get(1);
        verify(productDao).selectByBarcode("TEST123");
        verify(productDao).update(1, testProduct);
    }

    @Test
    void testUpdate_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.update(null, testProduct));
        verify(productDao, never()).update(any(), any());
    }

    @Test
    void testUpdate_NullProduct() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.update(1, null));
        verify(productDao, never()).update(any(), any());
    }

    @Test
    void testUpdate_ProductNotFound() {
        // Arrange
        when(productDao.select(1)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.update(1, testProduct));
        verify(productDao).select(1);
        verify(productDao, never()).update(any(), any());
    }

    @Test
    void testUpdate_DuplicateBarcode() {
        // Arrange
        ProductPojo existingProduct = new ProductPojo();
        existingProduct.setId(1);
        existingProduct.setName("Existing Product");
        existingProduct.setBarcode("EXISTING123");
        existingProduct.setClientId(1);
        existingProduct.setMrp(50.0);

        ProductPojo duplicateProduct = new ProductPojo();
        duplicateProduct.setId(2);
        duplicateProduct.setBarcode("TEST123");

        when(productDao.select(1)).thenReturn(existingProduct);
        when(clientApi.get(1)).thenReturn(testClient);
        when(productDao.selectByBarcode("TEST123")).thenReturn(duplicateProduct);

        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.update(1, testProduct));
        verify(productDao).select(1);
        verify(productDao, never()).update(any(), any());
    }

    @Test
    void testGetByName_Success() {
        // Arrange
        when(productDao.selectByField("name", "Test Product")).thenReturn(testProduct);

        // Act
        ProductPojo result = productApi.getByName("Test Product");

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        assertEquals(testProduct.getName(), result.getName());
        verify(productDao).selectByField("name", "Test Product");
    }

    @Test
    void testGetByName_NullName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByName(null));
        verify(productDao, never()).selectByField(any(), any());
    }

    @Test
    void testGetByName_EmptyName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByName(""));
        verify(productDao, never()).selectByField(any(), any());
    }

    @Test
    void testGetByNameLike_Success() {
        // Arrange
        List<ProductPojo> products = Arrays.asList(testProduct);
        when(productDao.selectByFieldLike("name", "Test")).thenReturn(products);

        // Act
        List<ProductPojo> result = productApi.getByNameLike("Test");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProduct.getId(), result.get(0).getId());
        verify(productDao).selectByFieldLike("name", "Test");
    }

    @Test
    void testGetByNameLike_NullName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByNameLike(null));
        verify(productDao, never()).selectByFieldLike(any(), any());
    }

    @Test
    void testGetByBarcode_Success() {
        // Arrange
        when(productDao.selectByBarcode("TEST123")).thenReturn(testProduct);

        // Act
        ProductPojo result = productApi.getByBarcode("TEST123");

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        assertEquals(testProduct.getBarcode(), result.getBarcode());
        verify(productDao).selectByBarcode("TEST123");
    }

    @Test
    void testGetByBarcode_NullBarcode() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByBarcode(null));
        verify(productDao, never()).selectByBarcode(any());
    }

    @Test
    void testGetByBarcode_EmptyBarcode() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByBarcode(""));
        verify(productDao, never()).selectByBarcode(any());
    }

    @Test
    void testGetByBarcode_NotFound() {
        // Arrange
        when(productDao.selectByBarcode("NONEXISTENT")).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByBarcode("NONEXISTENT"));
        verify(productDao).selectByBarcode("NONEXISTENT");
    }

    @Test
    void testGetByBarcodeLike_Success() {
        // Arrange
        List<ProductPojo> products = Arrays.asList(testProduct);
        when(productDao.selectByFieldLike("barcode", "TEST")).thenReturn(products);

        // Act
        List<ProductPojo> result = productApi.getByBarcodeLike("TEST");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProduct.getId(), result.get(0).getId());
        verify(productDao).selectByFieldLike("barcode", "TEST");
    }

    @Test
    void testGetByBarcodeLike_NullBarcode() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByBarcodeLike(null));
        verify(productDao, never()).selectByFieldLike(any(), any());
    }

    @Test
    void testGetByClientId_Success() {
        // Arrange
        List<ProductPojo> products = Arrays.asList(testProduct);
        when(productDao.selectByClientId(1)).thenReturn(products);

        // Act
        List<ProductPojo> result = productApi.getByClientId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProduct.getId(), result.get(0).getId());
        verify(productDao).selectByClientId(1);
    }

    @Test
    void testGetByClientId_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByClientId(null));
        verify(productDao, never()).selectByClientId(any());
    }

    @Test
    void testGetByClientName_Success() {
        // Arrange
        List<ProductPojo> products = Arrays.asList(testProduct);
        when(clientApi.getByName("Test Client")).thenReturn(testClient);
        when(productDao.selectByClientId(1)).thenReturn(products);

        // Act
        List<ProductPojo> result = productApi.getByClientName("Test Client");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProduct.getId(), result.get(0).getId());
        verify(clientApi).getByName("Test Client");
        verify(productDao).selectByClientId(1);
    }

    @Test
    void testGetByClientName_NullName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByClientName(null));
        verify(clientApi, never()).getByName(any());
    }

    @Test
    void testGetByClientName_EmptyName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByClientName(""));
        verify(clientApi, never()).getByName(any());
    }

    @Test
    void testGetByClientName_ClientNotFound() {
        // Arrange
        when(clientApi.getByName("NonExistent")).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByClientName("NonExistent"));
        verify(clientApi).getByName("NonExistent");
        verify(productDao, never()).selectByFields(any(), any());
    }

    @Test
    void testHasProductsByClientId_True() {
        // Arrange
        when(productDao.hasProductsByClientId(1)).thenReturn(true);

        // Act
        boolean result = productApi.hasProductsByClientId(1);

        // Assert
        assertTrue(result);
        verify(productDao).hasProductsByClientId(1);
    }

    @Test
    void testHasProductsByClientId_False() {
        // Arrange
        when(productDao.hasProductsByClientId(1)).thenReturn(false);

        // Act
        boolean result = productApi.hasProductsByClientId(1);

        // Assert
        assertFalse(result);
        verify(productDao).hasProductsByClientId(1);
    }

    @Test
    void testHasProductsByClientId_NullId() {
        // Act
        boolean result = productApi.hasProductsByClientId(null);

        // Assert
        assertFalse(result);
        verify(productDao, never()).hasProductsByClientId(any());
    }

    @Test
    void testHasProductsByClientName_True() {
        // Arrange
        when(clientApi.getByName("Test Client")).thenReturn(testClient);
        when(productDao.hasProductsByClientId(1)).thenReturn(true);

        // Act
        boolean result = productApi.hasProductsByClientName("Test Client");

        // Assert
        assertTrue(result);
        verify(clientApi).getByName("Test Client");
        verify(productDao).hasProductsByClientId(1);
    }

    @Test
    void testHasProductsByClientName_False() {
        // Arrange
        when(clientApi.getByName("Test Client")).thenReturn(testClient);
        when(productDao.hasProductsByClientId(1)).thenReturn(false);

        // Act
        boolean result = productApi.hasProductsByClientName("Test Client");

        // Assert
        assertFalse(result);
        verify(clientApi).getByName("Test Client");
        verify(productDao).hasProductsByClientId(1);
    }

    @Test
    void testHasProductsByClientName_NullName() {
        // Act
        boolean result = productApi.hasProductsByClientName(null);

        // Assert
        assertFalse(result);
        verify(clientApi, never()).getByName(any());
    }

    @Test
    void testHasProductsByClientName_EmptyName() {
        // Act
        boolean result = productApi.hasProductsByClientName("");

        // Assert
        assertFalse(result);
        verify(clientApi, never()).getByName(any());
    }

    @Test
    void testHasProductsByClientName_ClientNotFound() {
        // Arrange
        when(clientApi.getByName("NonExistent")).thenReturn(null);

        // Act
        boolean result = productApi.hasProductsByClientName("NonExistent");

        // Assert
        assertFalse(result);
        verify(clientApi).getByName("NonExistent");
        verify(productDao, never()).hasProductsByClientId(any());
    }
} 