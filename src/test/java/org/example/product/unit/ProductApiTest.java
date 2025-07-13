package org.example.product.unit;

import org.example.api.ProductApi;
import org.example.dao.ProductDao;
import org.example.api.ClientApi;
import org.example.pojo.ProductPojo;
import org.example.pojo.ClientPojo;
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
class ProductApiTest {

    @Mock
    private ProductDao productDao;

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
        testProduct.setClientName("TestClient");
        testProduct.setMrp(100.0);

        testClient = new ClientPojo();
        testClient.setId(1);
        testClient.setClientName("TestClient");
        testClient.setStatus(true);

        // Inject the dao field using reflection
        Field daoField = productApi.getClass().getSuperclass().getDeclaredField("dao");
        daoField.setAccessible(true);
        daoField.set(productApi, productDao);
    }

    @Test
    void testAdd_Success() {
        // Arrange
        when(clientApi.getByName("TestClient")).thenReturn(testClient);
        when(productDao.selectByBarcode("TEST123")).thenReturn(null);
        doNothing().when(productDao).insert(any(ProductPojo.class));

        // Act
        productApi.add(testProduct);

        // Assert
        verify(clientApi, atLeastOnce()).getByName("TestClient");
        verify(productDao, atLeastOnce()).selectByBarcode("TEST123");
        verify(productDao).insert(any(ProductPojo.class));
    }

    @Test
    void testAdd_NullProduct() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.add(null));
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
        verify(productDao).select(1);
    }

    @Test
    void testGet_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.get(null));
        verify(productDao, never()).select(any());
    }

    @Test
    void testGetAll_Success() {
        // Arrange
        List<ProductPojo> products = Arrays.asList(testProduct, testProduct);
        when(productDao.selectAll()).thenReturn(products);

        // Act
        List<ProductPojo> result = productApi.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productDao).selectAll();
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        when(productDao.select(1)).thenReturn(testProduct);
        when(clientApi.getByName("TestClient")).thenReturn(testClient);
        doNothing().when(productDao).update(1, testProduct);

        // Act
        productApi.update(1, testProduct);

        // Assert
        verify(productDao, atLeastOnce()).select(1);
        verify(clientApi, atLeastOnce()).getByName("TestClient");
        verify(productDao).update(eq(1), any(ProductPojo.class));
    }

    @Test
    void testUpdate_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.update(null, testProduct));
        verify(productDao, never()).update(anyInt(), any());
    }

    @Test
    void testUpdate_NullProduct() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.update(1, null));
        verify(productDao, never()).update(anyInt(), any());
    }

    @Test
    void testUpdate_NotFound() {
        // Arrange
        when(productDao.select(999)).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.update(999, testProduct));
        verify(productDao).select(999);
        verify(productDao, never()).update(anyInt(), any());
    }

    @Test
    void testGetByName_Success() {
        // Arrange
        when(productDao.selectByField("name", "Test Product")).thenReturn(testProduct);

        // Act
        ProductPojo result = productApi.getByName("Test Product");

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getName(), result.getName());
        verify(productDao).selectByField("name", "Test Product");
    }

    @Test
    void testGetByName_NullName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByName(null));
        verify(productDao, never()).selectByField(anyString(), any());
    }

    @Test
    void testGetByName_EmptyName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByName(""));
        verify(productDao, never()).selectByField(anyString(), any());
    }

    @Test
    void testGetByBarcode_Success() {
        // Given
        when(productDao.selectByBarcode("TEST123")).thenReturn(testProduct);

        // When
        ProductPojo result = productApi.getByBarcode("TEST123");

        // Then
        assertNotNull(result);
        assertEquals(testProduct.getBarcode(), result.getBarcode());
        verify(productDao).selectByBarcode("TEST123");
    }

    @Test
    void testGetByBarcode_NullBarcode() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByBarcode(null));
        verify(productDao, never()).selectByField(anyString(), any());
    }

    @Test
    void testGetByBarcode_EmptyBarcode() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByBarcode(""));
        verify(productDao, never()).selectByField(anyString(), any());
    }

    @Test
    void testGetByClientName_Success() {
        // Arrange
        List<ProductPojo> products = Arrays.asList(testProduct);
        when(productDao.selectByClientName("TestClient")).thenReturn(products);

        // Act
        List<ProductPojo> result = productApi.getByClientName("TestClient");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productDao).selectByClientName("TestClient");
    }

    @Test
    void testGetByClientName_NullClientName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByClientName(null));
        verify(productDao, never()).selectByClientName(any());
    }

    @Test
    void testGetByClientName_EmptyClientName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByClientName(""));
        verify(productDao, never()).selectByClientName(any());
    }

    @Test
    void testHasProductsByClientName_Success() {
        // Arrange
        when(productDao.hasProductsByClientName("TestClient")).thenReturn(true);

        // Act
        boolean result = productApi.hasProductsByClientName("TestClient");

        // Assert
        assertTrue(result);
        verify(productDao).hasProductsByClientName("TestClient");
    }

    @Test
    void testHasProductsByClientName_NullClientName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.hasProductsByClientName(null));
        verify(productDao, never()).hasProductsByClientName(any());
    }

    @Test
    void testHasProductsByClientName_EmptyClientName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.hasProductsByClientName(""));
        verify(productDao, never()).hasProductsByClientName(any());
    }
} 