package org.example.product.unit;

import org.example.api.ProductApi;
import org.example.api.ClientApi;
import org.example.dao.ProductDao;
import org.example.dao.InventoryDao;
import org.example.pojo.ProductPojo;
import org.example.pojo.ClientPojo;
import org.example.exception.ApiException;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
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
        testClient = new ClientPojo();
        testClient.setId(1);
        testClient.setClientName("test client");
        testClient.setStatus(true);

        testProduct = new ProductPojo();
        testProduct.setId(1);
        testProduct.setName("Test Product");
        testProduct.setBarcode("123456789");
        testProduct.setMrp(100.0);
        testProduct.setImageUrl("test-image.jpg");
        testProduct.setClientId(1);

        // Inject the dao field into AbstractApi
        java.lang.reflect.Field daoField = org.example.api.AbstractApi.class.getDeclaredField("dao");
        daoField.setAccessible(true);
        daoField.set(productApi, productDao);
    }

    @Test
    void testGetByName_Success() {
        // Arrange
        when(productDao.selectByField("name", "Test Product")).thenReturn(testProduct);

        // Act
        ProductPojo result = productApi.getByName("Test Product");

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productDao).selectByField("name", "Test Product");
    }

    @Test
    void testGetByName_NotFound() {
        // Arrange
        when(productDao.selectByField("name", "Nonexistent Product")).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByName("Nonexistent Product"));
        verify(productDao).selectByField("name", "Nonexistent Product");
    }

    @Test
    void testGetByNameLike_Success() {
        // Arrange
        List<ProductPojo> products = Arrays.asList(testProduct);
        when(productDao.selectByFieldLike("name", "Test")).thenReturn(products);

        // Act
        List<ProductPojo> result = productApi.getByNameLike("Test");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        verify(productDao).selectByFieldLike("name", "Test");
    }

    @Test
    void testGetByBarcode_Success() {
        // Arrange
        when(productDao.selectByField("barcode", "123456789")).thenReturn(testProduct);

        // Act
        ProductPojo result = productApi.getByBarcode("123456789");

        // Assert
        assertNotNull(result);
        assertEquals("123456789", result.getBarcode());
        verify(productDao).selectByField("barcode", "123456789");
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
    void testGetByBarcodeLike_Success() {
        // Arrange
        List<ProductPojo> products = Arrays.asList(testProduct);
        when(productDao.selectByFieldLike("barcode", "123")).thenReturn(products);

        // Act
        List<ProductPojo> result = productApi.getByBarcodeLike("123");

        // Assert
        assertEquals(1, result.size());
        assertEquals("123456789", result.get(0).getBarcode());
        verify(productDao).selectByFieldLike("barcode", "123");
    }

    @Test
    void testGetByBarcodeLike_NullBarcode() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByBarcodeLike(null));
        verify(productDao, never()).selectByFieldLike(anyString(), any());
    }

    @Test
    void testGetByBarcodeLike_EmptyBarcode() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByBarcodeLike(""));
        verify(productDao, never()).selectByFieldLike(anyString(), any());
    }

    @Test
    void testGetByClientId_Success() {
        // Arrange
        List<ProductPojo> products = Arrays.asList(testProduct);
        when(productDao.selectByClientId(1)).thenReturn(products);

        // Act
        List<ProductPojo> result = productApi.getByClientId(1);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        verify(productDao).selectByClientId(1);
    }

    @Test
    void testGetByClientId_NullClientId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByClientId(null));
        verify(productDao, never()).selectByClientId(any());
    }

    @Test
    void testGetByClientName_Success() {
        // Arrange
        List<ProductPojo> products = Arrays.asList(testProduct);
        when(clientApi.getByName("test client")).thenReturn(testClient);
        when(productDao.selectByClientId(1)).thenReturn(products);

        // Act
        List<ProductPojo> result = productApi.getByClientName("test client");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        verify(clientApi).getByName("test client");
        verify(productDao).selectByClientId(1);
    }

    @Test
    void testGetByClientName_ClientNotFound() {
        // Arrange
        when(clientApi.getByName("nonexistent client")).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByClientName("nonexistent client"));
        verify(clientApi).getByName("nonexistent client");
        verify(productDao, never()).selectByClientId(any());
    }

    @Test
    void testGetByClientName_NullClientName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByClientName(null));
        verify(clientApi, never()).getByName(any());
        verify(productDao, never()).selectByClientId(any());
    }

    @Test
    void testGetByClientName_EmptyClientName() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByClientName(""));
        verify(clientApi, never()).getByName(any());
        verify(productDao, never()).selectByClientId(any());
    }

    @Test
    void testGetByClientName_ClientApiException() {
        // Arrange
        when(clientApi.getByName("test client")).thenThrow(new ApiException("Client error"));

        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByClientName("test client"));
        verify(clientApi).getByName("test client");
        verify(productDao, never()).selectByClientId(any());
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
    void testHasProductsByClientId_NullClientId() {
        // Act
        boolean result = productApi.hasProductsByClientId(null);

        // Assert
        assertFalse(result);
        verify(productDao, never()).hasProductsByClientId(any());
    }

    @Test
    void testHasProductsByClientName_True() {
        // Arrange
        when(clientApi.getByName("test client")).thenReturn(testClient);
        when(productDao.hasProductsByClientId(1)).thenReturn(true);

        // Act
        boolean result = productApi.hasProductsByClientName("test client");

        // Assert
        assertTrue(result);
        verify(clientApi).getByName("test client");
        verify(productDao).hasProductsByClientId(1);
    }

    @Test
    void testHasProductsByClientName_False() {
        // Arrange
        when(clientApi.getByName("test client")).thenReturn(testClient);
        when(productDao.hasProductsByClientId(1)).thenReturn(false);

        // Act
        boolean result = productApi.hasProductsByClientName("test client");

        // Assert
        assertFalse(result);
        verify(clientApi).getByName("test client");
        verify(productDao).hasProductsByClientId(1);
    }

    @Test
    void testHasProductsByClientName_NullClientName() {
        // Act
        boolean result = productApi.hasProductsByClientName(null);

        // Assert
        assertFalse(result);
        verify(clientApi, never()).getByName(any());
        verify(productDao, never()).hasProductsByClientId(any());
    }

    @Test
    void testHasProductsByClientName_EmptyClientName() {
        // Act
        boolean result = productApi.hasProductsByClientName("");

        // Assert
        assertFalse(result);
        verify(clientApi, never()).getByName(any());
        verify(productDao, never()).hasProductsByClientId(any());
    }

    @Test
    void testHasProductsByClientName_ClientNotFound() {
        // Arrange
        when(clientApi.getByName("nonexistent client")).thenReturn(null);

        // Act
        boolean result = productApi.hasProductsByClientName("nonexistent client");

        // Assert
        assertFalse(result);
        verify(clientApi).getByName("nonexistent client");
        verify(productDao, never()).hasProductsByClientId(any());
    }

    @Test
    void testHasProductsByClientName_ClientApiException() {
        // Arrange
        when(clientApi.getByName("test client")).thenThrow(new RuntimeException("Client error"));

        // Act
        boolean result = productApi.hasProductsByClientName("test client");

        // Assert
        assertFalse(result);
        verify(clientApi).getByName("test client");
        verify(productDao, never()).hasProductsByClientId(any());
    }

    @Test
    void testGetByNameLikePaginated_Success() {
        // Arrange
        PaginationRequest request = new PaginationRequest();
        request.setPageNumber(0);
        request.setPageSize(10);
        
        PaginationResponse<ProductPojo> expectedResponse = new PaginationResponse<>();
        expectedResponse.setContent(Arrays.asList(testProduct));
        expectedResponse.setTotalElements(1);
        
        when(productDao.getPaginated(any())).thenReturn(expectedResponse);

        // Act
        PaginationResponse<ProductPojo> result = productApi.getByNameLikePaginated("Test", request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(productDao).getPaginated(any());
    }

    @Test
    void testGetByClientIdPaginated_Success() {
        // Arrange
        PaginationRequest request = new PaginationRequest();
        request.setPageNumber(0);
        request.setPageSize(10);
        
        PaginationResponse<ProductPojo> expectedResponse = new PaginationResponse<>();
        expectedResponse.setContent(Arrays.asList(testProduct));
        expectedResponse.setTotalElements(1);
        
        when(productDao.selectByClientIdPaginated(1, request)).thenReturn(expectedResponse);

        // Act
        PaginationResponse<ProductPojo> result = productApi.getByClientIdPaginated(1, request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(productDao).selectByClientIdPaginated(1, request);
    }

    @Test
    void testGetByClientIdPaginated_NullClientId() {
        // Arrange
        PaginationRequest request = new PaginationRequest();

        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByClientIdPaginated(null, request));
        verify(productDao, never()).selectByClientIdPaginated(any(), any());
    }

    @Test
    void testGetByClientNamePaginated_Success() {
        // Arrange
        PaginationRequest request = new PaginationRequest();
        request.setPageNumber(0);
        request.setPageSize(10);
        
        PaginationResponse<ProductPojo> expectedResponse = new PaginationResponse<>();
        expectedResponse.setContent(Arrays.asList(testProduct));
        expectedResponse.setTotalElements(1);
        
        when(clientApi.getByName("test client")).thenReturn(testClient);
        when(productDao.selectByClientIdPaginated(1, request)).thenReturn(expectedResponse);

        // Act
        PaginationResponse<ProductPojo> result = productApi.getByClientNamePaginated("test client", request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(clientApi).getByName("test client");
        verify(productDao).selectByClientIdPaginated(1, request);
    }

    @Test
    void testGetByClientNamePaginated_ClientNotFound() {
        // Arrange
        PaginationRequest request = new PaginationRequest();
        when(clientApi.getByName("nonexistent client")).thenReturn(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByClientNamePaginated("nonexistent client", request));
        verify(clientApi).getByName("nonexistent client");
        verify(productDao, never()).selectByClientIdPaginated(any(), any());
    }

    @Test
    void testGetByClientNamePaginated_NullClientName() {
        // Arrange
        PaginationRequest request = new PaginationRequest();

        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByClientNamePaginated(null, request));
        verify(clientApi, never()).getByName(any());
        verify(productDao, never()).selectByClientIdPaginated(any(), any());
    }

    @Test
    void testGetByClientNamePaginated_EmptyClientName() {
        // Arrange
        PaginationRequest request = new PaginationRequest();

        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByClientNamePaginated("", request));
        verify(clientApi, never()).getByName(any());
        verify(productDao, never()).selectByClientIdPaginated(any(), any());
    }

    @Test
    void testGetByClientNamePaginated_ClientApiException() {
        // Arrange
        PaginationRequest request = new PaginationRequest();
        when(clientApi.getByName("test client")).thenThrow(new ApiException("Client error"));

        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.getByClientNamePaginated("test client", request));
        verify(clientApi).getByName("test client");
        verify(productDao, never()).selectByClientIdPaginated(any(), any());
    }

    @Test
    void testAdd_Success() {
        // Arrange
        doNothing().when(productDao).insert(testProduct);

        // Act
        productApi.add(testProduct);

        // Assert
        verify(productDao).insert(testProduct);
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
        assertEquals(1, result.getId());
        verify(productDao).select(1);
    }

    @Test
    void testGet_NullId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.get(null));
        verify(productDao, never()).select(any());
    }

    @Test
    void testGet_ZeroId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.get(0));
        verify(productDao, never()).select(any());
    }

    @Test
    void testGet_NegativeId() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productApi.get(-1));
        verify(productDao, never()).select(any());
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
    void testUpdate_Success() {
        // Arrange
        doNothing().when(productDao).update(1, testProduct);

        // Act
        productApi.update(1, testProduct);

        // Assert
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
    void testGetAll_Success() {
        // Arrange
        List<ProductPojo> products = Arrays.asList(testProduct);
        when(productDao.selectAll()).thenReturn(products);

        // Act
        List<ProductPojo> result = productApi.getAll();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        verify(productDao).selectAll();
    }
} 