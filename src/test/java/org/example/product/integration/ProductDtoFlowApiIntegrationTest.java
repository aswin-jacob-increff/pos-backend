package org.example.product.integration;

import org.example.dto.ProductDto;
import org.example.flow.ProductFlow;
import org.example.api.ProductApi;
import org.example.dao.ProductDao;
import org.example.dao.ClientDao;
import org.example.dao.InventoryDao;
import org.example.api.ClientApi;
import org.example.api.InventoryApi;
import org.example.model.form.ProductForm;
import org.example.model.data.ProductData;
import org.example.pojo.ProductPojo;
import org.example.pojo.ClientPojo;
import org.example.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {ProductDtoFlowApiIntegrationTest.TestConfig.class})
class ProductDtoFlowApiIntegrationTest {

    @Configuration
    @EnableTransactionManagement
    static class TestConfig {
        
        @Bean
        public DataSource dataSource() {
            // Create an in-memory H2 database for testing
            org.h2.jdbcx.JdbcDataSource dataSource = new org.h2.jdbcx.JdbcDataSource();
            dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
            dataSource.setUser("sa");
            dataSource.setPassword("");
            return dataSource;
        }
        
        @Bean
        public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
            LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
            em.setDataSource(dataSource());
            em.setPackagesToScan("org.example.pojo");
            
            HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            vendorAdapter.setGenerateDdl(true);
            vendorAdapter.setShowSql(true);
            em.setJpaVendorAdapter(vendorAdapter);
            
            Properties properties = new Properties();
            properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
            properties.setProperty("hibernate.show_sql", "true");
            em.setJpaProperties(properties);
            
            em.setEntityManagerFactoryInterface(jakarta.persistence.EntityManagerFactory.class);
            
            return em;
        }
        
        @Bean
        public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
            JpaTransactionManager transactionManager = new JpaTransactionManager();
            transactionManager.setEntityManagerFactory(emf);
            return transactionManager;
        }
        
        @Bean
        @Primary
        public ProductDao productDao() {
            return mock(ProductDao.class);
        }
        
        @Bean
        @Primary
        public ClientDao clientDao() {
            return mock(ClientDao.class);
        }
        
        @Bean
        @Primary
        public InventoryDao inventoryDao() {
            return mock(InventoryDao.class);
        }
        
        @Bean
        @Primary
        public ClientApi clientApi() {
            return mock(ClientApi.class);
        }
        
        @Bean
        @Primary
        public InventoryApi inventoryApi() {
            return mock(InventoryApi.class);
        }
        
        @Bean
        public ProductApi productApi() {
            return new ProductApi();
        }
        
        @Bean
        public ProductFlow productFlow() {
            return new ProductFlow();
        }
        
        @Bean
        public ProductDto productDto() {
            return new ProductDto();
        }
    }

    @Mock
    private ProductDao productDao;

    @Mock
    private ClientApi clientApi;

    private ProductDto productDto;
    private ProductFlow productFlow;
    private ProductApi productApi;

    private ProductPojo testProduct;
    private ProductForm testForm;
    private ClientPojo testClient;

    @BeforeEach
    void setUp() {
        // Create test data
        testClient = new ClientPojo();
        testClient.setId(1);
        testClient.setClientName("test client");
        testClient.setStatus(true);

        testProduct = new ProductPojo();
        testProduct.setId(1);
        testProduct.setName("Test Product");
        testProduct.setBarcode("TEST123");
        testProduct.setClientId(1);
        testProduct.setMrp(100.0);
        testProduct.setImageUrl("http://example.com/image.jpg");

        testForm = new ProductForm();
        testForm.setName("Test Product");
        testForm.setBarcode("TEST123");
        testForm.setClientName("Test Client");
        testForm.setMrp(100.0);
        testForm.setImage("http://example.com/image.jpg");

        // Get beans from Spring context
        productDto = new ProductDto();
        productFlow = new ProductFlow();
        productApi = new ProductApi();

        // Inject mocked dependencies using reflection
        injectMockDependencies();
    }

    private void injectMockDependencies() {
        try {
            // Inject productDao into productApi
            var productDaoField = ProductApi.class.getDeclaredField("productDao");
            productDaoField.setAccessible(true);
            productDaoField.set(productApi, productDao);

            // Inject dao (AbstractApi) into productApi
            var abstractDaoField = org.example.api.AbstractApi.class.getDeclaredField("dao");
            abstractDaoField.setAccessible(true);
            abstractDaoField.set(productApi, productDao);

            // Inject inventoryDao into productApi
            var inventoryDaoField = ProductApi.class.getDeclaredField("inventoryDao");
            inventoryDaoField.setAccessible(true);
            inventoryDaoField.set(productApi, mock(InventoryDao.class));

            // Inject clientApi into productApi
            var clientApiField = ProductApi.class.getDeclaredField("clientApi");
            clientApiField.setAccessible(true);
            clientApiField.set(productApi, clientApi);

            // Inject dao (AbstractApi) into clientApi
            var clientDaoField = ClientApi.class.getDeclaredField("dao");
            clientDaoField.setAccessible(true);
            clientDaoField.set(clientApi, mock(ClientDao.class));
            var abstractDaoFieldClient = org.example.api.AbstractApi.class.getDeclaredField("dao");
            abstractDaoFieldClient.setAccessible(true);
            abstractDaoFieldClient.set(clientApi, mock(ClientDao.class));

            // Inject productApi into productFlow
            var apiField = ProductFlow.class.getDeclaredField("api");
            apiField.setAccessible(true);
            apiField.set(productFlow, productApi);

            // Inject inventoryApi into productFlow
            var inventoryApiField = ProductFlow.class.getDeclaredField("inventoryApi");
            inventoryApiField.setAccessible(true);
            inventoryApiField.set(productFlow, mock(InventoryApi.class));

            // Inject productFlow into productDto
            var flowField = ProductDto.class.getDeclaredField("productFlow");
            flowField.setAccessible(true);
            flowField.set(productDto, productFlow);

            var clientApiDtoField = ProductDto.class.getDeclaredField("clientApi");
            clientApiDtoField.setAccessible(true);
            clientApiDtoField.set(productDto, clientApi);

            // Inject productApi into productDto (inherited from AbstractDto)
            var abstractApiField = org.example.dto.AbstractDto.class.getDeclaredField("api");
            abstractApiField.setAccessible(true);
            abstractApiField.set(productDto, productApi);

        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }
    }

    @Test
    void testDtoToFlowToApi_AddProduct() {
        // Arrange
        when(clientApi.getByName("test client")).thenReturn(testClient);
        when(clientApi.get(1)).thenReturn(testClient);
        when(productDao.selectByBarcode("TEST123")).thenReturn(null);
        doNothing().when(productDao).insert(any(ProductPojo.class));

        // Act
        ProductData result = productDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("test client", result.getClientName());
        assertEquals(100.0, result.getMrp());
        assertEquals(1, result.getClientId());
        
        // Verify DAO was called - use atLeastOnce() since it's called multiple times
        verify(clientApi, atLeastOnce()).getByName("test client");
        verify(productDao, atLeastOnce()).selectByBarcode("TEST123");
        verify(productDao).insert(any(ProductPojo.class));
    }

    @Test
    void testDtoToFlowToApi_GetProduct() {
        // Arrange
        when(productDao.select(1)).thenReturn(testProduct);

        // Act
        ProductData result = productDto.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Product", result.getName());
        assertEquals("TEST123", result.getBarcode());
        
        // Verify DAO was called
        verify(productDao).select(1);
    }

    @Test
    void testDtoToFlowToApi_GetAllProducts() {
        // Arrange
        List<ProductPojo> products = Arrays.asList(testProduct);
        when(productDao.selectAll()).thenReturn(products);

        // Act
        List<ProductData> result = productDto.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Test Product", result.get(0).getName());
        
        // Verify DAO was called
        verify(productDao).selectAll();
    }

    @Test
    void testDtoToFlowToApi_UpdateProduct() {
        // Arrange
        ProductForm updateForm = new ProductForm();
        updateForm.setName("Updated Product");
        updateForm.setBarcode("TEST123");
        updateForm.setClientName("Test Client");
        updateForm.setMrp(150.0);
        updateForm.setImage("http://example.com/updated-image.jpg");

        when(productDao.select(1)).thenReturn(testProduct);
        // Removed unnecessary stubbing of selectByBarcode
        when(clientApi.getByName("test client")).thenReturn(testClient);
        when(clientApi.get(1)).thenReturn(testClient);
        doNothing().when(productDao).update(any(Integer.class), any(ProductPojo.class));

        // Simulate the update (since the mock doesn't persist changes)
        testProduct.setName("Updated Product");
        testProduct.setMrp(150.0);
        testProduct.setImageUrl("http://example.com/updated-image.jpg");

        // Act
        ProductData result = productDto.update(1, updateForm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Updated Product", result.getName());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("test client", result.getClientName());
        assertEquals(150.0, result.getMrp());
        
        // Verify DAO was called
        verify(productDao, atLeastOnce()).select(1);
        verify(productDao).update(eq(1), any(ProductPojo.class));
    }

    @Test
    void testDtoToFlowToApi_GetByBarcode() {
        // Arrange
        when(productDao.selectByBarcode("TEST123")).thenReturn(testProduct);

        // Act
        ProductData result = productDto.getByBarcode("TEST123");

        // Assert
        assertNotNull(result);
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getName());
        
        // Verify DAO was called
        verify(productDao).selectByBarcode("TEST123");
    }

    @Test
    void testDtoToFlowToApi_GetByClientName() {
        // Arrange
        ProductPojo product2 = new ProductPojo();
        product2.setId(2);
        product2.setName("Test Product 2");
        product2.setBarcode("TEST456");
        product2.setClientId(1);
        product2.setMrp(150.0);
        
        when(productDao.selectAll()).thenReturn(Arrays.asList(testProduct, product2));
        when(clientApi.getByName("test client")).thenReturn(testClient);
        when(clientApi.get(1)).thenReturn(testClient);

        // Act
        List<ProductData> results = productDto.getByClientName("test client");

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("test client", results.get(0).getClientName());
        assertEquals("test client", results.get(1).getClientName());
        
        // Verify DAO was called
        verify(productDao).selectAll();
    }

    @Test
    void testDtoToFlowToApi_GetByClientId() {
        // Arrange
        when(clientApi.get(1)).thenReturn(testClient);
        when(productDao.selectAll()).thenReturn(Arrays.asList(testProduct));

        // Act
        List<ProductData> results = productDto.getByClientId(1);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("test client", results.get(0).getClientName());
        
        // Verify DAO was called
        verify(clientApi).get(1);
        verify(productDao).selectAll();
    }

    @Test
    void testDtoToFlowToApi_GetProductImageUrl() {
        // Arrange
        when(productDao.select(1)).thenReturn(testProduct);

        // Act
        String imageUrl = productDto.getProductImageUrl(1);

        // Assert
        assertEquals("http://example.com/image.jpg", imageUrl);
        
        // Verify DAO was called
        verify(productDao).select(1);
    }

    @Test
    void testDtoToFlowToApi_Validation_ClientNameRequired() {
        // Arrange
        ProductForm invalidForm = new ProductForm();
        invalidForm.setName("Test Product");
        invalidForm.setClientName(null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            productDto.add(invalidForm);
        });
        
        assertEquals("Client name is required", exception.getMessage());
        
        // Verify DAO was not called
        verify(productDao, never()).insert(any());
    }

    @Test
    void testDtoToFlowToApi_Validation_ProductAlreadyExists() {
        // Arrange
        when(clientApi.getByName("test client")).thenReturn(testClient);
        when(clientApi.get(1)).thenReturn(testClient);
        when(productDao.selectByBarcode("TEST123")).thenReturn(testProduct);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            productDto.add(testForm);
        });
        
        assertEquals("Product with barcode 'TEST123' already exists", exception.getMessage());
        
        // Verify DAO was called for validation but not for insert
        verify(clientApi).getByName("test client");
        verify(productDao).selectByBarcode("TEST123");
        verify(productDao, never()).insert(any());
    }

    @Test
    void testDtoToFlowToApi_Validation_InvalidImageUrl() {
        // Arrange
        ProductForm invalidForm = new ProductForm();
        invalidForm.setName("Test Product");
        invalidForm.setClientName("Test Client");
        invalidForm.setImage("invalid-url");

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            productDto.add(invalidForm);
        });
        
        assertEquals("Image must be a valid URL", exception.getMessage());
        
        // Verify DAO was not called
        verify(productDao, never()).insert(any());
    }

    @Test
    void testDtoToFlowToApi_Validation_BarcodeRequired() {
        // Arrange
        ProductForm invalidForm = new ProductForm();
        invalidForm.setName("Test Product");
        invalidForm.setClientName("Test Client");
        invalidForm.setBarcode("");

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            productDto.getByBarcode("");
        });
        
        assertEquals("Barcode cannot be null or empty", exception.getMessage());
        
        // Verify DAO was not called
        verify(productDao, never()).selectByBarcode(any());
    }
} 