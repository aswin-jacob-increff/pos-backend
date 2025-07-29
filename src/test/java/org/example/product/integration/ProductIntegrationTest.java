package org.example.product.integration;

import org.example.dto.ProductDto;
import org.example.api.ProductApi;
import org.example.api.ClientApi;
import org.example.model.data.ProductData;
import org.example.model.form.ProductForm;
import org.example.model.data.TsvUploadResult;
import org.example.pojo.ProductPojo;
import org.example.pojo.ClientPojo;
import org.example.exception.ApiException;
import org.example.util.ProductTsvParser;
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
class ProductIntegrationTest {

    @Mock
    private ProductApi productApi;

    @Mock
    private ClientApi clientApi;

    @InjectMocks
    private ProductDto productDto;

    private ProductForm testForm;
    private ProductPojo testProduct;
    private ProductData testProductData;
    private ClientPojo testClient;

    @BeforeEach
    void setUp() throws Exception {
        testClient = new ClientPojo();
        testClient.setId(1);
        testClient.setClientName("test client");
        testClient.setStatus(true);

        testForm = new ProductForm();
        testForm.setName("Test Product");
        testForm.setBarcode("123456789");
        testForm.setMrp(100.0);
        testForm.setImage("test-image.jpg");
        testForm.setClientId(1);

        testProduct = new ProductPojo();
        testProduct.setId(1);
        testProduct.setName("Test Product");
        testProduct.setBarcode("123456789");
        testProduct.setMrp(100.0);
        testProduct.setImageUrl("test-image.jpg");
        testProduct.setClientId(1);

        testProductData = new ProductData();
        testProductData.setId(1);
        testProductData.setName("Test Product");
        testProductData.setBarcode("123456789");
        testProductData.setMrp(100.0);
        testProductData.setImageUrl("test-image.jpg");
        testProductData.setClientId(1);
        testProductData.setClientName("test client");

        // Inject the api field from AbstractDto
        Field apiField = productDto.getClass().getSuperclass().getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(productDto, productApi);
    }

    @Test
    void testAdd_Integration_Success() {
        // Arrange - API layer adds product
        doNothing().when(productApi).add(any(ProductPojo.class));

        // Act - DTO calls API
        ProductData result = productDto.add(testForm);

        // Assert - Verify the entire flow
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals("123456789", result.getBarcode());
        assertEquals(100.0, result.getMrp());
        verify(productApi).add(any(ProductPojo.class));
    }

    @Test
    void testAdd_Integration_WithClientName() {
        // Arrange
        testForm.setClientId(null);
        testForm.setClientName("test client");
        when(clientApi.getByName("test client")).thenReturn(testClient);
        doNothing().when(productApi).add(any(ProductPojo.class));

        // Act
        ProductData result = productDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(clientApi).getByName("test client");
        verify(productApi).add(any(ProductPojo.class));
    }

    @Test
    void testAdd_Integration_ValidationFailure() {
        // Arrange
        testForm.setName(null);

        // Act & Assert
        assertThrows(ApiException.class, () -> productDto.add(testForm));
        verify(productApi, never()).add(any());
    }

    @Test
    void testGet_Integration_Success() {
        // Arrange - API layer returns product
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);

        // Act - DTO calls API
        ProductData result = productDto.get(1);

        // Assert - Verify the entire flow
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Product", result.getName());
        assertEquals("test client", result.getClientName());
        verify(productApi).get(1);
        verify(clientApi).get(1);
    }

    @Test
    void testGet_Integration_ProductNotFound() {
        // Arrange
        when(productApi.get(1)).thenThrow(new ApiException("Product not found"));

        // Act & Assert
        assertThrows(ApiException.class, () -> productDto.get(1));
        verify(productApi).get(1);
        verify(clientApi, never()).get(any());
    }

    @Test
    void testUpdate_Integration_Success() {
        // Arrange
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);
        doNothing().when(productApi).update(anyInt(), any(ProductPojo.class));

        // Act
        ProductData result = productDto.update(1, testForm);

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productApi).update(eq(1), any(ProductPojo.class));
    }

    @Test
    void testUpdate_Integration_ProductNotFound() {
        // Arrange
        when(productApi.get(1)).thenThrow(new ApiException("Product not found"));

        // Act & Assert
        assertThrows(ApiException.class, () -> productDto.update(1, testForm));
        verify(productApi).get(1);
        // Note: update method calls api.update() then api.get(), so both will be called
        verify(productApi).update(eq(1), any(ProductPojo.class));
    }

    @Test
    void testGetAll_Integration_Success() {
        // Arrange
        List<ProductPojo> products = Arrays.asList(testProduct);
        when(productApi.getAll()).thenReturn(products);
        when(clientApi.get(1)).thenReturn(testClient);

        // Act
        List<ProductData> result = productDto.getAll();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        assertEquals("test client", result.get(0).getClientName());
        verify(productApi).getAll();
        verify(clientApi).get(1);
    }

    @Test
    void testGetByBarcode_Integration_Success() {
        // Arrange
        when(productApi.getByField("barcode", "123456789")).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);

        // Act
        ProductData result = productDto.getByBarcode("123456789");

        // Assert
        assertNotNull(result);
        assertEquals("123456789", result.getBarcode());
        verify(productApi).getByField("barcode", "123456789");
    }

    @Test
    void testGetByBarcode_Integration_NotFound() {
        // Arrange
        when(productApi.getByField("barcode", "123456789")).thenThrow(new ApiException("Product not found"));

        // Act & Assert
        assertThrows(ApiException.class, () -> productDto.getByBarcode("123456789"));
        verify(productApi).getByField("barcode", "123456789");
    }

    @Test
    void testGetByClientName_Integration_Success() {
        // Arrange
        List<ProductPojo> products = Arrays.asList(testProduct);
        when(productApi.getByClientName("test client")).thenReturn(products);
        when(clientApi.get(1)).thenReturn(testClient);

        // Act
        List<ProductData> result = productDto.getByClientName("test client");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        verify(productApi).getByClientName("test client");
    }

    @Test
    void testGetByClientName_Integration_ClientNotFound() {
        // Arrange
        when(productApi.getByClientName("nonexistent client")).thenThrow(new ApiException("Client not found"));

        // Act & Assert
        assertThrows(ApiException.class, () -> productDto.getByClientName("nonexistent client"));
        verify(productApi).getByClientName("nonexistent client");
    }

    @Test
    void testGetByNameLike_Integration_Success() {
        // Arrange
        List<ProductPojo> products = Arrays.asList(testProduct);
        when(productApi.getByFieldLikeWithValidation("name", "test", "name")).thenReturn(products);
        when(clientApi.get(1)).thenReturn(testClient);

        // Act
        List<ProductData> result = productDto.getByNameLike("test");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        verify(productApi).getByFieldLikeWithValidation("name", "test", "name");
    }

    @Test
    void testGetByClientId_Integration_Success() {
        // Arrange
        List<ProductPojo> products = Arrays.asList(testProduct);
        when(productApi.getByFields(new String[]{"clientId"}, new Object[]{1})).thenReturn(products);
        when(clientApi.get(1)).thenReturn(testClient);

        // Act
        List<ProductData> result = productDto.getByClientId(1);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        verify(productApi).getByFields(new String[]{"clientId"}, new Object[]{1});
    }

    @Test
    void testGetByClientId_Integration_NullClientId() {
        // Act & Assert - AbstractDto.getByFields doesn't validate individual values
        List<ProductData> result = productDto.getByClientId(null);
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(productApi).getByFields(new String[]{"clientId"}, new Object[]{null});
    }

    @Test
    void testUploadProductsFromTsv_Integration_Success() throws Exception {
        // Arrange
        String tsvContent = "name\tbarcode\tmrp\timage\tclientName\nTest Product 1\t123456789\t100.0\ttest1.jpg\ttest client\nTest Product 2\t987654321\t200.0\ttest2.jpg\ttest client";
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
        try (MockedStatic<ProductTsvParser> mockedParser = mockStatic(ProductTsvParser.class);
             MockedStatic<FileValidationUtil> mockedValidation = mockStatic(FileValidationUtil.class)) {
            
            doNothing().when(FileValidationUtil.class);
            FileValidationUtil.validateTsvFile(any(MockMultipartFile.class));
            mockedParser.when(() -> ProductTsvParser.parseWithDuplicateDetection(any(InputStream.class)))
                .thenReturn(mockResult);



            // Act
            TsvUploadResult result = productDto.uploadProductsFromTsv(file);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getTotalRows());
            assertEquals(2, result.getSuccessfulRows());
            assertEquals(0, result.getFailedRows());
        }
    }

    @Test
    void testUploadProductsFromTsv_Integration_NullFile() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productDto.uploadProductsFromTsv(null));
    }

    @Test
    void testUploadProductsFromTsv_Integration_EmptyFile() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.tsv", "text/tab-separated-values", new byte[0]);

        // Act & Assert
        assertThrows(ApiException.class, () -> productDto.uploadProductsFromTsv(file));
    }

    @Test
    void testGetByFieldLikePaginated_Integration_Success() {
        // Arrange
        org.example.model.form.PaginationRequest request = new org.example.model.form.PaginationRequest();
        request.setPageNumber(0);
        request.setPageSize(10);
        
        org.example.model.data.PaginationResponse<ProductPojo> expectedResponse = new org.example.model.data.PaginationResponse<>();
        expectedResponse.setContent(Arrays.asList(testProduct));
        expectedResponse.setTotalElements(1);
        
        when(productApi.getPaginated(any())).thenReturn(expectedResponse);
        when(clientApi.get(1)).thenReturn(testClient);

        // Act
        org.example.model.data.PaginationResponse<ProductData> result = productDto.getByFieldLikePaginated("name", "test", request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(productApi).getPaginated(any());
    }
} 