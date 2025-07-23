package org.example.inventory.integration;

import org.example.dto.InventoryDto;
import org.example.flow.InventoryFlow;
import org.example.api.InventoryApi;
import org.example.dao.InventoryDao;
import org.example.dao.ProductDao;
import org.example.dao.ClientDao;
import org.example.api.ProductApi;
import org.example.api.ClientApi;
import org.example.model.form.InventoryForm;
import org.example.model.data.InventoryData;
import org.example.pojo.InventoryPojo;
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
@ContextConfiguration(classes = {InventoryDtoFlowApiIntegrationTest.TestConfig.class})
class InventoryDtoFlowApiIntegrationTest {

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
        public InventoryDao inventoryDao() {
            return mock(InventoryDao.class);
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
        public ProductApi productApi() {
            return mock(ProductApi.class);
        }
        
        @Bean
        @Primary
        public ClientApi clientApi() {
            return mock(ClientApi.class);
        }
        
        @Bean
        public InventoryApi inventoryApi() {
            return new InventoryApi();
        }
        
        @Bean
        public InventoryFlow inventoryFlow() {
            return new InventoryFlow();
        }
        
        @Bean
        public InventoryDto inventoryDto() {
            return new InventoryDto();
        }
    }

    @Mock
    private InventoryDao inventoryDao;

    @Mock
    private ProductApi productApi;

    @Mock
    private ClientApi clientApi;

    private InventoryDto inventoryDto;
    private InventoryFlow inventoryFlow;
    private InventoryApi inventoryApi;

    private InventoryPojo testInventory;
    private InventoryForm testForm;
    private ProductPojo testProduct;
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

        testInventory = new InventoryPojo();
        testInventory.setId(1);
        testInventory.setProductId(1);
        testInventory.setQuantity(50);

        testForm = new InventoryForm();
        testForm.setBarcode("TEST123");
        testForm.setProductName("Test Product");
        testForm.setClientName("Test Client");
        testForm.setQuantity(50);
        testForm.setMrp(100.0);

        // Get beans from Spring context
        inventoryDto = new InventoryDto();
        inventoryFlow = new InventoryFlow();
        inventoryApi = new InventoryApi();

        // Inject mocked dependencies using reflection
        injectMockDependencies();
    }

    private void injectMockDependencies() {
        try {
            // Inject inventoryDao into inventoryApi
            var inventoryDaoField = InventoryApi.class.getDeclaredField("inventoryDao");
            inventoryDaoField.setAccessible(true);
            inventoryDaoField.set(inventoryApi, inventoryDao);

            // Inject inventoryDao into AbstractApi's 'dao' field
            var abstractDaoField = org.example.api.AbstractApi.class.getDeclaredField("dao");
            abstractDaoField.setAccessible(true);
            abstractDaoField.set(inventoryApi, inventoryDao);

            // Inject productApi into inventoryApi
            var productApiField = InventoryApi.class.getDeclaredField("productApi");
            productApiField.setAccessible(true);
            productApiField.set(inventoryApi, productApi);

            // Inject clientApi into inventoryApi
            var clientApiField = InventoryApi.class.getDeclaredField("clientApi");
            clientApiField.setAccessible(true);
            clientApiField.set(inventoryApi, clientApi);

            // Inject inventoryApi into inventoryFlow
            var apiField = InventoryFlow.class.getDeclaredField("api");
            apiField.setAccessible(true);
            apiField.set(inventoryFlow, inventoryApi);

            // Inject inventoryFlow into inventoryDto
            var flowField = InventoryDto.class.getDeclaredField("inventoryFlow");
            flowField.setAccessible(true);
            flowField.set(inventoryDto, inventoryFlow);

            var productApiDtoField = InventoryDto.class.getDeclaredField("productApi");
            productApiDtoField.setAccessible(true);
            productApiDtoField.set(inventoryDto, productApi);

            var clientApiDtoField = InventoryDto.class.getDeclaredField("clientApi");
            clientApiDtoField.setAccessible(true);
            clientApiDtoField.set(inventoryDto, clientApi);

            // Inject inventoryApi into inventoryDto (inherited from AbstractDto)
            var abstractApiField = org.example.dto.AbstractDto.class.getDeclaredField("api");
            abstractApiField.setAccessible(true);
            abstractApiField.set(inventoryDto, inventoryApi);

        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }
    }

    @Test
    void testDtoToFlowToApi_AddInventory() {
        // Arrange
        when(productApi.getByBarcode("TEST123")).thenReturn(testProduct);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);
        doAnswer(invocation -> {
            InventoryPojo inventory = invocation.getArgument(0);
            inventory.setId(1); // Set the ID as if it was inserted
            return null;
        }).when(inventoryDao).insert(any(InventoryPojo.class));

        // Act
        InventoryData result = inventoryDto.add(testForm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getProductName());
        assertEquals(50, result.getQuantity());
        assertEquals(100.0, result.getMrp());
        
        // Verify DAO was called
        verify(productApi, times(1)).getByBarcode("TEST123");
        verify(inventoryDao).insert(any(InventoryPojo.class));
    }

    @Test
    void testDtoToFlowToApi_GetInventory() {
        // Arrange
        when(inventoryDao.select(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);

        // Act
        InventoryData result = inventoryDto.get(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(1, result.getProductId());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getProductName());
        assertEquals(50, result.getQuantity());
        
        // Verify DAO was called
        verify(inventoryDao).select(1);
        verify(productApi).get(1);
    }

    @Test
    void testDtoToFlowToApi_GetAllInventory() {
        // Arrange
        InventoryPojo inventory2 = new InventoryPojo();
        inventory2.setId(2);
        inventory2.setProductId(2);
        inventory2.setQuantity(25);
        
        when(inventoryDao.selectAll()).thenReturn(Arrays.asList(testInventory, inventory2));
        when(productApi.get(1)).thenReturn(testProduct);
        when(productApi.get(2)).thenReturn(testProduct);

        // Act
        List<InventoryData> results = inventoryDto.getAll();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getProductId());
        assertEquals(2, results.get(1).getProductId());
        assertEquals("TEST123", results.get(0).getBarcode());
        assertEquals("TEST123", results.get(1).getBarcode());
        
        // Verify DAO was called
        verify(inventoryDao).selectAll();
        verify(productApi, times(2)).get(anyInt());
    }

    @Test
    void testDtoToFlowToApi_UpdateInventory() {
        // Arrange
        InventoryForm updateForm = new InventoryForm();
        updateForm.setBarcode("TEST123");
        updateForm.setQuantity(75);

        when(inventoryDao.select(1)).thenReturn(testInventory);
        when(productApi.getByBarcode("TEST123")).thenReturn(testProduct);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);
        doAnswer(invocation -> {
            Integer id = invocation.getArgument(0);
            InventoryPojo updatedInventory = invocation.getArgument(1);
            testInventory.setQuantity(updatedInventory.getQuantity());
            return null;
        }).when(inventoryDao).update(eq(1), any(InventoryPojo.class));

        // Act
        InventoryData result = inventoryDto.update(1, updateForm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getProductName());
        assertEquals(75, result.getQuantity());
        
        // Verify DAO was called
        verify(inventoryDao, times(2)).select(1);
        verify(productApi, times(1)).getByBarcode("TEST123");
        verify(inventoryDao).update(eq(1), any(InventoryPojo.class));
    }

    @Test
    void testDtoToFlowToApi_AddStock() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);
        doAnswer(invocation -> {
            Integer id = invocation.getArgument(0);
            InventoryPojo updatedInventory = invocation.getArgument(1);
            testInventory.setQuantity(updatedInventory.getQuantity());
            return null;
        }).when(inventoryDao).update(eq(1), any(InventoryPojo.class));

        // Act
        InventoryData result = inventoryDto.addStock(1, 25);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getProductName());
        assertEquals(75, result.getQuantity()); // 50 + 25
        
        // Verify DAO was called
        verify(inventoryDao, times(2)).getByProductId(1);
        verify(inventoryDao).update(eq(1), any(InventoryPojo.class));
    }

    @Test
    void testDtoToFlowToApi_RemoveStock() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);
        doAnswer(invocation -> {
            Integer id = invocation.getArgument(0);
            InventoryPojo updatedInventory = invocation.getArgument(1);
            testInventory.setQuantity(updatedInventory.getQuantity());
            return null;
        }).when(inventoryDao).update(eq(1), any(InventoryPojo.class));

        // Act
        InventoryData result = inventoryDto.removeStock(1, 20);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getProductName());
        assertEquals(30, result.getQuantity()); // 50 - 20
        
        // Verify DAO was called
        verify(inventoryDao, times(2)).getByProductId(1);
        verify(inventoryDao).update(eq(1), any(InventoryPojo.class));
    }

    @Test
    void testDtoToFlowToApi_SetStock() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);
        when(clientApi.get(1)).thenReturn(testClient);
        doAnswer(invocation -> {
            Integer id = invocation.getArgument(0);
            InventoryPojo updatedInventory = invocation.getArgument(1);
            testInventory.setQuantity(updatedInventory.getQuantity());
            return null;
        }).when(inventoryDao).update(eq(1), any(InventoryPojo.class));

        // Act
        InventoryData result = inventoryDto.setStock(1, 100);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getProductName());
        assertEquals(100, result.getQuantity());
        
        // Verify DAO was called
        verify(inventoryDao, times(2)).getByProductId(1);
        verify(inventoryDao).update(eq(1), any(InventoryPojo.class));
    }

    @Test
    void testDtoToFlowToApi_GetByProductId() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);
        when(productApi.get(1)).thenReturn(testProduct);

        // Act
        InventoryData result = inventoryDto.getByProductId(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals("TEST123", result.getBarcode());
        assertEquals("Test Product", result.getProductName());
        assertEquals(50, result.getQuantity());
        
        // Verify DAO was called
        verify(inventoryDao).getByProductId(1);
        verify(productApi).get(1);
    }

    @Test
    void testDtoToFlowToApi_Validation_BarcodeRequired() {
        // Arrange
        InventoryForm invalidForm = new InventoryForm();
        invalidForm.setBarcode(null);
        invalidForm.setQuantity(50);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            inventoryDto.add(invalidForm);
        });
        
        assertEquals("Product barcode is required", exception.getMessage());
        
        // Verify DAO was not called
        verify(inventoryDao, never()).insert(any());
    }

    @Test
    void testDtoToFlowToApi_Validation_ProductNotFound() {
        // Arrange
        when(productApi.getByBarcode("INVALID123")).thenThrow(new ApiException("Product not found"));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            testForm.setBarcode("INVALID123");
            inventoryDto.add(testForm);
        });
        
        assertEquals("Product with barcode 'INVALID123' not found", exception.getMessage());
        
        // Verify DAO was not called
        verify(inventoryDao, never()).insert(any());
    }

    @Test
    void testDtoToFlowToApi_Validation_AddStockNegativeQuantity() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            inventoryDto.addStock(1, -5);
        });
        
        assertEquals("Quantity to add must be positive", exception.getMessage());
        
        // Verify DAO was not called
        verify(inventoryDao, never()).update(anyInt(), any());
    }

    @Test
    void testDtoToFlowToApi_Validation_RemoveStockInsufficientQuantity() {
        // Arrange
        when(inventoryDao.getByProductId(1)).thenReturn(testInventory);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            inventoryDto.removeStock(1, 100);
        });
        
        assertEquals("Insufficient stock. Available: 50, Requested: 100", exception.getMessage());
        
        // Verify DAO was not called
        verify(inventoryDao, never()).update(anyInt(), any());
    }

    @Test
    void testDtoToFlowToApi_Validation_SetStockNegativeQuantity() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            inventoryDto.setStock(1, -10);
        });
        
        assertEquals("Stock quantity cannot be negative", exception.getMessage());
        
        // Verify DAO was not called
        verify(inventoryDao, never()).update(anyInt(), any());
    }
} 